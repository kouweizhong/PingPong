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

import kilim.Mailbox;
import kilim.Pausable;
import kilim.Task;

/**
 * Receives Pings, and send Pongs back.
 * Implemented using Kilim Tasks.
 */
public class KilimPonger extends Task {
    private final Mailbox<Object> pingerMB;
    private final Mailbox<Object> pongerMB;

    /** Some mutable data of Ponger. */
    private int pings;

    /** A Ping request, targeted at Ponger. */
    private static class PingRequest {
        /** Some data needed when processing a request. */
        private final int input;

        /** Creates a Ping request. */
        public PingRequest(final int _input) {
            input = _input;
        }

        /** Processes the ping(int) request. */
        public int processRequest(final KilimPonger ponger) throws Exception {
            ponger.pings++;
            return input + 1;
        }
    }

    public KilimPonger(final Mailbox<Object> _pingerMB,
            final Mailbox<Object> _pongerMB) {
        pingerMB = _pingerMB;
        pongerMB = _pongerMB;
    }

    @Override
    public void execute() throws Pausable {
        while (true) {
            final Object msg = pongerMB.get();
            if (msg instanceof PingRequest) {
                final PingRequest req = (PingRequest) msg;
                try {
                    pingerMB.put(req.processRequest(this));
                } catch (final Exception e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                new Exception("Expceted PingRequest but got " + msg.getClass())
                        .printStackTrace();
            }
        }
    }

    /** Sends a ping(int) request to the Ponger. Blocks and returns response. */
    public void ping(final int input) throws Exception {
        pongerMB.put(new PingRequest(input));
    }
}
