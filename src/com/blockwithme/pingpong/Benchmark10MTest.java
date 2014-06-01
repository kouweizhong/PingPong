/*
 * Copyright (C) 2013 Sebastien Diot.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blockwithme.pingpong;

import org.agilewiki.jactor2.core.reactors.IsolationReactor;
import org.agilewiki.jactor2.core.reactors.NonBlockingReactor;
import org.junit.Before;
import org.junit.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.blockwithme.pingpong.latency.impl.AkkaBlockingPinger;
import com.blockwithme.pingpong.latency.impl.AkkaBlockingPonger;
import com.blockwithme.pingpong.latency.impl.AkkaNonBlockingPinger;
import com.blockwithme.pingpong.latency.impl.AkkaNonBlockingPonger;
import com.blockwithme.pingpong.latency.impl.ExecutorServicePinger;
import com.blockwithme.pingpong.latency.impl.ExecutorServicePonger;
import com.blockwithme.pingpong.latency.impl.JActor2NonBlockingPinger;
import com.blockwithme.pingpong.latency.impl.JActor2NonBlockingPonger;
import com.blockwithme.pingpong.latency.impl.JActorBlockingPinger;
import com.blockwithme.pingpong.latency.impl.JActorBlockingPonger;
import com.blockwithme.pingpong.latency.impl.ThreadWithBlockingQueuePinger;
import com.blockwithme.pingpong.latency.impl.ThreadWithBlockingQueuePonger;
import com.blockwithme.pingpong.latency.impl.kilim.KilimTask;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

/**
 * Tests the number of seconds required to do sequential request/reply cycles,
 * for different possible Actor implementations.
 *
 * It is in essence a latency test, not a throughput test.
 */
@AxisRange(min = 0, max = 3)
@BenchmarkMethodChart(filePrefix = "Benchmark10M")
public class Benchmark10MTest extends Benchmark100MTest {

    /** Default number of messages */
    protected static final int DEFAULT_MESSAGES = Benchmark100MTest.DEFAULT_MESSAGES / 10;

    /** Allows disabling the tests easily. */
    private static final boolean RUN = true;

    /** Allows disabling the testThreadWithBlockingQueue method easily. */
    public static final boolean testExecutorService = RUN;

    /** Allows disabling the  method easily. */
    public static final boolean testThreadWithBlockingQueue = RUN;

    /** Allows disabling the testAkkaBlocking method easily. */
    public static final boolean testAkkaBlocking = RUN;

    /** Allows disabling the testAkkaNonBlocking method easily. */
    public static final boolean testAkkaNonBlocking = RUN;

    /** Allows disabling the testJActorBlocking method easily. */
    public static final boolean testJActorBlocking = RUN;

    /** Allows disabling the testKilimDirectTask method easily. */
    public static final boolean testKilimDirectTask = RUN;

    /** Allows disabling the testJActor2NonBlocking method easily. */
    public static final boolean testJActor2NonBlocking = RUN;

    /** Allows disabling the testJActor2Isolation method easily. */
    public static final boolean testJActor2Isolation = RUN;

    /** Setup all "services" for all test methods.
     * @throws Exception */
    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        MESSAGES = DEFAULT_MESSAGES;
    }

    /** Tests using an ExecutorService. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testExecutorService() throws Exception {
        if (testExecutorService) {
            final ExecutorServicePinger pinger = new ExecutorServicePinger(
                    executorService);
            final ExecutorServicePonger ponger = new ExecutorServicePonger(
                    executorService);
            try {
                try {
                    final Integer result = pinger.hammer(ponger, MESSAGES);
                    if (result != MESSAGES) {
                        throw new IllegalStateException("Expected " + MESSAGES
                                + " but got " + result);
                    }
                } finally {
                    ponger.kill();
                }
            } finally {
                pinger.kill();
            }
        }
    }

    /** Test using Threads and blocking queues. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testThreadWithBlockingQueue() throws Exception {
        if (testThreadWithBlockingQueue) {
            final ThreadWithBlockingQueuePinger pinger = new ThreadWithBlockingQueuePinger();
            final ThreadWithBlockingQueuePonger ponger = new ThreadWithBlockingQueuePonger();
            pinger.start();
            try {
                ponger.start();
                try {
                    final Integer result = pinger.hammer(ponger, MESSAGES);
                    if (result != MESSAGES) {
                        throw new IllegalStateException("Expected " + MESSAGES
                                + " but got " + result);
                    }
                } finally {
                    ponger.kill();
                }
            } finally {
                pinger.kill();
            }
        }
    }

    /** Test in Akka, using blocking Futures. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testAkkaBlocking() throws Exception {
        if (testAkkaBlocking) {
            final ActorRef pinger = system.actorOf(
                    Props.create(AkkaBlockingPinger.class), "blockingPinger");
            final ActorRef ponger = system.actorOf(
                    Props.create(AkkaBlockingPonger.class), "blockingPonger");

            final Timeout timeout = new Timeout(Duration.create(600, "seconds"));
            final Future<Object> future = Patterns.ask(pinger,
                    AkkaBlockingPinger.hammer(ponger, MESSAGES), timeout);
            final Integer result = (Integer) Await.result(future,
                    timeout.duration());
            if (result.intValue() != MESSAGES) {
                throw new IllegalStateException("Expected " + MESSAGES
                        + " but got " + result);
            }
        }
    }

    /** Test in Akka, by having a reply generate the next request, to eliminate blocking. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testAkkaNonBlocking() throws Exception {
        if (testAkkaNonBlocking) {
            final ActorRef pinger = system.actorOf(
                    Props.create(AkkaNonBlockingPinger.class),
                    "nonBlockingPinger");
            final ActorRef ponger = system.actorOf(
                    Props.create(AkkaNonBlockingPonger.class),
                    "nonBlockingPonger");

            final Timeout timeout = new Timeout(Duration.create(600, "seconds"));
            final Future<Object> future = Patterns.ask(pinger,
                    AkkaNonBlockingPinger.hammer(ponger, MESSAGES), timeout);
            final Integer result = (Integer) Await.result(future,
                    timeout.duration());
            if (result.intValue() != MESSAGES) {
                throw new IllegalStateException("Expected " + MESSAGES
                        + " but got " + result);
            }
        }
    }

    /** Test in JActors, using blocking Futures. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testJActorBlocking() throws Exception {
        if (testJActorBlocking) {
            final JActorBlockingPinger pinger = new JActorBlockingPinger(
                    jaMailboxFactory.createMailbox());
            final JActorBlockingPonger ponger = new JActorBlockingPonger(
                    jaMailboxFactory.createMailbox());
            final Integer result = pinger.hammer(ponger, MESSAGES);
            if (result.intValue() != MESSAGES) {
                throw new IllegalStateException("Expected " + MESSAGES
                        + " but got " + result);
            }
        }
    }

    /** Test with Kilim */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testKilimDirectTask() throws Exception {
        if (testKilimDirectTask) {
            final int result = KilimTask.test(MESSAGES);
            if (result != MESSAGES) {
                throw new IllegalStateException("Expected " + MESSAGES
                        + " but got " + result);
            }
        }
    }

    /** Test with JActor2/async/non-blocking/non-shared reactor. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testJActor2NonBlocking() throws Exception {
        if (testJActor2NonBlocking) {
            final JActor2NonBlockingPinger pinger = new JActor2NonBlockingPinger(
                    new NonBlockingReactor());
            final JActor2NonBlockingPonger ponger = new JActor2NonBlockingPonger(
                    new NonBlockingReactor());
            final int result = pinger.hammer(ponger, MESSAGES);
            if (result != MESSAGES) {
                throw new IllegalStateException("Expected " + MESSAGES
                        + " but got " + result);
            }
        }
    }

    /** Test with JActor2/async/Isolation/non-shared reactor. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testJActor2Isolation() throws Exception {
        if (testJActor2Isolation) {
            final JActor2NonBlockingPinger pinger = new JActor2NonBlockingPinger(
                    new IsolationReactor());
            final JActor2NonBlockingPonger ponger = new JActor2NonBlockingPonger(
                    new IsolationReactor());
            final int result = pinger.hammer(ponger, MESSAGES);
            if (result != MESSAGES) {
                throw new IllegalStateException("Expected " + MESSAGES
                        + " but got " + result);
            }
        }
    }
}
