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

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

/**
 * Receives Pings, and send Pongs back.
 * Implemented with JActors. Blocks using JAFuture.
 */
public class JActorBlockingPonger extends JLPCActor {
    /** Some mutable data of Ponger, which must be access in a thread-safe way. */
    private int pings;

    /** A Ping request, targeted at Ponger. */
    private static class PingRequest extends
            Request<String, JActorBlockingPonger> {
        /** Just some state to use when processing requests. */
        private final String from;

        /** Creates the Ping request. */
        public PingRequest(final String _from) {
            from = _from;
        }

        /** Processes the ping(String) request, from within the Thread of the Ponger. */
        @SuppressWarnings("unchecked")
        @Override
        public void processRequest(final JLPCActor targetActor,
                @SuppressWarnings("rawtypes") final RP responseProcessor)
                throws Exception {
            final JActorBlockingPonger ponger = (JActorBlockingPonger) targetActor;
            responseProcessor.processResponse("Pong " + (ponger.pings++)
                    + " to " + from + "!");
        }

        @Override
        public boolean isTargetType(final Actor targetActor) {
            return JActorBlockingPonger.class.isInstance(targetActor);
        }
    }

    /** Creates a Ponger, with it's own mailbox.
     * @throws Exception */
    public JActorBlockingPonger(final Mailbox mbox) throws Exception {
        initialize(mbox);
    }

    /** Sends a ping(String) request to the Ponger. Blocks and returns response. */
    public String ping(final String from) throws Exception {
        final JAFuture future = new JAFuture();
        return new PingRequest(from).send(future, this);
    }
}
