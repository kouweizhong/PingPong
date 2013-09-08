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

import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor2.core.processing.NonBlockingMessageProcessor;
import org.agilewiki.jactor2.core.threading.ModuleContext;
import org.jetlang.fibers.PoolFiberFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import akka.actor.ActorSystem;

import com.blockwithme.pingpong.latency.impl.DirectPinger;
import com.blockwithme.pingpong.latency.impl.DirectPonger;
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
    private static final boolean RUN = false;

    /** Allows disabling the testDirect method easily. */
    private static final boolean testDirect = RUN;

    /** Allows disabling the testJActorIterator method easily. */
    private static final boolean testJActorIterator = RUN;

    /** Allows disabling the testJActorStackOverflow method easily. */
    private static final boolean testJActorStackOverflow = RUN;

    /** Allows disabling the testJetLang method easily. */
    private static final boolean testJetLang = RUN;

    /** Allows disabling the testJActor2NonBlockingSharedMailbox method easily. */
    private static final boolean testJActor2NonBlockingSharedMailbox = true;

    /** Default number of messages */
    protected static final int DEFAULT_MESSAGES = 10000000;//100000000;

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

    /** The JActor2 Default MailboxFactory */
    protected ModuleContext ja2ModuleContext;

    /** The Akka ActorSystem */
    protected ActorSystem system;

    /** Sets the benchmark properties, for stats and graphics generation. */
    static {
        System.setProperty("jub.consumers", "CONSOLE,H2");
        System.setProperty("jub.db.file", "benchmarks");
        System.setProperty("jub.charts.dir", "charts");
    }

    /** Setup all "services" for all test methods. */
    @Before
    public void setup() {
//        executorService = Executors.newFixedThreadPool(8);
//        system = ActorSystem.create("AkkaTest");
//        jaMailboxFactory = JAMailboxFactory.newMailboxFactory(8);
        ja2ModuleContext = new ModuleContext(2);
//        fiberPool = new PoolFiberFactory(executorService);
    }

    /** Shuts down all "services" for all test methods.
     * @throws Exception */
    @After
    public void teardown() throws Exception {
//        system.shutdown();
        system = null;
//        jaMailboxFactory.close();
        jaMailboxFactory = null;
        ja2ModuleContext.close();
        ja2ModuleContext = null;
//        fiberPool.dispose();
        fiberPool = null;
//        if (!executorService.isShutdown()) {
//            executorService.shutdownNow();
//        }
        executorService = null;
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

    /** Test with JActor2s, by having a reply generate the next request, to eliminate blocking. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testJActor2NonBlockingSharedMailbox() throws Exception {
        if (testJActor2NonBlockingSharedMailbox) {
            final JActor2NonBlockingPinger pinger = new JActor2NonBlockingPinger(
                    new NonBlockingMessageProcessor(ja2ModuleContext));
            final JActor2NonBlockingPonger ponger = new JActor2NonBlockingPonger(
                    pinger.getMailbox());
            final Integer result = pinger.hammer(ponger, MESSAGES);
            if (result.intValue() != MESSAGES) {
                throw new IllegalStateException("Expected " + MESSAGES
                        + " but got " + result);
            }
        }
    }
}