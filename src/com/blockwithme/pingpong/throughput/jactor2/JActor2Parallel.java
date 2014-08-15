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
package com.blockwithme.pingpong.throughput.jactor2;

import org.agilewiki.jactor2.core.blades.BladeBase;
import org.agilewiki.jactor2.core.reactors.Reactor;
import org.agilewiki.jactor2.core.requests.AsyncRequestImplWithData;
import org.agilewiki.jactor2.core.requests.AsyncResponseProcessor;
import org.agilewiki.jactor2.core.requests.StaticAOp;

/**
 * @author monster
 *
 */
public class JActor2Parallel extends BladeBase {

    private final JActor2Sender[] senders;

    /** Constructs a JActor2Echo. */
    public JActor2Parallel(final Reactor _reactor,
            final JActor2Sender[] _senders) throws Exception {
        _initialize(_reactor);
        senders = _senders;
    }

    /** Creates an start parallel request. */
    private static final StaticAOp<JActor2Parallel, Void> START_PAR_REQ = new StaticAOp<JActor2Parallel, Void>(
            JActor2Parallel.class) {
        private final IntVar replies = var(0);

        @Override
        protected void processAsyncOperation(final JActor2Parallel blade,
                final AsyncRequestImplWithData<Void> _asyncRequestImpl,
                final AsyncResponseProcessor<Void> _asyncResponseProcessor)
                throws Exception {
            final JActor2Sender[] senders = blade.senders;
            final AsyncResponseProcessor<Void> echoResponseProcessor = new AsyncResponseProcessor<Void>() {
                @Override
                public void processAsyncResponse(final Void response)
                        throws Exception {
                    if (replies.inc(_asyncRequestImpl) == senders.length - 1) {
                        _asyncRequestImpl.processAsyncResponse(response);
                    }
                }
            };
            for (int i = 0; i < senders.length; i++) {
                _asyncRequestImpl.send(senders[i].startEchoReq(),
                        echoResponseProcessor);
            }
        }
    };

    public AsyncRequestImplWithData<Void> startParReq() {
        final AsyncRequestImplWithData<Void> result = START_PAR_REQ
                .create(this);
        result.setExpectedPendingResponses((int) (senders.length * 1.3));
        return result;
    }
}
