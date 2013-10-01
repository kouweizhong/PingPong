package com.blockwithme.pingpong.latency.impl;

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
        public final int output;

        /** Creates the reply. */
        public PongReply(final int _output) {
            output = _output;
        }
    }

    /** A Ping request, targeted at Ponger. */
    private static class PingRequest {
        /** Some data used when processing the request. */
        private final int input;

        /** Creates the ping request. */
        public PingRequest(final int _input) {
            input = _input;
        }

        /** Processes the ping(String) request, from within the Thread of the Ponger. */
        public PongReply processRequest(
                final ThreadWithBlockingQueuePonger ponger) throws Exception {
            return new PongReply(input + 1);
        }
    }

    /** Sends a ping(String) request to the Ponger. */
    public void ping(final int input, final ActorThreadWithBlockingQueue sender)
            throws Exception {
        queueMessage(new PingRequest(input), sender);
    }
}
