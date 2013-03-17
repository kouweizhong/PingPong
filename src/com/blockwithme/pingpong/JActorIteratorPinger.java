package com.blockwithme.pingpong;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.JABidiIterator;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

/**
 * The Pinger's job is to hammer the Ponger with ping() request,
 * to count how many can be done in one second.
 */
public class JActorIteratorPinger extends JLPCActor {
    /** A Hammer request, targeted at Pinger. */
    private static class HammerRequest extends Request<String, JActorIteratorPinger> {
        /** The Ponger to hammer. */
        private final JActorIteratorPonger ponger;

        /** The number of exchanges to do. */
        private final int count;

        /** The number of pings. */
        private int done;

        /** The pinger */
        private JActorIteratorPinger pinger;

        /** Creates a hammer request, with the targeted Ponger. */
        public HammerRequest(final JActorIteratorPonger _ponger, final int _count) {
            ponger = _ponger;
            count = _count;
        }

        /** Process the hammer request. */
        @SuppressWarnings("unchecked")
        @Override
        public void processRequest(final JLPCActor targetActor,
                @SuppressWarnings("rawtypes") final RP _responseProcessor)
                throws Exception {
            pinger = (JActorIteratorPinger) targetActor;

            (new JABidiIterator() {
                @Override
                protected void sendRequest(
                        @SuppressWarnings("rawtypes") final RP responseProcessor)
                        throws Exception {
                    ponger.ping(pinger, responseProcessor);
                }

                @Override
                protected Object processResponse(final Object response)
                        throws Exception {
                    // response is ignored in this case, but we *could* have used it,
                    // which is the main thing.
                    done++;
                    if (done < count) {
                        return null;
                    } else {
                        return "done";
                    }
                }
            }).iterate(_responseProcessor);
        }

        @Override
        public boolean isTargetType(final Actor targetActor) {
            return JActorIteratorPinger.class.isInstance(targetActor);
        }
    }

    /** Creates a Pinger, with it's own mailbox and name.
     * @throws Exception */
    public JActorIteratorPinger(final Mailbox mbox) throws Exception {
        initialize(mbox);
    }

    /** Tells the pinger to hammer the Ponger. Describes the speed in the result. */
    public String hammer(final JActorIteratorPonger ponger, final int _count)
            throws Exception {
        final JAFuture future = new JAFuture();
        return new HammerRequest(ponger, _count).send(future, this);
    }
}
