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

import java.util.concurrent.ExecutorService;

/**
 * Receives Pings, and send Pongs back.
 * Implemented using an ExecutorService.
 */
public class ExecutorServicePonger extends ExecutorServiceActor {
    /** Some mutable data of Ponger, which must be access in a thread-safe way. */
    private int pings;

    /** Process all incoming messages, including replies. */
    @Override
    protected void processMessage(final Object msg,
            final ExecutorServiceActor sender) throws Exception {
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
        public final int output;

        /** Creates a PongReply */
        public PongReply(final int _output) {
            output = _output;
        }
    }

    /** A Ping request, targeted at Ponger. */
    private static class PingRequest {
        private final int input;

        /** Create a PingRequest */
        public PingRequest(final int _input) {
            input = _input;
        }

        /** Processes the ping(int) request, from within the Thread of the Ponger. */
        public PongReply processRequest(final ExecutorServicePonger ponger)
                throws Exception {
            ponger.pings++;
            return new PongReply(input + 1);
        }
    }

    /** Constructor */
    public ExecutorServicePonger(final ExecutorService _executorService) {
        super(_executorService);
    }

    /** Sends a ping(int) request to the Ponger. */
    public void ping(final int _input, final ExecutorServiceActor sender)
            throws Exception {
        queueMessage(new PingRequest(_input), sender);
    }
}
