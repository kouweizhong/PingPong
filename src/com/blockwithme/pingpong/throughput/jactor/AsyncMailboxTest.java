package com.blockwithme.pingpong.throughput.jactor;

import junit.framework.TestCase;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;
import org.agilewiki.jactor.lpc.JLPCActor;

/**
 * Test code.
 */
public class AsyncMailboxTest extends TestCase {
    public void testTiming() {
        final int count = 2;
        final int buffer = 3;
        final int actors = 1;
        final int threads = 1;

        //System.out.println("####################################################");
        //int count = 500;
        //int b = 1000;
        //int p = 1000;
        //int t = 8;

        //burst size of 100
        //1000 parallel runs of 1,000,000 messages each.
        //1,000,000,000 messages sent with 8 threads.
        //msgs per sec = 81,083,272

        final MailboxFactory mailboxFactory = JAMailboxFactory
                .newMailboxFactory(threads);
        try {
            final Actor[] senders = new Actor[actors];
            int i = 0;
            while (i < actors) {
                final Mailbox echoMailbox = mailboxFactory.createAsyncMailbox();
                final Echo echo = new Echo();
                echo.initialize(echoMailbox);
                echo.setInitialBufferCapacity(buffer + 10);
                final Mailbox senderMailbox = mailboxFactory
                        .createAsyncMailbox();
                final JLPCActor s = (buffer == 1) ? new Sender1(echo, count,
                        buffer) : new Sender(echo, count, buffer);
                s.initialize(senderMailbox);
                senders[i] = s;
                senders[i].setInitialBufferCapacity(buffer + 10);
                i += 1;
            }
            final JAParallel parallel = new JAParallel();
            parallel.initialize(mailboxFactory.createAsyncMailbox());
            parallel.actors = senders;
            final JAFuture future = new JAFuture();
            RealRequest.req.send(future, parallel);
            RealRequest.req.send(future, parallel);
            final long t0 = System.currentTimeMillis();
            RealRequest.req.send(future, parallel);
            final long t1 = System.currentTimeMillis();
            SimpleRequest.req.send(future, parallel);
            final long t2 = System.currentTimeMillis();
            System.out.println("null test time " + (t2 - t1));
            System.out.println("" + actors + " parallel runs of "
                    + (2L * count * buffer) + " messages each.");
            System.out.println("" + (2L * count * buffer * actors)
                    + " messages sent with " + threads + " threads.");
            if (t1 != t0 && t1 - t0 - t2 + t1 > 0) {
                System.out.println("msgs per sec = "
                        + ((2L * count * buffer * actors) * 1000L / (t1 - t0)));
                System.out.println("adjusted msgs per sec = "
                        + ((2L * count * buffer * actors) * 1000L / (t1 - t0
                                - t2 + t1)));
            }
        } catch (final Throwable e) {
            e.printStackTrace();
        } finally {
            mailboxFactory.close();
        }
    }
}
