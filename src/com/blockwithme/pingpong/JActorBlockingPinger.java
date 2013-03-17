package com.blockwithme.pingpong;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

/**
 * The Pinger's job is to hammer the Ponger with ping() request,
 * to count how many can be done in one second.
 */
public class JActorBlockingPinger extends JLPCActor {
    /** A Hammer request, targeted at Pinger. */
    private static class HammerRequest extends Request<String, JActorBlockingPinger> {
        /** The Ponger to hammer. */
        private final JActorBlockingPonger ponger;

        /** The number of exchanges to do. */
        private final int count;

        /** Creates a hammer request, with the targeted Ponger. */
        public HammerRequest(final JActorBlockingPonger _ponger, final int _count) {
            ponger = _ponger;
            count = _count;
        }

        /** Process the hammer request. */
        @SuppressWarnings("unchecked")
        @Override
        public void processRequest(final JLPCActor targetActor,
                @SuppressWarnings("rawtypes") final RP responseProcessor)
                throws Exception {
            final JActorBlockingPinger pinger = (JActorBlockingPinger) targetActor;
            final String name = pinger.toString();
            int done = 0;
            while (done < count) {
                ponger.ping(name);
                done++;
            }
            responseProcessor.processResponse("done");
        }

        @Override
        public boolean isTargetType(final Actor targetActor) {
            return JActorBlockingPinger.class.isInstance(targetActor);
        }
    }

    /** Creates a Pinger, with it's own mailbox and name.
     * @throws Exception */
    public JActorBlockingPinger(final Mailbox mbox) throws Exception {
        initialize(mbox);
    }

    /** Tells the pinger to hammer the Ponger. Describes the speed in the result. */
    public String hammer(final JActorBlockingPonger ponger, final int _count)
            throws Exception {
        final JAFuture future = new JAFuture();
        return new HammerRequest(ponger, _count).send(future, this);
    }
}
