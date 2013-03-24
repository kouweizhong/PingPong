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

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

/**
 * Receives Pings, and send Pongs back.
 * Implemented with the JActor.
 */
public class JActorIteratorPonger extends JLPCActor {
    /** Some mutable data of Ponger, which must be access in a thread-safe way. */
    private int pings;

    /** A Ping request, targeted at Ponger. */
    private static class PingRequest extends
            Request<Integer, JActorIteratorPonger> {
        private final int input;

        /** Creates a Ping request. */
        public PingRequest(final int _input) {
            input = _input;
        }

        /** Processes the ping(String) request, from within the Thread of the Ponger. */
        @SuppressWarnings("unchecked")
        @Override
        public void processRequest(final JLPCActor targetActor,
                @SuppressWarnings("rawtypes") final RP responseProcessor)
                throws Exception {
            final JActorIteratorPonger ponger = (JActorIteratorPonger) targetActor;
            ponger.pings++;
            responseProcessor.processResponse(input + 1);
        }

        @Override
        public boolean isTargetType(final Actor targetActor) {
            return JActorIteratorPonger.class.isInstance(targetActor);
        }
    }

    /** Creates a Ponger, with it's own mailbox.
     * @throws Exception */
    public JActorIteratorPonger(final Mailbox mbox) throws Exception {
        initialize(mbox);
    }

    /** Sends a ping(String) request to the Ponger. */
    public void ping(final JActorIteratorPinger pinger, final RP<Integer> rp,
            final int _input) throws Exception {
        new PingRequest(_input).send(pinger, this, rp);
    }
}
