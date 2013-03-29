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
 * The Pinger's job is to hammer the Ponger with ping() request.
 * This impl uses PActor, and generates request while processing reply, to eliminate blocking.
 */
public class PActorNonBlockingPinger {

    /** The Pinger's mailbox. */
    private final Mailbox mailbox;

    /** A Hammer request, targeted at Pinger. */
    private class HammerRequest extends RequestBase<Integer> {
        /** The Ponger to hammer. */
        private final PActorNonBlockingPonger ponger;

        /** The number of exchanges to do. */
        private final int count;

        /** The number of pings sent. */
        private int done;

        /** ResponseProcessor for benchmark results */
        private ResponseProcessor<Integer> responseProcessor;

        /** Creates a hammer request, with the targeted Ponger. */
        public HammerRequest(final Mailbox mbox,
                final PActorNonBlockingPonger _ponger, final int _count) {
            super(mbox);
            ponger = _ponger;
            count = _count;
        }

        /** Send a ping, unless we're done, in which case tell caller we are done. */
        private void ping() throws Exception {
            done++;
            if (done < count) {
                ponger.ping(PActorNonBlockingPinger.this,
                        new ResponseProcessor<Integer>() {
                            @Override
                            public void processResponse(final Integer response)
                                    throws Exception {
                                if (response.intValue() != done + 1) {
                                    throw new IllegalStateException("Expected "
                                            + done + " but got " + response);
                                }
                                ping();
                            }
                        }, done);
            } else {
                responseProcessor.processResponse(done);
            }
        }

        /** Process the hammer request. */
        @Override
        public void processRequest(
                final ResponseProcessor<Integer> _responseProcessor)
                throws Exception {
            done = 0;
            responseProcessor = _responseProcessor;
            // Send first ping, to get the "virtual loop" going
            ping();
        }
    }

    /** Creates a Pinger, with it's own mailbox and name. */
    public PActorNonBlockingPinger(final Mailbox mbox) {
        mailbox = mbox;
    }

    /** Tells the pinger to hammer the Ponger. */
    public Integer hammer(final PActorNonBlockingPonger ponger, final int count)
            throws Exception {
        return new HammerRequest(getMailbox(), ponger, count).call();
    }

    /**
     * @return the mailbox
     */
    public Mailbox getMailbox() {
        return mailbox;
    }
}
