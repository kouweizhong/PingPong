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
public class JActorStackOverflowPinger extends JLPCActor {
    /** A Hammer request, targeted at Pinger. */
    private static class HammerRequest2 extends Request<String, JActorStackOverflowPinger> {
        /** The Ponger to hammer. */
        private final JActorStackOverflowPonger ponger;

        /** The number of exchanges to do. */
        private final int count;

        /** The responseProcessor from the test, to call when done. */
        private RP<String> responseProcessor;

        /** The number of pings. */
        private int done;

        /** The pinger */
        private JActorStackOverflowPinger pinger;

        /** Creates a hammer request, with the targeted Ponger. */
        public HammerRequest2(final JActorStackOverflowPonger _ponger, final int _count) {
            ponger = _ponger;
            count = _count;
        }

        private void ping() throws Exception {
            ponger.ping(pinger, new RP<String>() {
                @Override
                public void processResponse(final String response)
                        throws Exception {
                    final long now = System.nanoTime();
                    done++;
                    if (done < count) {
                        // again ...
                        ping();
                    } else {
                        responseProcessor.processResponse("done");
                    }

                }
            });
        }

        /** Process the hammer request. */
        @SuppressWarnings("unchecked")
        @Override
        public void processRequest(final JLPCActor targetActor,
                @SuppressWarnings("rawtypes") final RP _responseProcessor)
                throws Exception {
            pinger = (JActorStackOverflowPinger) targetActor;
            responseProcessor = _responseProcessor;
            boolean again = true;
            while (again) {
                try {
                    ping();
                    again = false;
                } catch (final StackOverflowError e) {
                    // NOP
                }
            }
        }

        @Override
        public boolean isTargetType(final Actor targetActor) {
            return JActorStackOverflowPinger.class.isInstance(targetActor);
        }
    }

    /** Creates a Pinger, with it's own mailbox and name.
     * @throws Exception */
    public JActorStackOverflowPinger(final Mailbox mbox) throws Exception {
        initialize(mbox);
    }

    /** Tells the pinger to hammer the Ponger. Describes the speed in the result.
     * @param messages */
    public String hammer(final JActorStackOverflowPonger ponger, final int count) throws Exception {
        final JAFuture future = new JAFuture();
        return new HammerRequest2(ponger, count).send(future, this);
    }
}
