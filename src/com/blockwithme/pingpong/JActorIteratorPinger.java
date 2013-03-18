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
import org.agilewiki.jactor.JABidiIterator;
import org.agilewiki.jactor.JAFuture;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

/**
 * The Pinger's job is to hammer the Ponger with ping() request.
 * Implemented with JActors, using the JABidiIterator.
 */
public class JActorIteratorPinger extends JLPCActor {
    /** A Hammer request, targeted at Pinger. */
    private static class HammerRequest extends
            Request<String, JActorIteratorPinger> {
        /** The Ponger to hammer. */
        private final JActorIteratorPonger ponger;

        /** The number of exchanges to do. */
        private final int count;

        /** The number of pongs received. */
        private int done;

        /** The pinger */
        private JActorIteratorPinger pinger;

        /** Creates a hammer request, with the targeted Ponger. */
        public HammerRequest(final JActorIteratorPonger _ponger,
                final int _count) {
            ponger = _ponger;
            count = _count;
        }

        /** Process the hammer request. */
        @SuppressWarnings("unchecked")
        @Override
        public void processRequest(final JLPCActor targetActor,
                @SuppressWarnings("rawtypes") final RP _responseProcessor)
                throws Exception {
            pinger = (JActorIteratorPinger) targetActor;

            // The JABidiIterator needs to separate the sendRequest() (loop)
            // from the processResponse(), so that no Stack-Overflow happens.
            // Both methods can communicate with each other over shared state.
            (new JABidiIterator() {
                /** Loops, normally by sending some request. */
                @Override
                protected void sendRequest(
                        @SuppressWarnings("rawtypes") final RP responseProcessor)
                        throws Exception {
                    ponger.ping(pinger, responseProcessor);
                }

                /**
                 * Receives the response to some request.
                 * Returns null to continue, and non-null to signify the iterator
                 * is done, and that non-null value should be returned to the
                 * RP originally passed to the iterate(RP) method.
                 */
                @Override
                protected Object processResponse(final Object response)
                        throws Exception {
                    // response is ignored in this case, but we *could* have used it,
                    // which is the main thing.
                    done++;
                    if (done < count) {
                        return null;
                    } else {
                        return "done";
                    }
                }
            }).iterate(_responseProcessor);
        }

        @Override
        public boolean isTargetType(final Actor targetActor) {
            return JActorIteratorPinger.class.isInstance(targetActor);
        }
    }

    /** Creates a Pinger, with it's own mailbox and name.
     * @throws Exception */
    public JActorIteratorPinger(final Mailbox mbox) throws Exception {
        initialize(mbox);
    }

    /** Tells the pinger to hammer the Ponger. Blocks and returns result. */
    public String hammer(final JActorIteratorPonger ponger, final int _count)
            throws Exception {
        final JAFuture future = new JAFuture();
        return new HammerRequest(ponger, _count).send(future, this);
    }
}
