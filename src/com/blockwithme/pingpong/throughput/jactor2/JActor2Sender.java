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
public class JActor2Sender extends BladeBase {

    private final JActor2Echo echo;
    private final int count;
    private final int burst;

    /** Constructs a JActor2Echo. */
    public JActor2Sender(final Reactor _reactor, final JActor2Echo _echo,
            final int _count, final int _burst) throws Exception {
        initialize(_reactor);
        echo = _echo;
        count = _count;
        burst = _burst;
    }

    /** Creates an start echo request. */
    public AsyncRequest<Void> startEchoReq() {
        return new AsyncBladeRequest<Void>() {
            private final AsyncResponseProcessor<Void> dis = this;
            private final AsyncResponseProcessor<Void> echoResponseProcessor = new AsyncResponseProcessor<Void>() {
                @Override
                public void processAsyncResponse(final Void response)
                        throws Exception {
                    replies++;
                    if (replies == burst) {
                        replies = 0;
                        if (batches < count) {
                            echo();
                        } else {
                            dis.processAsyncResponse(response);
                        }
                    }
                }
            };
            /** Number of batches done. */
            private int batches = 0;
            /** Number of replies in the current batch. */
            private int replies = 0;

            private void echo() throws Exception {
                for (int i = 0; i < burst; i++) {
                    send(echo.echoReq(), echoResponseProcessor);
                }
                batches++;
            }

            @Override
            protected void processAsyncRequest() throws Exception {
                echo();
            }
        };
    }
}
