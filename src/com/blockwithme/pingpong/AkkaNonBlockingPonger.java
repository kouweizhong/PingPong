package com.blockwithme.pingpong;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

/**
 * Receives Pings, and send Pongs back.
 */
public class AkkaNonBlockingPonger extends UntypedActor {
    /** Some mutable data of Ponger, which must be access in a thread-safe way. */
    private int pings;

    /** A Ping request, targeted at Ponger. */
    private static class PingRequest {
        /** Processes the ping(String) request, from within the Thread of the Ponger. */
        public void processRequest(final AkkaNonBlockingPonger ponger,
                final ActorRef sender) throws Exception {
            sender.tell(new PongReply("Pong " + (ponger.pings++) + " to "
                    + sender + "!"), ponger.getSelf());
        }
    }

    /** Pong's reply */
    public static class PongReply {
        public final String pong;

        public PongReply(final String _pong) {
            pong = _pong;
        }
    }

    /* (non-Javadoc)
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(final Object msg) throws Exception {
        if (msg instanceof PingRequest) {
            final PingRequest req = (PingRequest) msg;
            req.processRequest(this, getSender());
        } else {
            unhandled(msg);
        }
    }

    /** Sends a ping(String) request to the Ponger. Blocks and returns response. */
    public static void ping(final ActorRef pinger, final ActorRef ponger)
            throws Exception {
        ponger.tell(new PingRequest(), pinger);
    }
}
