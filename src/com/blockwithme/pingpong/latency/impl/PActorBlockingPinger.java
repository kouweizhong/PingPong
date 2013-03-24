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
 * Implemented with PActor, blocks on ping() using pend().
 */
public class PActorBlockingPinger {
    /** The Pinger's mailbox. */
    private final Mailbox mailbox;

    /** A Hammer request, targeted at Pinger. */
    private class HammerRequest extends RequestBase<Integer> {
        /** The Ponger to hammer. */
        private final PActorBlockingPonger ponger;

        /** The number of exchanges to do. */
        private final int count;

        /** Creates a hammer request, with the targeted Ponger. */
        public HammerRequest(final Mailbox mbox,
                final PActorBlockingPonger _ponger, final int _count) {
            super(mbox);
            ponger = _ponger;
            count = _count;
        }

        /** Process the hammer request. */
        @Override
        public void processRequest(
                final ResponseProcessor<Integer> responseProcessor)
                throws Exception {
            int done = 0;
            while (done < count) {
                final Integer response = ponger.ping(done);
                done++;
                if (response.intValue() != done) {
                    throw new IllegalStateException("Expected " + done
                            + " but got " + response);
                }
            }
            responseProcessor.processResponse(done);
        }
    }

    /** Creates a Pinger, with it's own mailbox and name. */
    public PActorBlockingPinger(final Mailbox mbox) {
        mailbox = mbox;
    }

    /** Tells the pinger to hammer the Ponger. */
    public Integer hammer(final PActorBlockingPonger ponger, final int count)
            throws Exception {
        return new HammerRequest(mailbox, ponger, count).pend();
    }
}
