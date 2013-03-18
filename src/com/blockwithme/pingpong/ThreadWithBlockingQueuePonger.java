package com.blockwithme.pingpong;

/**
 * Receives Pings, and send Pongs back.
 * Implemented using independent threads with blocking queues.
 */
public class ThreadWithBlockingQueuePonger extends ActorThreadWithBlockingQueue {
    /** Some mutable data of Ponger, which must be access in a thread-safe way. */
    private int pings;

    /** Process all incoming messages, including replies. */
    @Override
    protected void processMessage(final Object msg,
            final ActorThreadWithBlockingQueue sender) throws Exception {
        if (msg instanceof PingRequest) {
            final PingRequest req = (PingRequest) msg;
            final Object response = req.processRequest(this);
            sender.queueMessage(response, this);
        } else {
            unhandled(msg);
        }
    }

    /** Pong's reply */
    public static class PongReply {
        /** The reply. */
        public final String pong;

        /** Creates the reply. */
        public PongReply(final String _pong) {
            pong = _pong;
        }
    }

    /** A Ping request, targeted at Ponger. */
    private static class PingRequest {
        /** Some data used when processing the request. */
        private final String from;

        /** Creates the ping request. */
        public PingRequest(final String _from) {
            from = _from;
        }

        /** Processes the ping(String) request, from within the Thread of the Ponger. */
        public PongReply processRequest(
                final ThreadWithBlockingQueuePonger ponger) throws Exception {
            return new PongReply("Pong " + (ponger.pings++) + " to " + from
                    + "!");
        }
    }

    /** Sends a ping(String) request to the Ponger. */
    public void ping(final String from,
            final ActorThreadWithBlockingQueue sender) throws Exception {
        queueMessage(new PingRequest(from), sender);
    }
}
