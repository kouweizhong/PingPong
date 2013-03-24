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
package com.blockwithme.pingpong.latency.impl;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;

/**
 * Receives Pings, and send Pongs back.
 * Implemented in Akka, by having the processing of the responses cause the new
 * request, therefore not requiring blocking.
 */
public class AkkaNonBlockingPonger extends UntypedActor {
    /** Some mutable data of Ponger, which must be access in a thread-safe way. */
    private int pings;

    /** A Ping request, targeted at Ponger. */
    private static class PingRequest {
        public final int input;

        public PingRequest(final int _input) {
            input = _input;
        }

        /** Processes the ping(String) request, from within the Thread of the Ponger. */
        public void processRequest(final AkkaNonBlockingPonger ponger,
                final ActorRef sender) throws Exception {
            ponger.pings++;
            sender.tell(new PongReply(input + 1), ponger.getSelf());
        }
    }

    /** Pong's reply */
    public static class PongReply {
        /** the reply. */
        public final int output;

        /** Creates a Pong reply. */
        public PongReply(final int _output) {
            output = _output;
        }
    }

    /** Processes some incoming message. */
    @Override
    public void onReceive(final Object msg) throws Exception {
        if (msg instanceof PingRequest) {
            final PingRequest req = (PingRequest) msg;
            req.processRequest(this, getSender());
        } else {
            unhandled(msg);
        }
    }

    /** Sends a ping(String) request to the Ponger. */
    public static void ping(final ActorRef pinger, final ActorRef ponger,
            final int _input) throws Exception {
        ponger.tell(new PingRequest(_input), pinger);
    }
}
