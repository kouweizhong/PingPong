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

import org.agilewiki.jactor2.core.blades.BladeBase;
import org.agilewiki.jactor2.core.messages.SyncRequest;
import org.agilewiki.jactor2.core.reactors.Reactor;

/**
 * The Pinger's job is to hammer the Ponger with ping() request.
 * Implemented using local calls in JActor2.
 */
public class JActor2LocalPinger extends BladeBase {

    /** Constructs a JActor2BlockingPinger. */
    public JActor2LocalPinger(final Reactor _reactor) throws Exception {
        initialize(_reactor);
    }

    /** Creates a hammer(ponger, int) request to the Pinger. */
    public SyncRequest<Integer> hammerReq(final JActor2LocalPonger ponger,
            final int count) {
        /** A Ping request, targeted at Ponger. */
        return new SyncBladeRequest<Integer>() {
            @Override
            protected Integer processSyncRequest() throws Exception {
                int done = 0;
                while (done < count) {
                    final Integer response = ponger.pingCall(done);
                    done++;
                    if (response.intValue() != done) {
                        throw new IllegalStateException("Expected " + done
                                + " but got " + response);
                    }
                }
                return done;
            }
        };
    }

    /** Tells the Pinger to hammer the Ponger. Blocks and returns the result. */
    public int hammer(final JActor2LocalPonger ponger, final int count)
            throws Exception {
        return hammerReq(ponger, count).call();
    }
}
