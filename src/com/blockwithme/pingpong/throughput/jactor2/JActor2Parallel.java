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
import org.agilewiki.jactor2.core.messages.AsyncRequest;
import org.agilewiki.jactor2.core.messages.AsyncResponseProcessor;
import org.agilewiki.jactor2.core.reactors.Reactor;

/**
 * @author monster
 *
 */
public class JActor2Parallel extends BladeBase {

    private final JActor2Sender[] senders;

    /** Constructs a JActor2Echo. */
    public JActor2Parallel(final Reactor _reactor,
            final JActor2Sender[] _senders) throws Exception {
        initialize(_reactor);
        senders = _senders;
    }

    /** Creates an start parallel request. */
    public AsyncRequest<Void> startParReq() {
        return new AsyncBladeRequest<Void>() {
            private final AsyncResponseProcessor<Void> dis = this;
            private final AsyncResponseProcessor<Void> echoResponseProcessor = new AsyncResponseProcessor<Void>() {
                @Override
                public void processAsyncResponse(final Void response)
                        throws Exception {
                    replies++;
                    if (replies == senders.length) {
                        dis.processAsyncResponse(response);
                    }
                }
            };
            /** Number of replies. */
            private int replies = 0;

            @Override
            protected void processAsyncRequest() throws Exception {
                for (int i = 0; i < senders.length; i++) {
                    send(senders[i].startEchoReq(), echoResponseProcessor);
                }
            }
        };
    }
}
