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
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

/**
 * Receives Pings, and send Pongs back.
 * Implemented in JActor using a simplistic impl, which causes occasional Stack-Overflow!
 */
public class JActorStackOverflowPonger extends JLPCActor {
    /** Some mutable data of Ponger, which must be access in a thread-safe way. */
    private int pings;

    /** A Ping request, targeted at Ponger. */
    private static class PingRequest2 extends
            Request<String, JActorStackOverflowPonger> {
        /** Some state needed to process the ping request. */
        private final String from;

        /** Creates a ping request */
        public PingRequest2(final String _from) {
            from = _from;
        }

        /** Processes the ping(String) request, from within the Thread of the Ponger. */
        @SuppressWarnings("unchecked")
        @Override
        public void processRequest(final JLPCActor targetActor,
                @SuppressWarnings("rawtypes") final RP responseProcessor)
                throws Exception {
            final JActorStackOverflowPonger ponger = (JActorStackOverflowPonger) targetActor;
            responseProcessor.processResponse("Pong " + (ponger.pings++)
                    + " to " + from + "!");
        }

        @Override
        public boolean isTargetType(final Actor targetActor) {
            return JActorStackOverflowPonger.class.isInstance(targetActor);
        }
    }

    /** Creates a Ponger, with it's own mailbox.
     * @throws Exception */
    public JActorStackOverflowPonger(final Mailbox mbox) throws Exception {
        initialize(mbox);
    }

    /** Sends a ping(String) request to the Ponger. */
    public void ping(final JActorStackOverflowPinger pinger, final RP<String> rp)
            throws Exception {
        new PingRequest2(pinger.toString()).send(pinger, this, rp);
    }
}
