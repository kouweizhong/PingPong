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
public class JActor2Sender extends BladeBase {

    final JActor2Echo echo;
    final int count;
    final int burst;

    /** Constructs a JActor2Echo. */
    public JActor2Sender(final Reactor _reactor, final JActor2Echo _echo,
            final int _count, final int _burst) throws Exception {
        _initialize(_reactor);
        echo = _echo;
        count = _count;
        burst = _burst;
    }

    /** Creates an start echo request. */
    private static StaticAOp<JActor2Sender, Void> START_ECHO_REQ = new StaticAOp<JActor2Sender, Void>(
            JActor2Sender.class) {
        private final IntVar batches = var(0);
        private final IntVar replies = var(0);

        private void echo(final JActor2Sender blade,
                final AsyncRequestImplWithData<Void> r,
                final AsyncResponseProcessor<Void> echoResponseProcessor)
                throws Exception {
            final int burst = blade.burst;
            final JActor2Echo echo = blade.echo;
            for (int i = 0; i < burst; i++) {
                r.send(echo.echoReq(), echoResponseProcessor);
            }
            /** Number of batches done. */
            batches.inc(r);
        }

        @Override
        protected void processAsyncOperation(final JActor2Sender blade,
                final AsyncRequestImplWithData<Void> _asyncRequestImpl,
                final AsyncResponseProcessor<Void> _asyncResponseProcessor)
                throws Exception {
            final int count = blade.count;
            // TODO Stupid work-around for compiler bug!
            final AsyncResponseProcessor<Void>[] echoResponseProcessor2 = new AsyncResponseProcessor[1];
            final AsyncResponseProcessor<Void> echoResponseProcessor = new AsyncResponseProcessor<Void>() {
                @Override
                public void processAsyncResponse(final Void response)
                        throws Exception {
                    /** Number of replies in the current batch. */
                    replies.inc(_asyncRequestImpl);
                    final int _replies = replies.get(_asyncRequestImpl);
                    if (_replies == blade.burst) {
                        replies.set(_asyncRequestImpl, 0);
                        final int _batches = batches.get(_asyncRequestImpl);
                        if (_batches < count) {
                            echo(blade, _asyncRequestImpl,
                                    echoResponseProcessor2[0]);
                        } else {
                            _asyncRequestImpl.processAsyncResponse(response);
                        }
                    }
                }
            };
            echoResponseProcessor2[0] = echoResponseProcessor;
            echo(blade, _asyncRequestImpl, echoResponseProcessor);
        }
    };

    /** Creates an start echo request. */
    public AsyncRequestImplWithData<Void> startEchoReq() {
        return START_ECHO_REQ.create(this);
    }
}
