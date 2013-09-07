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

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor2.core.processing.IsolationMessageProcessor;
import org.agilewiki.jactor2.core.threading.ModuleContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blockwithme.pingpong.throughput.jactor.JActorEcho;
import com.blockwithme.pingpong.throughput.jactor.JActorParallel;
import com.blockwithme.pingpong.throughput.jactor.JActorRealRequest;
import com.blockwithme.pingpong.throughput.jactor.JActorSender;
import com.blockwithme.pingpong.throughput.jactor2.JActor2Echo;
import com.blockwithme.pingpong.throughput.jactor2.JActor2Parallel;
import com.blockwithme.pingpong.throughput.jactor2.JActor2RealRequest;
import com.blockwithme.pingpong.throughput.jactor2.JActor2Sender;
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
@BenchmarkMethodChart(filePrefix = "ThroughputBenchmark")
public class ThroughputBenchmarkTest extends AbstractBenchmark {

    /** Sets the benchmark properties, for stats and graphics generation. */
    static {
        System.setProperty("jub.consumers", "CONSOLE,H2");
        System.setProperty("jub.db.file", "benchmarks");
        System.setProperty("jub.charts.dir", "charts");
    }

    /** Allows disabling the tests easily. */
    private static final boolean RUN = true;

    /** Allows disabling the testJActorAsyncMailbox method easily. */
    private static final boolean testJActorAsyncMailbox = RUN;

    /** Allows disabling the testJActorSharedMailbox method easily. */
    private static final boolean testJActorSharedMailbox = RUN;

    /** Allows disabling the testJActor2AsyncMailbox method easily. */
    private static final boolean testJActor2AsyncMailbox = RUN;

    /** Allows disabling the testJActor2SharedMailbox method easily. */
    private static final boolean testJActor2SharedMailbox = RUN;

    /**
     * How many messages to send per actor pair?
     *
     * It must be big enough, that the direct impl takes a measurable amount
     * of time. This means that the slower Actor impl will take each several minutes to test.
     */
    protected static final int MESSAGES = 1000;

    /**
     * How many actors pair per test?
     *
     * It must be big enough, that the direct impl takes a measurable amount
     * of time. This means that the slower Actor impl will take each several minutes to test.
     */
    protected static final int PAIRS = 1000;

    /** How big should the message buffers be? */
    protected static final int BUFFERS = 1000;

    /** How many threads? */
    protected static final int THREADS = 8;

    /** The JActor MailboxFactory */
    protected MailboxFactory jaMailboxFactory;

    /** The JActor2 Default MailboxFactory */
    protected ModuleContext paMailboxFactory;

    /** Setup all "services" for all test methods. */
    @Before
    public void setup() {
        jaMailboxFactory = JAMailboxFactory.newMailboxFactory(THREADS);
        paMailboxFactory = new ModuleContext();
    }

    /** Shuts down all "services" for all test methods.
     * @throws Exception */
    @After
    public void teardown() throws Exception {
        jaMailboxFactory.close();
        jaMailboxFactory = null;
        paMailboxFactory.close();
        paMailboxFactory = null;
    }

    /** Throughput test in JActors, using async Mailboxes. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testJActorAsyncMailbox() throws Exception {
        if (testJActorAsyncMailbox) {
            final Actor[] senders = new Actor[PAIRS];
            int i = 0;
            while (i < PAIRS) {
                final Mailbox echoMailbox = jaMailboxFactory
                        .createAsyncMailbox();
                final JActorEcho echo = new JActorEcho();
                echo.initialize(echoMailbox);
                echo.setInitialBufferCapacity(BUFFERS + 10);
                final Mailbox senderMailbox = jaMailboxFactory
                        .createAsyncMailbox();
                final JActorSender s = new JActorSender(echo, MESSAGES, BUFFERS);
                s.initialize(senderMailbox);
                senders[i] = s;
                senders[i].setInitialBufferCapacity(BUFFERS + 10);
                i += 1;
            }
            final JActorParallel parallel = new JActorParallel();
            parallel.initialize(jaMailboxFactory.createAsyncMailbox());
            parallel.actors = senders;
            final JAFuture future = new JAFuture();
            JActorRealRequest.req.send(future, parallel);
        }
    }

    /** Throughput test in JActors, using shared Mailboxes. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testJActorSharedMailbox() throws Exception {
        if (testJActorSharedMailbox) {
            final Actor[] senders = new Actor[PAIRS];
            int i = 0;
            while (i < PAIRS) {
                final Mailbox echoMailbox = jaMailboxFactory
                        .createAsyncMailbox();
                final JActorEcho echo = new JActorEcho();
                echo.initialize(echoMailbox);
                echo.setInitialBufferCapacity(BUFFERS + 10);
                final JActorSender s = new JActorSender(echo, MESSAGES, BUFFERS);
                s.initialize(echoMailbox);
                senders[i] = s;
                senders[i].setInitialBufferCapacity(BUFFERS + 10);
                i += 1;
            }
            final JActorParallel parallel = new JActorParallel();
            parallel.initialize(jaMailboxFactory.createMailbox());
            parallel.actors = senders;
            final JAFuture future = new JAFuture();
            JActorRealRequest.req.send(future, parallel);
        }
    }

    /** Throughput test in JActor2s, using async Mailboxes. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testJActor2AsyncMailbox() throws Exception {
        if (testJActor2AsyncMailbox) {
            final JActor2Sender[] senders = new JActor2Sender[PAIRS];
            int i = 0;
            while (i < PAIRS) {
                final org.agilewiki.jactor2.core.processing.MessageProcessor echoMailbox = new IsolationMessageProcessor(
                        paMailboxFactory
                /*BUFFERS + 10*/);
                final JActor2Echo echo = new JActor2Echo();
                echo.initialize(echoMailbox);
                final org.agilewiki.jactor2.core.processing.MessageProcessor senderMailbox = new IsolationMessageProcessor(
                        paMailboxFactory/*BUFFERS + 10*/);
                final JActor2Sender s = new JActor2Sender(echo, MESSAGES,
                        BUFFERS);
                s.initialize(senderMailbox);
                senders[i] = s;
                i += 1;
            }
            final JActor2Parallel parallel = new JActor2Parallel();
            parallel.initialize(new IsolationMessageProcessor(paMailboxFactory));
            parallel.actors = senders;
            new JActor2RealRequest(new IsolationMessageProcessor(
                    paMailboxFactory), parallel).call();
        }
    }

    /** Throughput test in JActor2s, using shared Mailboxes. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testJActor2SharedMailbox() throws Exception {
        if (testJActor2SharedMailbox) {
            final JActor2Sender[] senders = new JActor2Sender[PAIRS];
            int i = 0;
            while (i < PAIRS) {
                final org.agilewiki.jactor2.core.processing.MessageProcessor echoMailbox = new IsolationMessageProcessor(
                        paMailboxFactory/*BUFFERS + 10*/);
                final JActor2Echo echo = new JActor2Echo();
                echo.initialize(echoMailbox);
                final JActor2Sender s = new JActor2Sender(echo, MESSAGES,
                        BUFFERS);
                s.initialize(echoMailbox);
                senders[i] = s;
                i += 1;
            }
            final JActor2Parallel parallel = new JActor2Parallel();
            parallel.initialize(new IsolationMessageProcessor(paMailboxFactory));
            parallel.actors = senders;
            new JActor2RealRequest(new IsolationMessageProcessor(
                    paMailboxFactory), parallel).call();
        }
    }
}
