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
package com.blockwithme.pingpong.latency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.pamailbox.DefaultMailboxFactoryImpl;
import org.jetlang.fibers.PoolFiberFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import akka.actor.ActorSystem;

import com.blockwithme.pingpong.latency.impl.DirectPinger;
import com.blockwithme.pingpong.latency.impl.DirectPonger;
import com.blockwithme.pingpong.latency.impl.JActorIteratorPinger;
import com.blockwithme.pingpong.latency.impl.JActorIteratorPonger;
import com.blockwithme.pingpong.latency.impl.JActorStackOverflowPinger;
import com.blockwithme.pingpong.latency.impl.JActorStackOverflowPonger;
import com.blockwithme.pingpong.latency.impl.JetlangPinger;
import com.blockwithme.pingpong.latency.impl.JetlangPonger;
import com.blockwithme.pingpong.latency.impl.PActorNonBlockingPinger;
import com.blockwithme.pingpong.latency.impl.PActorNonBlockingPonger;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

/**
 * Tests the number of seconds required to do sequential request/reply cycles,
 * for different possible Actor implementations.
 *
 * It is in essence a latency test, not a throughput test.
 *
 * It only tests the fastest implementations.
 */
@AxisRange(min = 0, max = 3)
@BenchmarkMethodChart(filePrefix = "PingPongFastBenchmarks")
public class SmallLatencyBenchmark extends AbstractBenchmark {

    /** Sets the benchmark properties, for stats and graphics generation. */
    static {
        System.setProperty("jub.consumers", "CONSOLE,H2");
        System.setProperty("jub.db.file", "benchmarks");
        System.setProperty("jub.charts.dir", "charts");
    }

    /**
     * How many messages to send per test?
     *
     * It must be big enough, that the direct impl takes a measurable amount
     * of time. This means that the slower Actor impl will take each several minutes to test.
     */
    protected static final int MESSAGES = 1000;

    /** The Akka ActorSystem */
    protected ActorSystem system;

    /** The JActor MailboxFactory */
    protected MailboxFactory jaMailboxFactory;

    /** The PActor Default MailboxFactory */
    protected DefaultMailboxFactoryImpl paMailboxFactory;

    /** The ExecutorService */
    protected ExecutorService executorService;

    /** Factory for JetLang fibers. */
    protected PoolFiberFactory fiberPool;

    /** Setup all "services" for all test methods. */
    @Before
    public void setup() {
        executorService = Executors.newCachedThreadPool();
        system = ActorSystem.create("AkkaTest");
        jaMailboxFactory = JAMailboxFactory.newMailboxFactory(2);
        paMailboxFactory = new DefaultMailboxFactoryImpl(executorService, false);
        fiberPool = new PoolFiberFactory(executorService);
    }

    /** Shuts down all "services" for all test methods.
     * @throws Exception */
    @After
    public void teardown() throws Exception {
        system.shutdown();
        system = null;
        jaMailboxFactory.close();
        jaMailboxFactory = null;
        paMailboxFactory.close();
        paMailboxFactory = null;
        fiberPool.dispose();
        fiberPool = null;
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        executorService = null;
    }

    /** Baseline test: How fast would it go in a single thread? */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testDirect() throws Exception {
        final DirectPinger pinger = new DirectPinger();
        final DirectPonger ponger = new DirectPonger();
        final int result = pinger.hammer(ponger, MESSAGES);
        if (result != MESSAGES) {
            throw new IllegalStateException("Expected " + MESSAGES
                    + " but got " + result);
        }
    }

    /** Test in JActors, using the Iterator helper class. */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testJActorIterator() throws Exception {
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

    /** Test with PActors, by having a reply generate the next request, to eliminate blocking. */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testPActorNonBlocking() throws Exception {
        final PActorNonBlockingPinger pinger = new PActorNonBlockingPinger(
                paMailboxFactory.createMailbox());
        final PActorNonBlockingPonger ponger = new PActorNonBlockingPonger(
                pinger.getMailbox());
        final Integer result = pinger.hammer(ponger, MESSAGES);
        if (result.intValue() != MESSAGES) {
            throw new IllegalStateException("Expected " + MESSAGES
                    + " but got " + result);
        }
    }

    /** Test non-blocking JActor simplistic impl, which causes occasional Stack-Overflow! */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testJActorStackOverflow() throws Exception {
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

    /** Test with JetLang. */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testJetLang() throws Exception {
        final JetlangPinger pinger = new JetlangPinger(fiberPool.create());
        final JetlangPonger ponger = new JetlangPonger(fiberPool.create());
        final Integer result = pinger.hammer(ponger, MESSAGES);
        if (result.intValue() != MESSAGES) {
            throw new IllegalStateException("Expected " + MESSAGES
                    + " but got " + result);
        }
    }

    /** Test with Kilim. */
/*
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testKilim() throws Exception {
        final kilim.Mailbox<Object> pingerMB = new kilim.Mailbox<Object>();
        final kilim.Mailbox<Object> pongerMB = new kilim.Mailbox<Object>();
        final KilimPonger ponger = new KilimPonger(pingerMB, pongerMB);
        final KilimPinger pinger = new KilimPinger(pingerMB, ponger);
        pingerMB.start();
        pongerMB.start();
        final Integer result = pinger.hammer(MESSAGES);
        if (result.intValue() != MESSAGES) {
            throw new IllegalStateException("Expected " + MESSAGES
                    + " but got " + result);
        }
    }
*/
}
