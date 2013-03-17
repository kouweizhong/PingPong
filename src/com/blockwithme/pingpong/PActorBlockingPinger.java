package com.blockwithme.pingpong;

import org.agilewiki.pactor.Mailbox;
import org.agilewiki.pactor.Request;
import org.agilewiki.pactor.ResponseProcessor;

/**
 * The Pinger's job is to hammer the Ponger with ping() request,
 * to count how many can be done in one second.
 */
public class PActorBlockingPinger {
    /** The Pinger's mailbox. */
    private final Mailbox mailbox;

    /** A Hammer request, targeted at Pinger. */
    private class HammerRequest extends Request<String> {
        /** The Ponger to hammer. */
        private final PActorBlockingPonger ponger;

        /** The number of exchanges to do. */
        private final int count;

        /** Creates a hammer request, with the targeted Ponger. */
        public HammerRequest(final Mailbox mbox, final PActorBlockingPonger _ponger,
                final int _count) {
            super(mbox);
            ponger = _ponger;
            count = _count;
        }

        /** Process the hammer request. */
        @Override
        public void processRequest(
                final ResponseProcessor<String> responseProcessor)
                throws Exception {
            final String name = PActorBlockingPinger.this.toString();
            int done = 0;
            while (done < count) {
                ponger.ping(name);
                done++;
            }
            responseProcessor.processResponse("done");
        }
    }

    /** Creates a Pinger, with it's own mailbox and name. */
    public PActorBlockingPinger(final Mailbox mbox) {
        mailbox = mbox;
    }

    /** Tells the pinger to hammer the Ponger. Describes the speed in the result. */
    public String hammer(final PActorBlockingPonger ponger, final int count) throws Exception {
        return new HammerRequest(mailbox, ponger, count).pend();
    }
}
