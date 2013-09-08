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

import org.agilewiki.jactor2.core.messaging.AsyncRequest;
import org.agilewiki.jactor2.core.messaging.AsyncResponseProcessor;
import org.agilewiki.jactor2.core.processing.MessageProcessor;

/**
 * Receives Pings, and send Pongs back.
 *
 * Implemented using JActor2, by having the processing of the reply create the
 * next request, therefore not needing blocking.
 */
public class JActor2NonBlockingPonger {
    /** Ponger mailbox */
    private final MessageProcessor mailbox;

    /** Some mutable data of Ponger, which must be access in a thread-safe way. */
    private int pings;

    private volatile String STATE = "Ponger() CREATED";

    private volatile PingRequest LAST_PING;

    /** A Ping request, targeted at Ponger. */
    private class PingRequest extends AsyncRequest<Integer> {
        private volatile String STATE = "CREATED";
        /** Some state needed to process the request. */
        private final int input;

        /** Creates a Ping request. */
        public PingRequest(final MessageProcessor mbox, final int _input) {
            super(mbox);
            input = _input;
            STATE = "PingRequest(" + input + ") CREATED";
        }

        /** Processes the ping(String) request, from within the Thread of the Ponger. */
        @Override
        public void processAsyncRequest() throws Exception {
            STATE = "PingRequest(" + input + ") BEFORE processResponse("
                    + (input + 1) + ")";
            processAsyncResponse(input + 1);
            STATE = "PingRequest(" + input + ") AFTER processResponse("
                    + (input + 1) + ")";
        }
    }

    /** Creates a Ponger, with it's own mailbox. */
    public JActor2NonBlockingPonger(final MessageProcessor mbox) {
        mailbox = mbox;
    }

    /** Sends a ping(String) request to the Ponger. */
    public void ping(final JActor2NonBlockingPinger from,
            final AsyncResponseProcessor<Integer> responseProcessor,
            final int input) throws Exception {
        STATE = "Ponger() GOT ping(" + input + ") CALL";
        final PingRequest ping = new PingRequest(mailbox, input);
        LAST_PING = ping;
        ping.send(from.getMailbox(), responseProcessor);
        STATE = "Ponger() SENT ping(" + input
                + ") REQUEST / WAITING FOR RESPONSE";
    }
}
