package com.blockwithme.pingpong;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import akka.util.Timeout;

/**
 * Receives Pings, and send Pongs back.
 */
public class AkkaBlockingPonger extends UntypedActor {
    /** Some mutable data of Ponger, which must be access in a thread-safe way. */
    private int pings;

    /** A Ping request, targeted at Ponger. */
    private static class PingRequest {
        /** Processes the ping(String) request, from within the Thread of the Ponger. */
        public void processRequest(final AkkaBlockingPonger ponger, final ActorRef sender)
                throws Exception {
            sender.tell("Pong " + (ponger.pings++) + " to " + sender + "!",
                    ponger.getSelf());
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
    public static String ping(final ActorRef pinger, final ActorRef ponger)
            throws Exception {
        final Timeout timeout = new Timeout(Duration.create(60, "seconds"));
        final Future<Object> future = Patterns.ask(ponger, new PingRequest(),
                timeout);
        return (String) Await.result(future, timeout.duration());
    }
}
