package com.blockwithme.pingpong;


/**
 * Receives Pings, and send Pongs back.
 */
public class DirectPonger {
    /** Some mutable data of Ponger, which must be access in a thread-safe way. */
    private int pings;

    /** A Ping request, targeted at Ponger. */
    private static class PingRequest {
        private final String from;

        public PingRequest(final String _from) {
            from = _from;
        }

        /** Processes the ping(String) request, from within the Thread of the Ponger. */
        public String processRequest(final DirectPonger ponger)
                throws Exception {
            return "Pong " + (ponger.pings++) + " to " + from + "!";
        }
    }

    /** Sends a ping(String) request to the Ponger. Blocks and returns response. */
    public String ping(final String from) throws Exception {
        return new PingRequest(from).processRequest(this);
    }
}
