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

import kilim.Mailbox;
import kilim.Pausable;
import kilim.Task;

/**
 * The Pinger's job is to hammer the Ponger with ping() request.
 * Implemented using Kilim Tasks.
 */
public class KilimPinger extends Task {
    /** A Hammer request, targeted at Pinger. */
    private static class HammerRequest {
        /** The number of exchanges to do. */
        private final int count;

        /** The request result will be returned to that Mailbox. */
        private final Mailbox<Integer> callerMB;

        /** Creates a hammer request, with the targeted Ponger. */
        public HammerRequest(final int _count, final Mailbox<Integer> _callerMB) {
            count = _count;
            callerMB = _callerMB;
        }

        /** Process the hammer request. */
        public void processRequest(final KilimPinger pinger) throws Exception {
            pinger.count = count;
            pinger.callerMB = callerMB;
        }
    }

    private final Mailbox<Object> pingerMB;
    private final KilimPonger ponger;
    private Mailbox<Integer> callerMB;

    /** Number of replies received. */
    private int pongs;

    /** The number of exchanges to do. */
    private int count;

    public KilimPinger(final Mailbox<Object> _pingerMB,
            final KilimPonger _ponger) {
        pingerMB = _pingerMB;
        ponger = _ponger;
    }

    @Override
    public void execute() throws Pausable {
        while (true) {
            final Object msg = pingerMB.get();
            if (msg instanceof HammerRequest) {
                final HammerRequest req = (HammerRequest) msg;
                try {
                    req.processRequest(this);
                    // Sends the first ping
                    ponger.ping(0);
                } catch (final Exception e) {
                    e.printStackTrace();
                    return;
                }
            } else if (msg instanceof Integer) {
                final Integer response = (Integer) msg;
                pongs++;
                if (response.intValue() != pongs) {
                    throw new IllegalStateException("Expected " + pongs
                            + " but got " + response);
                }
                if (pongs < count) {
                    try {
                        ponger.ping(pongs);
                    } catch (final Exception e) {
                        e.printStackTrace();
                        return;
                    }
                } else {
                    callerMB.put(pongs);
                }
            } else {
                new Exception("Expceted HammerRequest but got "
                        + msg.getClass()).printStackTrace();
                return;
            }
        }
    }

    /** Tells the pinger to hammer the Ponger. Blocks and returns the result. */
    public Integer hammer(final int _count) throws Exception {
        final Mailbox<Integer> callerMB = new Mailbox<Integer>();
        pingerMB.put(new HammerRequest(_count, callerMB));
        return callerMB.get();
    }
}
