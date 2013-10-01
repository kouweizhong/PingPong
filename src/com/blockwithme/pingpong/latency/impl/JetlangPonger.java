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

import org.jetlang.channels.AsyncRequest;
import org.jetlang.channels.MemoryRequestChannel;
import org.jetlang.channels.Request;
import org.jetlang.core.Callback;
import org.jetlang.fibers.Fiber;

/**
 * Receives Pings, and send Pongs back.
 * Implemented with Jetlang.
 */
public class JetlangPonger {
    /** A Ping request, targeted at Ponger. */
    private static class PingRequest {
        /** Some data needed when processing a request. */
        private final int input;

        /** Creates a Ping request. */
        public PingRequest(final int _input) {
            input = _input;
        }

        /** Processes the ping(int) request. */
        public int processRequest(final JetlangPonger ponger) {
            ponger.pings++;
            return input + 1;
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

    /** The jetlang fiber. */
    private final Fiber fiber;

    /** Our channel */
    @SuppressWarnings("rawtypes")
    private final MemoryRequestChannel channel;

    /** Some mutable data of Ponger. */
    private int pings;

    /** Creates a JetlangPonger. */
    public JetlangPonger(final Fiber _fiber) {
        fiber = _fiber;
        channel = new MemoryRequestChannel();
        final Callback<Request> onReq = new Callback<Request>() {
            @Override
            public void onMessage(final Request message) {
                final Object request = message.getRequest();
                if (request instanceof PingRequest) {
                    final PingRequest ping = (PingRequest) request;
                    message.reply(ping.processRequest(JetlangPonger.this));
                } else {
                    throw new IllegalStateException(
                            "Expected PingRequest but got "
                                    + request.getClass());
                }
            }
        };
        fiber.start();
        channel.subscribe(fiber, onReq);
    }

    /** Sends a ping(int) request to the Ponger. Blocks and returns response. */
    public void ping(final int input, final Callback<Integer> onReply)
            throws Exception {
        final PingRequest req = new PingRequest(input);
        AsyncRequest.withOneReply(fiber, channel, req, onReply);
    }
}
