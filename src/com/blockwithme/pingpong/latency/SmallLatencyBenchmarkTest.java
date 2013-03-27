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

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;
import kilim.Pausable;
import kilim.Task;

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
import com.blockwithme.pingpong.latency.impl.KilimPinger;
import com.blockwithme.pingpong.latency.impl.KilimPonger;
import com.blockwithme.pingpong.latency.impl.PActorNonBlockingPinger;
import com.blockwithme.pingpong.latency.impl.PActorNonBlockingPonger;
import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

// TODO: Auto-generated Javadoc
/**
 * Tests the number of seconds required to do sequential request/reply cycles,
 * for different possible Actor implementations.
 *
 * It is in essence a latency test, not a throughput test.
 *
 * It only tests the fastest implementations.
 */
@AxisRange(min = 0, max = 2)
@BenchmarkMethodChart(filePrefix = "PingPongFastBenchmarks")
@SuppressWarnings("all")
public class SmallLatencyBenchmarkTest extends AbstractBenchmark {

    /**
     * The Class DirectTask.
     */
    public class DirectTask extends kilim.Task {
        SmallLatencyBenchmarkTest target;

        /**
         * Instantiates a new direct task.
         *
         * @param target the target
         */
        public DirectTask(final SmallLatencyBenchmarkTest target) {
            this.target = target;
        }

        /** {@inheritDoc} */
        @Override
        public void execute() throws Pausable, Exception {
            target.kilim();
        }
    }

    /**
     * The Class ReflectedTask.
     */
    public class ReflectedTask extends Task {

        Method method;

        Object target;

        /**
         * Instantiates a new reflected task.
         *
         * @param target the target
         * @param method the method
         */
        public ReflectedTask(final Object target, final Method method) {
            this.method = method;
            this.target = target;
        }

        /** {@inheritDoc} */
        @Override
        public void execute() throws Pausable, Exception {
            ReflectedTask.invoke(method, target);
        }
    }

    /**
     * How many messages to send per test?
     *
     * It must be big enough, that the direct impl takes a measurable amount
     * of time. This means that the slower Actor impl will take each several minutes to test.
     */
    protected static final int MESSAGES = 1000000;

    /** The direct task. */
    private DirectTask directTask;

    /** The reflected task. */
    private ReflectedTask reflectedTask;

    /**
     * Test value that is increased each time the {@link #methodCall()}
    performs one step.
     */
    int step;

    /** The ExecutorService */
    protected ExecutorService executorService;

    /** Factory for JetLang fibers. */
    protected PoolFiberFactory fiberPool;

    /** The JActor MailboxFactory */
    protected MailboxFactory jaMailboxFactory;

    /** The PActor Default MailboxFactory */
    protected DefaultMailboxFactoryImpl paMailboxFactory;

    /** The Akka ActorSystem */
    protected ActorSystem system;

    /** Sets the benchmark properties, for stats and graphics generation. */
    static {
        System.setProperty("jub.consumers", "CONSOLE,H2");
        System.setProperty("jub.db.file", "benchmarks");
        System.setProperty("jub.charts.dir", "charts");
    }

    /**
     * Kilim.
     *
     * @throws Pausable the pausable
     * @throws Exception the exception
     */
    public void kilim() throws Pausable, Exception {
        final kilim.Mailbox<Object> pingerMB = new kilim.Mailbox<Object>();
        final kilim.Mailbox<Object> pongerMB = new kilim.Mailbox<Object>();
        final KilimPonger ponger = new KilimPonger(pingerMB, pongerMB);
        final KilimPinger pinger = new KilimPinger(pingerMB, ponger);
        final Integer result = pinger.hammer(MESSAGES);
        if (result.intValue() != MESSAGES) {
            throw new IllegalStateException("Expected " + MESSAGES
                    + " but got " + result);
        }
    }

    /** Setup all "services" for all test methods. */
    @Before
    public void setup() {
        executorService = Executors.newFixedThreadPool(8);
        system = ActorSystem.create("AkkaTest");
        jaMailboxFactory = JAMailboxFactory.newMailboxFactory(8);
        paMailboxFactory = new DefaultMailboxFactoryImpl(executorService, false);
        fiberPool = new PoolFiberFactory(executorService);

        try {
            directTask = new DirectTask(this);
            final Method kilimMethod = SmallLatencyBenchmarkTest.class
                    .getMethod("kilim");
            reflectedTask = new ReflectedTask(this, kilimMethod);
        } catch (final NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testKilimDirectTask() {
        try {
            directTask._runExecute(null);
        } catch (final Throwable e) {
            // TODO Auto-generated catch block
            Assert.fail(e.getMessage());
        }
    }

    /** Test with Kilim. */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testKilimReflectedTask() {
        try {
            reflectedTask._runExecute(null);
        } catch (final Throwable e) {
            // TODO Auto-generated catch block
            Assert.fail(e.getMessage());
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
}