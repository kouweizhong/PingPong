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
 * The Pinger's job is to hammer the Ponger with ping() request.
 * Implemented in the current Thread, as the baseline, fastest possible impl.
 */
public class DirectPinger {
    /** A Hammer request, targeted at Pinger. */
    private static class HammerRequest {
        /** The Ponger to hammer. */
        private final DirectPonger ponger;

        /** The number of exchanges to do. */
        private final int count;

        /** Creates a hammer request, with the targeted Ponger. */
        public HammerRequest(final DirectPonger _ponger, final int _count) {
            ponger = _ponger;
            count = _count;
        }

        /** Process the hammer request. */
        public int processRequest(final DirectPinger pinger) throws Exception {
            int done = 0;
            while (done < count) {
                final Integer response = ponger.ping(done);
                done++;
                if (response.intValue() != done) {
                    throw new IllegalStateException("Expected " + done
                            + " but got " + response);
                }
            }
            return done;
        }
    }

    /** Tells the pinger to hammer the Ponger. Blocks and returns the result. */
    public int hammer(final DirectPonger ponger, final int _count)
            throws Exception {
        return new HammerRequest(ponger, _count).processRequest(this);
    }
}
