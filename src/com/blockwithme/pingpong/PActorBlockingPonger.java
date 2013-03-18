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
package com.blockwithme.pingpong;

import org.agilewiki.pactor.Mailbox;
import org.agilewiki.pactor.Request;
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
    private class PingRequest extends Request<String> {
        private final String from;

        public PingRequest(final Mailbox mbox, final String _from) {
            super(mbox);
            from = _from;
        }

        /** Processes the ping(String) request, from within the Thread of the Ponger. */
        @Override
        public void processRequest(
                final ResponseProcessor<String> responseProcessor)
                throws Exception {
            responseProcessor.processResponse("Pong " + (pings++) + " to "
                    + from + "!");
        }
    }

    /** Creates a Ponger, with it's own mailbox. */
    public PActorBlockingPonger(final Mailbox mbox) {
        mailbox = mbox;
    }

    /** Sends a ping(String) request to the Ponger. Blocks and returns response. */
    public String ping(final String from) throws Exception {
        return new PingRequest(mailbox, from).pend();
    }
}
