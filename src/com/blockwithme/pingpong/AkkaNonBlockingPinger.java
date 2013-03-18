/*
 * Copyright (C) 2013 Sebastien Diot.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blockwithme.pingpong;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

/**
 * The Pinger's job is to hammer the Ponger with ping() request.
 * Implemented in Akka, by having the processing of the responses cause the new
 * request, therefore not requiring blocking.
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
            // Sends the first ping, to start the loop.
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

    /** Reacts to PongReply
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

    /** Creates a HammerRequest, to hammer the Ponger. Does NOT send the request. */
    public static HammerRequest hammer(final ActorRef ponger, final int _count)
            throws Exception {
        return new HammerRequest(ponger, _count);
    }

    /** Processes all incoming messages, including replies. */
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
