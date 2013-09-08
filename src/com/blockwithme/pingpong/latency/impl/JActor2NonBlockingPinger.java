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
 * The Pinger's job is to hammer the Ponger with ping() request.
 * This impl uses JActor2, and generates request while processing reply, to eliminate blocking.
 */
public class JActor2NonBlockingPinger {

    private volatile String STATE = "Pinger() CREATED";

    /** The Pinger's mailbox. */
    private final MessageProcessor mailbox;

    /** A Hammer request, targeted at Pinger. */
    private class HammerRequest extends AsyncRequest<Integer> {

        private volatile String STATE = "HammerRequest() CREATED";

        /** The Ponger to hammer. */
        private final JActor2NonBlockingPonger ponger;

        /** The number of exchanges to do. */
        private final int count;

        /** The number of pings sent. */
        private int done;

        /** Creates a hammer request, with the targeted Ponger. */
        public HammerRequest(final MessageProcessor mbox,
                final JActor2NonBlockingPonger _ponger, final int _count) {
            super(mbox);
            ponger = _ponger;
            count = _count;
            STATE = "HammerRequest(" + count + ") CREATED";
        }

        /** Send a ping, unless we're done, in which case tell caller we are done. */
        private void ping() throws Exception {
            done++;
            if (done < count) {
                STATE = "HammerRequest(" + count + ") GOT PING. done=" + done
                        + " PINGING SOME MORE ...";
                ponger.ping(JActor2NonBlockingPinger.this,
                        new AsyncResponseProcessor<Integer>() {
                            @Override
                            public void processAsyncResponse(
                                    final Integer response) throws Exception {
                                if (response.intValue() != done + 1) {
                                    STATE = "HammerRequest(" + count
                                            + ") GOT RESPONSE " + response
                                            + " BUT EXPECTED " + (done + 1)
                                            + " FAILING!";
                                    throw new IllegalStateException("Expected "
                                            + done + " but got " + response);
                                }
                                STATE = "HammerRequest(" + count
                                        + ") GOT RESPONSE " + response
                                        + " ONE MORE PING ...";
                                ping();
                            }
                        }, done);
            } else {
                STATE = "HammerRequest(" + count + ") GOT PING. done=" + done
                        + " SENDING RESPONSE ...";
                processAsyncResponse(done);
                STATE = "HammerRequest(" + count + ") GOT PING. done=" + done
                        + " RESPONSE SENT";
            }
        }

        /** Process the hammer request. */
        @Override
        public void processAsyncRequest() throws Exception {
            STATE = "HammerRequest(" + count
                    + ") GOT processRequest(). STARTING ping()s ...";
            done = 0;
            // Send first ping, to get the "virtual loop" going
            ping();
        }
    }

    /** Creates a Pinger, with it's own mailbox and name. */
    public JActor2NonBlockingPinger(final MessageProcessor mbox) {
        mailbox = mbox;
    }

    /** Tells the pinger to hammer the Ponger. */
    public Integer hammer(final JActor2NonBlockingPonger ponger, final int count)
            throws Exception {
        final HammerRequest hammer = new HammerRequest(getMailbox(), ponger,
                count);
        STATE = "HammerRequest() BEFORE HammerRequest(" + count + ").call()";
        final Integer result = hammer.call();
        STATE = "HammerRequest() AFTER HammerRequest(" + count + ").call()";
        return result;
    }

    /**
     * @return the mailbox
     */
    public MessageProcessor getMailbox() {
        return mailbox;
    }
}
