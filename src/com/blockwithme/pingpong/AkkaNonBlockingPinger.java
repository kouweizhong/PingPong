package com.blockwithme.pingpong;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

/**
 * The Pinger's job is to hammer the Ponger with ping() request,
 * to count how many can be done in one second.
 */
public class AkkaNonBlockingPinger extends UntypedActor {
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
        public void processRequest(final AkkaNonBlockingPinger pinger,
                final ActorRef sender) throws Exception {
            pinger.count = count;
            pinger.ponger = ponger;
            pinger.requester = sender;
            final ActorRef pingerRef = pinger.getSelf();
            AkkaNonBlockingPonger.ping(pingerRef, ponger);
        }
    }

    /** Number of replies received. */
    private int pongs;

    /** The Ponger to hammer. */
    private ActorRef ponger;

    /** The requester. */
    private ActorRef requester;

    /** The number of exchanges to do. */
    private int count;

    /** Reacts to reply
     * @throws Exception */
    private void onReply(final AkkaNonBlockingPonger.PongReply reply)
            throws Exception {
        pongs++;
        if (pongs < count) {
            AkkaNonBlockingPonger.ping(getSelf(), ponger);
        } else {
            requester.tell("done", getSelf());
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
        } else if (msg instanceof AkkaNonBlockingPonger.PongReply) {
            onReply((AkkaNonBlockingPonger.PongReply) msg);
        } else {
            unhandled(msg);
        }
    }
}
