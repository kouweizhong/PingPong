package com.blockwithme.pingpong;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

/**
 * Receives Pings, and send Pongs back.
 */
public class JActorStackOverflowPonger extends JLPCActor {
    /** Some mutable data of Ponger, which must be access in a thread-safe way. */
    private int pings;

    /** A Ping request, targeted at Ponger. */
    private static class PingRequest2 extends Request<String, JActorStackOverflowPonger> {
        private final String from;

        public PingRequest2(final String _from) {
            from = _from;
        }

        /** Processes the ping(String) request, from within the Thread of the Ponger. */
        @SuppressWarnings("unchecked")
        @Override
        public void processRequest(final JLPCActor targetActor,
                @SuppressWarnings("rawtypes") final RP responseProcessor)
                throws Exception {
            final JActorStackOverflowPonger ponger = (JActorStackOverflowPonger) targetActor;
            responseProcessor.processResponse("Pong " + (ponger.pings++)
                    + " to " + from + "!");
        }

        @Override
        public boolean isTargetType(final Actor targetActor) {
            return JActorStackOverflowPonger.class.isInstance(targetActor);
        }
    }

    /** Creates a Ponger, with it's own mailbox.
     * @throws Exception */
    public JActorStackOverflowPonger(final Mailbox mbox) throws Exception {
        initialize(mbox);
    }

    /** Sends a ping(String) request to the Ponger. Blocks and returns response. */
    public void ping(final JActorStackOverflowPinger pinger, final RP<String> rp) throws Exception {
        new PingRequest2(pinger.toString()).send(pinger, this, rp);
    }
}
