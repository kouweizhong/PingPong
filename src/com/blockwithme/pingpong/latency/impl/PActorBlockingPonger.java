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

import org.agilewiki.pactor.Mailbox;
import org.agilewiki.pactor.RequestBase;
import org.agilewiki.pactor.ResponseProcessor;

/**
 * Receives Pings, and send Pongs back.
 * Implemented with PActors.
 */
public class PActorBlockingPonger {
    /** Ponger mailbox */
    private final Mailbox mailbox;

    /** Some mutable data of Ponger, which must be access in a thread-safe way. */
    private int pings;

    /** A Ping request, targeted at Ponger. */
    private class PingRequest extends RequestBase<Integer> {
        private final int input;

        public PingRequest(final Mailbox mbox, final int _input) {
            super(mbox);
            input = _input;
        }

        /** Processes the ping(String) request, from within the Thread of the Ponger. */
        @Override
        public void processRequest(
                final ResponseProcessor<Integer> responseProcessor)
                throws Exception {
            pings++;
            responseProcessor.processResponse(input + 1);
        }
    }

    /** Creates a Ponger, with it's own mailbox. */
    public PActorBlockingPonger(final Mailbox mbox) {
        mailbox = mbox;
    }

    /** Sends a ping(String) request to the Ponger. Blocks and returns response. */
    public Integer ping(final int input) throws Exception {
        return new PingRequest(mailbox, input).call();
    }
}
