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
import org.agilewiki.jactor2.core.reactors.Reactor;
import org.agilewiki.jactor2.core.requests.StaticSOp;
import org.agilewiki.jactor2.core.requests.impl.RequestImpl;
import org.agilewiki.jactor2.core.requests.impl.RequestImplWithData;

/**
 * Receives Pings, and send Pongs back.
 * Implemented using async calls in JActor2.
 */
public class JActor2Ponger extends BladeBase {
    private static final class PingReq extends
            StaticSOp<JActor2Ponger, Integer> {
        private final IntVar input = var(0);

        public PingReq() {
            super(JActor2Ponger.class);
        }

        @Override
        protected Integer processSyncOperation(final JActor2Ponger ponger,
                final RequestImplWithData<Integer> _requestImpl)
                throws Exception {
            ponger.pings++;
            return input.get(_requestImpl) + 1;
        }

        /** Creates a ping(int) request to the Ponger. */
        public RequestImpl<Integer> pingReq(final JActor2Ponger p,
                final int _input) {
            /** A Ping request, targeted at Ponger. */
            return input.set(create(p), _input);
        }
    };

    private static final PingReq PING_REQ = new PingReq();

    /** Some mutable data of Ponger. */
    private int pings;

    /** Constructs a JActor2Ponger. */
    public JActor2Ponger(final Reactor _reactor) throws Exception {
        _initialize(_reactor);
    }

    /** Creates a ping(int) request to the Ponger. */
    public RequestImpl<Integer> pingReq(final int input) {
        return PING_REQ.pingReq(this, input);
    }

    /** Returns the number of Pings received. */
    public int getPings() {
        return pings;
    }
}
