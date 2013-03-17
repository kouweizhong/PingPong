package com.blockwithme.pingpong;

import org.agilewiki.pactor.Mailbox;
import org.agilewiki.pactor.Request;
import org.agilewiki.pactor.ResponseProcessor;

/**
 * The Pinger's job is to hammer the Ponger with ping() request,
 * to count how many can be done in one second.
 */
public class PActorNonBlockingPinger {

    /** The Pinger's mailbox. */
    private final Mailbox mailbox;

    /** A Hammer request, targeted at Pinger. */
    private class HammerRequest2 extends Request<String> {
        /** The Ponger to hammer. */
        private final PActorNonBlockingPonger ponger;

        /** The number of exchanges to do. */
        private final int count;

        /** The number of pings sent. */
        private int done;

        /** ResponseProcessor for benchmark results */
        private ResponseProcessor<String> responseProcessor;

        /** Creates a hammer request, with the targeted Ponger. */
        public HammerRequest2(final Mailbox mbox, final PActorNonBlockingPonger _ponger,
                final int _count) {
            super(mbox);
            ponger = _ponger;
            count = _count;
        }

        private void ping() throws Exception {
            done++;
            if (done < count) {
                ponger.ping(PActorNonBlockingPinger.this, new ResponseProcessor<String>() {
                    @Override
                    public void processResponse(final String response)
                            throws Exception {
                        ping();
                    }
                });
            } else {
                responseProcessor.processResponse("done");
            }
        }

        /** Process the hammer request. */
        @Override
        public void processRequest(
                final ResponseProcessor<String> _responseProcessor)
                throws Exception {
            done = 0;
            responseProcessor = _responseProcessor;
            ping();
        }
    }

    /** Creates a Pinger, with it's own mailbox and name. */
    public PActorNonBlockingPinger(final Mailbox mbox) {
        mailbox = mbox;
    }

    /** Tells the pinger to hammer the Ponger. Describes the speed in the result. */
    public String hammer(final PActorNonBlockingPonger ponger, final int count) throws Exception {
        return new HammerRequest2(getMailbox(), ponger, count).pend();
    }

    /**
     * @return the mailbox
     */
    public Mailbox getMailbox() {
        return mailbox;
    }
}
