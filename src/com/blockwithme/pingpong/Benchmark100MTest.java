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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor2.core.facilities.Facility;
import org.agilewiki.jactor2.core.reactors.NonBlockingReactor;
import org.jetlang.fibers.PoolFiberFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import akka.actor.ActorSystem;

import com.blockwithme.pingpong.latency.impl.DirectPinger;
import com.blockwithme.pingpong.latency.impl.DirectPonger;
import com.blockwithme.pingpong.latency.impl.JActor2LocalPinger;
import com.blockwithme.pingpong.latency.impl.JActor2LocalPonger;
import com.blockwithme.pingpong.latency.impl.JActor2NonBlockingPinger;
import com.blockwithme.pingpong.latency.impl.JActor2NonBlockingPonger;
import com.blockwithme.pingpong.latency.impl.JActorIteratorPinger;
import com.blockwithme.pingpong.latency.impl.JActorIteratorPonger;
import com.blockwithme.pingpong.latency.impl.JActorStackOverflowPinger;
import com.blockwithme.pingpong.latency.impl.JActorStackOverflowPonger;
import com.blockwithme.pingpong.latency.impl.JetlangPinger;
import com.blockwithme.pingpong.latency.impl.JetlangPonger;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

/**
 * Tests the number of seconds required to do sequential request/reply cycles,
 * for different possible Actor implementations.
 *
 * It is in essence a latency test, not a throughput test.
 *
 * It only tests the fastest implementations.
 */
@AxisRange(min = 0, max = 2)
@BenchmarkMethodChart(filePrefix = "Benchmark100MTest")
@SuppressWarnings("all")
public class Benchmark100MTest extends AbstractBenchmark {

    /** Allows disabling the tests easily. */
    private static final boolean RUN = true;

    /** Allows disabling the testDirect method easily. */
    private static final boolean testDirect = RUN;

    /** Allows disabling the testJActorIterator method easily. */
    private static final boolean testJActorIterator = RUN;

    /** Allows disabling the testJActorStackOverflow method easily. */
    private static final boolean testJActorStackOverflow = RUN;

    /** Allows disabling the testJetLang method easily. */
    private static final boolean testJetLang = RUN;

    /** Allows disabling the testJActor2NonBlockingSharedMailbox method easily. */
    private static final boolean testJActor2NonBlockingSharedMailbox = RUN;

    /** Allows disabling the testJActor2Local method easily. */
    private static final boolean testJActor2Local = RUN;

    /** Allows disabling the testJActor2SharedNonBlocking method easily. */
    private static final boolean testJActor2SharedNonBlocking = RUN;

    /** Number of threads per pool */
    protected static final int THREAD_POOL_SIZE = 8;

    /** 1 million */
    private static final int ONE_MILLION = 1000000;

    /** 10 millions */
    private static final int TEN_MILLION = 10000000;

    /** 100 millions */
    private static final int HUNDRED_MILLION = 100000000;

    /** Default number of messages */
    protected static final int DEFAULT_MESSAGES = TEN_MILLION;

    /**
     * How many messages to send per test?
     *
     * It must be big enough, that the direct impl takes a measurable amount
     * of time. This means that the slower Actor impl will take each several minutes to test.
     */
    protected int MESSAGES = DEFAULT_MESSAGES;

    /** The ExecutorService */
    protected ExecutorService executorService;

    /** Factory for JetLang fibers. */
    protected PoolFiberFactory fiberPool;

    /** The JActor MailboxFactory */
    protected MailboxFactory jaMailboxFactory;

    /** The Akka ActorSystem */
    protected ActorSystem system;

    /** The JActors Facility */
    protected Facility facility;

    /** Sets the benchmark properties, for stats and graphics generation. */
    static {
        System.setProperty("jub.consumers", "CONSOLE,H2");
        System.setProperty("jub.db.file", "benchmarks");
        System.setProperty("jub.charts.dir", "charts");
    }

    /** Setup all "services" for all test methods.
     * @throws Exception */
    @Before
    public void setup() throws Exception {
        if (testDirect || testJetLang || Benchmark10MTest.testExecutorService) {
            executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        }
        if (Benchmark10MTest.testAkkaBlocking
                || Benchmark10MTest.testAkkaNonBlocking) {
            final ConfigValue num = ConfigValueFactory
                    .fromAnyRef(THREAD_POOL_SIZE);
            final Config config = ConfigFactory
                    .load()
                    .withValue(
                            "akka.actor.default-dispatcher.fork-join-executor.parallelism-max",
                            num)
                    .withValue(
                            "akka.actor.default-dispatcher.thread-pool-executor.core-pool-size-max",
                            num)
                    .withValue(
                            "akka.actor.default-dispatcher.thread-pool-executor.max-pool-size-max",
                            num);
            system = ActorSystem.create("AkkaTest", config);
        }
        if (testJActorIterator || testJActorStackOverflow
                || Benchmark10MTest.testJActorBlocking) {
            jaMailboxFactory = JAMailboxFactory
                    .newMailboxFactory(THREAD_POOL_SIZE);
        }
        if (testJetLang) {
            fiberPool = new PoolFiberFactory(executorService);
        }
        if (testJActor2Local || testJActor2SharedNonBlocking
                || Benchmark10MTest.testJActor2NonBlocking
                || Benchmark10MTest.testJActor2Isolation) {
            facility = new Facility(THREAD_POOL_SIZE);
        }
    }

    /** Shuts down all "services" for all test methods.
     * @throws Exception */
    @After
    public void teardown() throws Exception {
        if (system != null) {
            system.shutdown();
            system = null;
        }
        if (jaMailboxFactory != null) {
            jaMailboxFactory.close();
            jaMailboxFactory = null;
        }
        if (fiberPool != null) {
            fiberPool.dispose();
            fiberPool = null;
        }
        if (executorService != null) {
            if (!executorService.isShutdown()) {
                executorService.shutdownNow();
            }
            executorService = null;
        }
        if (facility != null) {
            facility.close();
            facility = null;
        }
    }

    /** Baseline test: How fast would it go in a single thread? */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testDirect() throws Exception {
        if (testDirect) {
            final DirectPinger pinger = new DirectPinger();
            final DirectPonger ponger = new DirectPonger();
            final int result = pinger.hammer(ponger, MESSAGES);
            if (result != MESSAGES) {
                throw new IllegalStateException("Expected " + MESSAGES
                        + " but got " + result);
            }
        }
    }

    /** Test in JActors, using the Iterator helper class. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testJActorIterator() throws Exception {
        if (testJActorIterator) {
            final JActorIteratorPinger pinger = new JActorIteratorPinger(
                    jaMailboxFactory.createMailbox());
            final JActorIteratorPonger ponger = new JActorIteratorPonger(
                    pinger.getMailbox());
            final Integer result = pinger.hammer(ponger, MESSAGES);
            if (result.intValue() != MESSAGES) {
                throw new IllegalStateException("Expected " + MESSAGES
                        + " but got " + result);
            }
        }
    }

    /** Test non-blocking JActor simplistic impl, which causes occasional Stack-Overflow! */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testJActorStackOverflow() throws Exception {
        if (testJActorStackOverflow) {
            final JActorStackOverflowPinger pinger = new JActorStackOverflowPinger(
                    jaMailboxFactory.createMailbox());
            final JActorStackOverflowPonger ponger = new JActorStackOverflowPonger(
                    jaMailboxFactory.createMailbox());
            final Integer result = pinger.hammer(ponger, MESSAGES);
            if (result.intValue() != MESSAGES) {
                throw new IllegalStateException("Expected " + MESSAGES
                        + " but got " + result);
            }
        }
    }

    /** Test with JetLang. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testJetLang() throws Exception {
        if (testJetLang) {
            final JetlangPinger pinger = new JetlangPinger(fiberPool.create());
            final JetlangPonger ponger = new JetlangPonger(fiberPool.create());
            final Integer result = pinger.hammer(ponger, MESSAGES);
            if (result.intValue() != MESSAGES) {
                throw new IllegalStateException("Expected " + MESSAGES
                        + " but got " + result);
            }
        }
    }

    /** Test with JActor2/Local. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testJActor2Local() throws Exception {
        if (testJActor2Local) {
            final JActor2LocalPinger pinger = new JActor2LocalPinger(
                    new NonBlockingReactor(facility));
            final JActor2LocalPonger ponger = new JActor2LocalPonger(
                    pinger.getReactor());
            final int result = pinger.hammer(ponger, MESSAGES);
            if (result != MESSAGES) {
                throw new IllegalStateException("Expected " + MESSAGES
                        + " but got " + result);
            }
        }
    }

    /** Test with JActor2/async/non-blocking/shared reactor. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testJActor2SharedNonBlocking() throws Exception {
        if (testJActor2SharedNonBlocking) {
            final JActor2NonBlockingPinger pinger = new JActor2NonBlockingPinger(
                    new NonBlockingReactor(facility));
            final JActor2NonBlockingPonger ponger = new JActor2NonBlockingPonger(
                    pinger.getReactor());
            final int result = pinger.hammer(ponger, MESSAGES);
            if (result != MESSAGES) {
                throw new IllegalStateException("Expected " + MESSAGES
                        + " but got " + result);
            }
        }
    }
}