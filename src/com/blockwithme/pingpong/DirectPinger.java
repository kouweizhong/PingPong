package com.blockwithme.pingpong;

/**
 * The Pinger's job is to hammer the Ponger with ping() request,
 * to count how many can be done in one second.
 */
public class DirectPinger {
    /** A Hammer request, targeted at Pinger. */
    private static class HammerRequest {
        /** The Ponger to hammer. */
        private final DirectPonger ponger;

        /** The number of exchanges to do. */
        private final int count;

        /** Creates a hammer request, with the targeted Ponger. */
        public HammerRequest(final DirectPonger _ponger, final int _count) {
            ponger = _ponger;
            count = _count;
        }

        /** Process the hammer request. */
        public String processRequest(final DirectPinger pinger)
                throws Exception {
            final String name = pinger.toString();
            int done = 0;
            while (done < count) {
                ponger.ping(name);
                done++;
            }
            return "done";
        }
    }

    /** Tells the pinger to hammer the Ponger. Describes the speed in the result. */
    public String hammer(final DirectPonger ponger, final int _count)
            throws Exception {
        return new HammerRequest(ponger, _count).processRequest(this);
    }
}
