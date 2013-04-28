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
import org.agilewiki.pactor.impl.DefaultMailboxFactoryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blockwithme.pingpong.throughput.jactor.JActorEcho;
import com.blockwithme.pingpong.throughput.jactor.JActorParallel;
import com.blockwithme.pingpong.throughput.jactor.JActorRealRequest;
import com.blockwithme.pingpong.throughput.jactor.JActorSender;
import com.blockwithme.pingpong.throughput.pactor.PActorEcho;
import com.blockwithme.pingpong.throughput.pactor.PActorParallel;
import com.blockwithme.pingpong.throughput.pactor.PActorRealRequest;
import com.blockwithme.pingpong.throughput.pactor.PActorSender;
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

    /** Allows disabling the testPActorAsyncMailbox method easily. */
    private static final boolean testPActorAsyncMailbox = RUN;

    /** Allows disabling the testPActorSharedMailbox method easily. */
    private static final boolean testPActorSharedMailbox = RUN;

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

    /** The PActor Default MailboxFactory */
    protected DefaultMailboxFactoryImpl paMailboxFactory;

    /** Setup all "services" for all test methods. */
    @Before
    public void setup() {
        jaMailboxFactory = JAMailboxFactory.newMailboxFactory(THREADS);
        paMailboxFactory = new DefaultMailboxFactoryImpl();
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

    /** Throughput test in PActors, using async Mailboxes. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testPActorAsyncMailbox() throws Exception {
        if (testPActorAsyncMailbox) {
            final PActorSender[] senders = new PActorSender[PAIRS];
            int i = 0;
            while (i < PAIRS) {
                final org.agilewiki.pactor.api.Mailbox echoMailbox = paMailboxFactory
                        .createMailbox(BUFFERS + 10);
                final PActorEcho echo = new PActorEcho();
                echo.initialize(echoMailbox);
                final org.agilewiki.pactor.api.Mailbox senderMailbox = paMailboxFactory
                        .createMailbox(BUFFERS + 10);
                final PActorSender s = new PActorSender(echo, MESSAGES, BUFFERS);
                s.initialize(senderMailbox);
                senders[i] = s;
                i += 1;
            }
            final PActorParallel parallel = new PActorParallel();
            parallel.initialize(paMailboxFactory.createMailbox(true));
            parallel.actors = senders;
            PActorRealRequest.req.call(parallel);
        }
    }

    /** Throughput test in PActors, using shared Mailboxes. */
    @BenchmarkOptions(benchmarkRounds = 3, warmupRounds = 3)
    @Test
    public void testPActorSharedMailbox() throws Exception {
        if (testPActorSharedMailbox) {
            final PActorSender[] senders = new PActorSender[PAIRS];
            int i = 0;
            while (i < PAIRS) {
                final org.agilewiki.pactor.api.Mailbox echoMailbox = paMailboxFactory
                        .createMailbox(BUFFERS + 10);
                final PActorEcho echo = new PActorEcho();
                echo.initialize(echoMailbox);
                final PActorSender s = new PActorSender(echo, MESSAGES, BUFFERS);
                s.initialize(echoMailbox);
                senders[i] = s;
                i += 1;
            }
            final PActorParallel parallel = new PActorParallel();
            parallel.initialize(paMailboxFactory.createMailbox(true));
            parallel.actors = senders;
            PActorRealRequest.req.call(parallel);
        }
    }
}
