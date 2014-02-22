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
import org.agilewiki.jactor2.core.requests.AsyncResponseProcessor;

/**
 * The Pinger's job is to hammer the Ponger with ping() request.
 * Implemented using async calls in JActor2.
 */
public class JActor2NonBlockingPinger extends BladeBase {

    /** Constructs a JActor2BlockingPinger. */
    public JActor2NonBlockingPinger(final Reactor _reactor) throws Exception {
        _initialize(_reactor);
    }

    /** Creates a hammer(ponger, int) request to the Pinger. */
    public AsyncBladeRequest<Integer> hammerReq(
            final JActor2NonBlockingPonger ponger, final int count) {
        /** A Ping request, targeted at Ponger. */
        return new AsyncBladeRequest<Integer>() {
            private final AsyncResponseProcessor<Integer> dis = this;
            private final AsyncResponseProcessor<Integer> pingResponseProcessor = new AsyncResponseProcessor<Integer>() {
                @Override
                public void processAsyncResponse(final Integer response)
                        throws Exception {
                    if (response.intValue() != done) {
                        throw new IllegalStateException("Expected " + done
                                + " but got " + response);
                    }
                    if (done < count) {
                        send(ponger.pingReq(done++), pingResponseProcessor);
                    } else {
                        dis.processAsyncResponse(response);
                    }
                }
            };
            private int done = 0;

            @Override
            public void processAsyncRequest() throws Exception {
                send(ponger.pingReq(done++), pingResponseProcessor);
            }
        };
    }

    /** Tells the Pinger to hammer the Ponger. Blocks and returns the result. */
    public int hammer(final JActor2NonBlockingPonger ponger, final int count)
            throws Exception {
        return hammerReq(ponger, count).call();
    }
}
