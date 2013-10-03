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
 * Receives Pings, and send Pongs back.
 * Implemented using local calls in JActor2.
 */
public class JActor2LocalPonger extends BladeBase {
    /** Some mutable data of Ponger. */
    private int pings;

    /** Constructs a JActor2Ponger. */
    public JActor2LocalPonger(final Reactor _reactor) throws Exception {
        initialize(_reactor);
    }

    /** Creates a ping(int) request to the Ponger. */
    public SyncRequest<Integer> pingReq(final int input) {
        /** A Ping request, targeted at Ponger. */
        return new SyncBladeRequest<Integer>() {
            @Override
            protected Integer processSyncRequest() throws Exception {
                pings++;
                return input + 1;
            }
        };
    }

    /** Sends a ping(int) request to the Ponger. Blocks and returns response. */
    public Integer pingCall(final int input) throws Exception {
        return local(pingReq(input));
    }

    /** Returns the number of Pings received. */
    public int getPings() {
        return pings;
    }
}
