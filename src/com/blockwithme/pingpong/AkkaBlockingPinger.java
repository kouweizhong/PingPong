package com.blockwithme.pingpong;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

/**
 * The Pinger's job is to hammer the Ponger with ping() request,
 * to count how many can be done in one second.
 */
public class AkkaBlockingPinger extends UntypedActor {
    /** A Hammer request, targeted at Pinger. */
    public static class HammerRequest {
        /** The Ponger to hammer. */
        private final ActorRef ponger;

        /** The number of exchanges to do. */
        private final int count;

        /** Creates a hammer request, with the targeted Ponger. */
        public HammerRequest(final ActorRef _ponger, final int _count) {
            ponger = _ponger;
            count = _count;
        }

        /** Process the hammer request.
         * @param sender */
        public void processRequest(final AkkaBlockingPinger pinger, final ActorRef sender)
                throws Exception {
            final ActorRef pingerRef = pinger.getSelf();
            int done = 0;
            while (done < count) {
                AkkaBlockingPonger.ping(pingerRef, ponger);
                done++;
            }
            sender.tell("done", pingerRef);
        }
    }

    /** Tells the pinger to hammer the Ponger. Describes the speed in the result. */
    public static HammerRequest hammer(final ActorRef ponger, final int _count)
            throws Exception {
        return new HammerRequest(ponger, _count);
    }

    /* (non-Javadoc)
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(final Object msg) throws Exception {
        if (msg instanceof HammerRequest) {
            final HammerRequest req = (HammerRequest) msg;
            req.processRequest(this, getSender());
        } else {
            unhandled(msg);
        }
    }
}
