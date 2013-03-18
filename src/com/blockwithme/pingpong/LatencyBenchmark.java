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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;

/**
 * Tests the number of request/reply cycles per second,
 * for different possible Actor implementations.
 */
@AxisRange(min = 0, max = 3)
@BenchmarkMethodChart(filePrefix = "PingPongBenchmarks")
public class LatencyBenchmark extends AbstractBenchmark {

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
    private static final int MESSAGES = 1000000;

    /** The Akka ActorSystem */
    private ActorSystem system;

    /** The JActor MailboxFactory */
    private MailboxFactory jaMailboxFactory;

    /** The PActor MailboxFactory */
    private org.agilewiki.pactor.MailboxFactory paMailboxFactory;

    /** The ExecutorService */
    private ExecutorService executorService;

    /** Setup all "services" for all test methods. */
    @Before
    public void setup() {
        system = ActorSystem.create("AkkaTest");
        jaMailboxFactory = JAMailboxFactory.newMailboxFactory(2);
        paMailboxFactory = new org.agilewiki.pactor.MailboxFactory();
        executorService = Executors.newCachedThreadPool();
    }

    /** Shuts down all "services" for all test methods. */
    @After
    public void teardown() {
        system.shutdown();
        system = null;
        jaMailboxFactory.close();
        jaMailboxFactory = null;
        paMailboxFactory.shutdown();
        paMailboxFactory = null;
        executorService.shutdown();
        executorService = null;
    }

    /** Baseline test: How fast would it go in a single thread? */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testDirect() throws Exception {
        final DirectPinger pinger = new DirectPinger();
        final DirectPonger ponger = new DirectPonger();
        pinger.hammer(ponger, MESSAGES);
    }

    /** Tests using an ExecutorService. */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testExecutorService() throws Exception {
        final ExecutorServicePinger pinger = new ExecutorServicePinger(
                executorService);
        final ExecutorServicePonger ponger = new ExecutorServicePonger(
                executorService);
        try {
            try {
                pinger.hammer(ponger, MESSAGES);
            } finally {
                ponger.kill();
            }
        } finally {
            pinger.kill();
        }
    }

    /** Test using Threads and blocking queues. */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testThreadWithBlockingQueue() throws Exception {
        final ThreadWithBlockingQueuePinger pinger = new ThreadWithBlockingQueuePinger();
        final ThreadWithBlockingQueuePonger ponger = new ThreadWithBlockingQueuePonger();
        pinger.start();
        try {
            ponger.start();
            try {
                pinger.hammer(ponger, MESSAGES);
            } finally {
                ponger.kill();
            }
        } finally {
            pinger.kill();
        }
    }

    /** Test in Akka, using blocking Futures. */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testAkkaBlocking() throws Exception {
        final ActorRef pinger = system.actorOf(new Props(
                AkkaBlockingPinger.class), "blockingPinger");
        final ActorRef ponger = system.actorOf(new Props(
                AkkaBlockingPonger.class), "blockingPonger");

        final Timeout timeout = new Timeout(Duration.create(60, "seconds"));
        final Future<Object> future = Patterns.ask(pinger,
                AkkaBlockingPinger.hammer(ponger, MESSAGES), timeout);
        Await.result(future, timeout.duration());
    }

    /** Test in Akka, by having a reply generate the next request, to eliminate blocking. */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testAkkaNonBlocking() throws Exception {
        final ActorRef pinger = system.actorOf(new Props(
                AkkaNonBlockingPinger.class), "nonBlockingPinger");
        final ActorRef ponger = system.actorOf(new Props(
                AkkaNonBlockingPonger.class), "nonBlockingPonger");

        final Timeout timeout = new Timeout(Duration.create(60, "seconds"));
        final Future<Object> future = Patterns.ask(pinger,
                AkkaNonBlockingPinger.hammer(ponger, MESSAGES), timeout);
        Await.result(future, timeout.duration());
    }

    /** Test in JActors, using blocking Futures. */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testJActorBlocking() throws Exception {
        final JActorBlockingPinger pinger = new JActorBlockingPinger(
                jaMailboxFactory.createMailbox());
        final JActorBlockingPonger ponger = new JActorBlockingPonger(
                jaMailboxFactory.createMailbox());
        pinger.hammer(ponger, MESSAGES);
    }

    /** Test in JActors, using the Iterator helper class. */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testJActorIterator() throws Exception {
        final JActorIteratorPinger pinger = new JActorIteratorPinger(
                jaMailboxFactory.createMailbox());
        final JActorIteratorPonger ponger = new JActorIteratorPonger(
                pinger.getMailbox());
        pinger.hammer(ponger, MESSAGES);
    }

    /** Test non-blocking JActor simplistic impl, which causes occasional Stack-Overflow! */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testJActorStackOverflow() throws Exception {
        final JActorStackOverflowPinger pinger = new JActorStackOverflowPinger(
                jaMailboxFactory.createMailbox());
        final JActorStackOverflowPonger ponger = new JActorStackOverflowPonger(
                jaMailboxFactory.createMailbox());
        pinger.hammer(ponger, MESSAGES);
    }

    /** Test with PActors, using the pend() method to block. */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testPActorBlocking() throws Exception {
        final PActorBlockingPinger pinger = new PActorBlockingPinger(
                paMailboxFactory.createMailbox());
        final PActorBlockingPonger ponger = new PActorBlockingPonger(
                paMailboxFactory.createMailbox());
        pinger.hammer(ponger, MESSAGES);
    }

    /** Test with PActors, by having a reply generate the next request, to eliminate blocking. */
    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testPActorNonBlocking() throws Exception {
        final PActorNonBlockingPinger pinger = new PActorNonBlockingPinger(
                paMailboxFactory.createMailbox());
        final PActorNonBlockingPonger ponger = new PActorNonBlockingPonger(
                paMailboxFactory.createMailbox());
        pinger.hammer(ponger, MESSAGES);
    }
}
