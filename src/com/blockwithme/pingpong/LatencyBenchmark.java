package com.blockwithme.pingpong;

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

/**
 * Tests the number of request/reply cycles per second.
 */
public class LatencyBenchmark extends AbstractBenchmark {
    /** How many messages? */
    private static final int MESSAGES = 1000000;

    /** The MailboxFactory */
    private ActorSystem system;

    /** The JActor MailboxFactory */
    private MailboxFactory jaMailboxFactory;

    /** The MailboxFactory */
    private org.agilewiki.pactor.MailboxFactory paMailboxFactory;

    @Before
    public void setup() {
        system = ActorSystem.create("AkkaTest");
        jaMailboxFactory = JAMailboxFactory.newMailboxFactory(2);
        paMailboxFactory = new org.agilewiki.pactor.MailboxFactory();
    }

    @After
    public void teardown() {
        system.shutdown();
        system = null;
        jaMailboxFactory.close();
        jaMailboxFactory = null;
        paMailboxFactory.shutdown();
        paMailboxFactory = null;
    }

    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
    @Test
    public void testDirect() throws Exception {
        final DirectPinger pinger = new DirectPinger();
        final DirectPonger ponger = new DirectPonger();
        pinger.hammer(ponger, MESSAGES);
    }

//
//    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
//    @Test
//    public void testAkkaBlocking() throws Exception {
//        final ActorRef pinger = system.actorOf(new Props(
//                AkkaBlockingPinger.class), "blockingPinger");
//        final ActorRef ponger = system.actorOf(new Props(
//                AkkaBlockingPonger.class), "blockingPonger");
//
//        final Timeout timeout = new Timeout(Duration.create(60, "seconds"));
//        final Future<Object> future = Patterns.ask(pinger,
//                AkkaBlockingPinger.hammer(ponger, MESSAGES), timeout);
//        Await.result(future, timeout.duration());
//    }

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

//    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
//    @Test
//    public void testJActorBlocking() throws Exception {
//        final JActorBlockingPinger pinger = new JActorBlockingPinger(
//                jaMailboxFactory.createMailbox());
//        final JActorBlockingPonger ponger = new JActorBlockingPonger(
//                jaMailboxFactory.createMailbox());
//        pinger.hammer(ponger, MESSAGES);
//    }
//
//    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
//    @Test
//    public void testJActorIterator() throws Exception {
//        final JActorIteratorPinger pinger = new JActorIteratorPinger(
//                jaMailboxFactory.createMailbox());
//        final JActorIteratorPonger ponger = new JActorIteratorPonger(
//                pinger.getMailbox());
//        pinger.hammer(ponger, MESSAGES);
//    }
//
//    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
//    @Test
//    public void testJActorStackOverflow() throws Exception {
//        final JActorStackOverflowPinger pinger = new JActorStackOverflowPinger(
//                jaMailboxFactory.createMailbox());
//        final JActorStackOverflowPonger ponger = new JActorStackOverflowPonger(
//                jaMailboxFactory.createMailbox());
//        pinger.hammer(ponger, MESSAGES);
//    }
//
//    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
//    @Test
//    public void testPActorBlocking() throws Exception {
//        final PActorBlockingPinger pinger = new PActorBlockingPinger(
//                paMailboxFactory.createMailbox());
//        final PActorBlockingPonger ponger = new PActorBlockingPonger(
//                paMailboxFactory.createMailbox());
//        pinger.hammer(ponger, MESSAGES);
//    }
//
//    @BenchmarkOptions(benchmarkRounds = 10, warmupRounds = 3)
//    @Test
//    public void testPActorNonBlocking() throws Exception {
//        final PActorNonBlockingPinger pinger = new PActorNonBlockingPinger(
//                paMailboxFactory.createMailbox());
//        final PActorNonBlockingPonger ponger = new PActorNonBlockingPonger(
//                paMailboxFactory.createMailbox());
//        pinger.hammer(ponger, MESSAGES);
//    }
}
