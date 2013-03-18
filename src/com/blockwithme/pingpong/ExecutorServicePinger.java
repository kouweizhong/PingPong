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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

/**
 * The Pinger's job is to hammer the Ponger with ping() request.
 * Implemented using an ExecutorService.
 */
public class ExecutorServicePinger extends ExecutorServiceActor {
    /** A Hammer request, targeted at Pinger. */
    private static class HammerRequest {
        /** The Ponger to hammer. */
        private final ExecutorServicePonger ponger;

        /** The number of exchanges to do. */
        private final int count;

        /** Creates a hammer request, with the targeted Ponger.
         * @param sem */
        public HammerRequest(final ExecutorServicePonger _ponger,
                final int _count) {
            ponger = _ponger;
            count = _count;
        }

        /** Process the hammer request. */
        public void processRequest(final ExecutorServicePinger pinger)
                throws Exception {
            pinger.count = count;
            pinger.ponger = ponger;
            // Sends the first ping
            ponger.ping(pinger.toString(), pinger);
        }
    }

    /** Number of replies received. */
    private int pongs;

    /** The Ponger to hammer. */
    private ExecutorServicePonger ponger;

    /** The number of exchanges to do. */
    private int count;

    /** A Semaphore so we can tell the main() when we are done. */
    private final Semaphore sem = new Semaphore(0);

    /** Reacts to reply
     * @throws Exception */
    private void onReply(final ExecutorServicePonger.PongReply reply)
            throws Exception {
        pongs++;
        if (pongs < count) {
            ponger.ping(this.toString(), this);
        } else {
            sem.release();
        }
    }

    /** Constructor */
    public ExecutorServicePinger(final ExecutorService _executorService) {
        super(_executorService);
    }

    /** Tells the pinger to hammer the Ponger. Blocks and returns the result. */
    public String hammer(final ExecutorServicePonger ponger, final int _count)
            throws Exception {
        queueMessage(new HammerRequest(ponger, _count), this);
        sem.acquire();
        return "done";
    }

    /** Process all incoming messages, including responses. */
    @Override
    protected void processMessage(final Object message,
            final ExecutorServiceActor sender) throws Exception {
        if (message instanceof HammerRequest) {
            final HammerRequest req = (HammerRequest) message;
            req.processRequest(this);
        } else if (message instanceof ExecutorServicePonger.PongReply) {
            final ExecutorServicePonger.PongReply reply = (ExecutorServicePonger.PongReply) message;
            onReply(reply);
        } else {
            unhandled(message);
        }
    }

}
