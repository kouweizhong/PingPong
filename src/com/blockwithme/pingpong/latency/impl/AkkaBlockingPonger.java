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

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import akka.util.Timeout;

/**
 * Receives Pings, and send Pongs back.
 * Implemented with Akka, using blocking Futures.
 */
public class AkkaBlockingPonger extends UntypedActor {
    /** Some mutable data of Ponger, which must be access in a thread-safe way. */
    private int pings;

    /** A Ping request, targeted at Ponger. */
    private static class PingRequest {
        public final int input;

        public PingRequest(final int _input) {
            input = _input;
        }

        /** Processes the ping(String) request, from within the Thread of the Ponger. */
        public void processRequest(final AkkaBlockingPonger ponger,
                final ActorRef sender) throws Exception {
            ponger.pings++;
            sender.tell(input + 1, ponger.getSelf());
        }
    }

    /** Processes all incoming messages, including replies. */
    @Override
    public void onReceive(final Object msg) throws Exception {
        if (msg instanceof PingRequest) {
            final PingRequest req = (PingRequest) msg;
            req.processRequest(this, getSender());
        } else {
            unhandled(msg);
        }
    }

    /** Sends a ping(int) request to the Ponger. Blocks and returns response. */
    public static Integer ping(final ActorRef pinger, final ActorRef ponger,
            final int input) throws Exception {
        final Timeout timeout = new Timeout(Duration.create(60, "seconds"));
        final Future<Object> future = Patterns.ask(ponger, new PingRequest(
                input), timeout);
        return (Integer) Await.result(future, timeout.duration());
    }
}
