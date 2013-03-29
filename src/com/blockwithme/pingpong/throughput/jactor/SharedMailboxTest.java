package com.blockwithme.pingpong.throughput.jactor;

import junit.framework.TestCase;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.JAMailboxFactory;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.MailboxFactory;

/**
 * Test code.
 */
public class SharedMailboxTest extends TestCase {

    /** Allows disabling the tests eaqsily. */
    private static final boolean RUN = false;

    public void testTiming() {
        if (RUN) {
            final int c = 1;
            final int b = 1;
            final int p = 1;
            final int t = 1;

            //System.out.println("####################################################");
            //int c = 1000;
            //int b = 1000;
            //int p = 1000;
            //int t = 8;

            //burst size of 100
            //1000 parallel runs of 2,000,000 messages each.
            //2,000,000,000 messages sent with 8 threads.
            //msgs per sec = 222,861,399

            final MailboxFactory mailboxFactory = JAMailboxFactory
                    .newMailboxFactory(t);
            try {
                final Actor[] senders = new Actor[p];
                int i = 0;
                while (i < p) {
                    final Mailbox sharedMailbox = mailboxFactory
                            .createAsyncMailbox();
                    final Echo echo = new Echo();
                    echo.initialize(sharedMailbox);
                    echo.setInitialBufferCapacity(b + 10);
                    if (b == 1) {
                        final Sender1 s = new Sender1(echo, c, b);
                        s.initialize(sharedMailbox);
                        senders[i] = s;
                    } else {
                        final Sender s = new Sender(echo, c, b);
                        s.initialize(sharedMailbox);
                        senders[i] = s;
                    }
                    senders[i].setInitialBufferCapacity(b + 10);
                    i += 1;
                }
                final JAParallel parallel = new JAParallel();
                parallel.initialize(mailboxFactory.createMailbox());
                parallel.actors = senders;
                final JAFuture future = new JAFuture();
                RealRequest.req.send(future, parallel);
                System.out.println("!");
                final long t0 = System.currentTimeMillis();
                RealRequest.req.send(future, parallel);
                final long t1 = System.currentTimeMillis();
                //SimpleRequest.req.send(future, parallel);
                //long t2 = System.currentTimeMillis();
                //System.out.println("null test time " + (t2 - t1));
                System.out.println("" + p + " parallel runs of " + (2L * c * b)
                        + " messages each.");
                System.out.println("" + (2L * c * b * p)
                        + " messages sent with " + t + " threads.");
                //if (t1 != t0 && t1 - t0 - t2 + t1 > 0) {
                if (t1 - t0 > 0) {
                    System.out.println("msgs per sec = "
                            + ((2L * c * b * p) * 1000L / (t1 - t0)));
                    //    System.out.println("adjusted msgs per sec = " + ((2L * c * b * p) * 1000L / (t1 - t0 - t2 + t1)));
                }
            } catch (final Throwable e) {
                e.printStackTrace();
            } finally {
                mailboxFactory.close();
            }
        }
    }
}
