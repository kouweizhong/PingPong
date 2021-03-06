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

/**
 * Receives Pings, and send Pongs back.
 * Implemented in the current Thread, as the baseline, fastest possible impl.
 */
public class DirectPonger {
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
        public Integer processRequest(final DirectPonger ponger)
                throws Exception {
            ponger.pings++;
            return input + 1;
        }
    }

    /** Sends a ping(int) request to the Ponger. Blocks and returns response. */
    public Integer ping(final int input) throws Exception {
        return new PingRequest(input).processRequest(this);
    }
}
