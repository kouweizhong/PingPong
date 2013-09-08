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
import org.agilewiki.jactor2.core.processing.MessageProcessor;

/**
 * The Pinger's job is to hammer the Ponger with ping() request.
 * Implemented with JActor2, blocks on ping() using pend().
 */
public class JActor2BlockingPinger {
    /** The Pinger's mailbox. */
    private final MessageProcessor mailbox;

    /** A Hammer request, targeted at Pinger. */
    private class HammerRequest extends AsyncRequest<Integer> {
        /** The Ponger to hammer. */
        private final JActor2BlockingPonger ponger;

        /** The number of exchanges to do. */
        private final int count;

        /** Creates a hammer request, with the targeted Ponger. */
        public HammerRequest(final MessageProcessor mbox,
                final JActor2BlockingPonger _ponger, final int _count) {
            super(mbox);
            ponger = _ponger;
            count = _count;
        }

        /** Process the hammer request. */
        @Override
        public void processAsyncRequest() throws Exception {
            int done = 0;
            while (done < count) {
                final Integer response = ponger.ping(done);
                done++;
                if (response.intValue() != done) {
                    throw new IllegalStateException("Expected " + done
                            + " but got " + response);
                }
            }
            processAsyncResponse(done);
        }
    }

    /** Creates a Pinger, with it's own mailbox and name. */
    public JActor2BlockingPinger(final MessageProcessor mbox) {
        mailbox = mbox;
    }

    /** Tells the pinger to hammer the Ponger. */
    public Integer hammer(final JActor2BlockingPonger ponger, final int count)
            throws Exception {
        return new HammerRequest(mailbox, ponger, count).call();
    }
}
