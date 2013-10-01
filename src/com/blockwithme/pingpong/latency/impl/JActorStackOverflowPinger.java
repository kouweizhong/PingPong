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
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

/**
 * The Pinger's job is to hammer the Ponger with ping() request.
 * Implemented in JActor using a simplistic impl, which causes occasional Stack-Overflow!
 */
public class JActorStackOverflowPinger extends JLPCActor {
    /** A Hammer request, targeted at Pinger. */
    private static class HammerRequest extends
            Request<Integer, JActorStackOverflowPinger> {
        /** The Ponger to hammer. */
        private final JActorStackOverflowPonger ponger;

        /** The number of exchanges to do. */
        private final int count;

        /** The responseProcessor from the test, to call when done. */
        private RP<Integer> responseProcessor;

        /** The number of pings. */
        private int done;

        /** The pinger */
        private JActorStackOverflowPinger pinger;

        /** Creates a hammer request, with the targeted Ponger. */
        public HammerRequest(final JActorStackOverflowPonger _ponger,
                final int _count) {
            ponger = _ponger;
            count = _count;
        }

        /** Sends one ping request to the Ponger. */
        private void ping() throws Exception {
            ponger.ping(pinger, new RP<Integer>() {
                @Override
                public void processResponse(final Integer response)
                        throws Exception {
                    done++;
                    if (done != response.intValue()) {
                        throw new IllegalStateException("Expected " + done
                                + " but got " + response);
                    }
                    if (done < count) {
                        // again ...
                        ping();
                    } else {
                        responseProcessor.processResponse(done);
                    }

                }
            }, done);
        }

        /** Process the hammer request. */
        @SuppressWarnings("unchecked")
        @Override
        public void processRequest(final JLPCActor targetActor,
                @SuppressWarnings("rawtypes") final RP _responseProcessor)
                throws Exception {
            pinger = (JActorStackOverflowPinger) targetActor;
            responseProcessor = _responseProcessor;
            boolean again = true;
            while (again) {
                try {
                    ping();
                    again = false;
                } catch (final StackOverflowError e) {
                    // NOP
                }
            }
        }

        @Override
        public boolean isTargetType(final Actor targetActor) {
            return JActorStackOverflowPinger.class.isInstance(targetActor);
        }
    }

    /** Creates a Pinger, with it's own mailbox and name.
     * @throws Exception */
    public JActorStackOverflowPinger(final Mailbox mbox) throws Exception {
        initialize(mbox);
    }

    /** Tells the pinger to hammer the Ponger. Blocks and return results. */
    public Integer hammer(final JActorStackOverflowPonger ponger,
            final int count) throws Exception {
        final JAFuture future = new JAFuture();
        return new HammerRequest(ponger, count).send(future, this);
    }
}
