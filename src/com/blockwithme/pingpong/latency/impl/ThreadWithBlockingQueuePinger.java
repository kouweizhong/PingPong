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

import java.util.concurrent.Semaphore;

/**
 * The Pinger's job is to hammer the Ponger with ping() request.
 * Implemented using independent threads with blocking queues.
 */
public class ThreadWithBlockingQueuePinger extends ActorThreadWithBlockingQueue {
    /** A Hammer request, targeted at Pinger. */
    private static class HammerRequest {
        /** The Ponger to hammer. */
        private final ThreadWithBlockingQueuePonger ponger;

        /** The number of exchanges to do. */
        private final int count;

        /** Creates a hammer request, with the targeted Ponger.
         * @param sem */
        public HammerRequest(final ThreadWithBlockingQueuePonger _ponger,
                final int _count) {
            ponger = _ponger;
            count = _count;
        }

        /** Process the hammer request. */
        public void processRequest(final ThreadWithBlockingQueuePinger pinger)
                throws Exception {
            pinger.count = count;
            pinger.ponger = ponger;
            // Sends the first ping
            ponger.ping(0, pinger);
        }
    }

    /** Number of replies received. */
    private int pongs;

    /** The Ponger to hammer. */
    private ThreadWithBlockingQueuePonger ponger;

    /** The number of exchanges to do. */
    private int count;

    /** So we can tell the non-actor caller when we are done. */
    private final Semaphore sem = new Semaphore(0);

    /** Reacts to PongReply
     * @throws Exception */
    private void onReply(final ThreadWithBlockingQueuePonger.PongReply reply)
            throws Exception {
        pongs++;
        if (reply.output != pongs) {
            throw new IllegalStateException("Expected " + pongs + " but got "
                    + reply.output);
        }
        if (pongs < count) {
            ponger.ping(pongs, this);
        } else {
            sem.release();
        }
    }

    /** Tells the pinger to hammer the Ponger. Blocks and returns the result. */
    public Integer hammer(final ThreadWithBlockingQueuePonger ponger,
            final int _count) throws Exception {
        queueMessage(new HammerRequest(ponger, _count), this);
        sem.acquire();
        return pongs;
    }

    /** Process all incoming messages, including replies. */
    @Override
    protected void processMessage(final Object message,
            final ActorThreadWithBlockingQueue sender) throws Exception {
        if (message instanceof HammerRequest) {
            final HammerRequest req = (HammerRequest) message;
            req.processRequest(this);
        } else if (message instanceof ThreadWithBlockingQueuePonger.PongReply) {
            final ThreadWithBlockingQueuePonger.PongReply reply = (ThreadWithBlockingQueuePonger.PongReply) message;
            onReply(reply);
        } else {
            unhandled(message);
        }
    }

}
