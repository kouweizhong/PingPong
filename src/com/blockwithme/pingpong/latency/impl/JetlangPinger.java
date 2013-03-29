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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.jetlang.channels.AsyncRequest;
import org.jetlang.channels.MemoryRequestChannel;
import org.jetlang.channels.Request;
import org.jetlang.core.Callback;
import org.jetlang.fibers.Fiber;

/**
 * The Pinger's job is to hammer the Ponger with ping() request.
 * Implemented with Jetlang.
 */
public class JetlangPinger {
    /** A Hammer request, targeted at Pinger. */
    private static class HammerRequest {
        /** The Ponger to hammer. */
        private final JetlangPonger ponger;

        /** The number of exchanges to do. */
        private final int count;

        /** Creates a hammer request, with the targeted Ponger. */
        public HammerRequest(final JetlangPonger _ponger, final int _count) {
            ponger = _ponger;
            count = _count;
        }

        /** Process the hammer request. */
        public void processRequest(final JetlangPinger pinger) throws Exception {
            pinger.count = count;
            pinger.ponger = ponger;
        }
    }

    /** The jetlang fiber. */
    private final Fiber fiber;

    /** Our channel */
    @SuppressWarnings("rawtypes")
    private final MemoryRequestChannel channel;

    /** Number of replies received. */
    private int pongs;

    /** The number of exchanges to do. */
    private int count;

    /** The Ponger to hammer. */
    private JetlangPonger ponger;

    /** Creates a JetlangPinger. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public JetlangPinger(final Fiber _fiber) {
        fiber = _fiber;
        channel = new MemoryRequestChannel();
        final Callback<Request> onReq = new Callback<Request>() {
            @Override
            public void onMessage(final Request message) {
                final Object request = message.getRequest();
                if (request instanceof HammerRequest) {
                    final HammerRequest req = (HammerRequest) request;
                    try {
                        req.processRequest(JetlangPinger.this);
                        ping(message);
                    } catch (final Exception e) {
                        e.printStackTrace();
                        message.reply(-1);
                    }
                } else {
                    throw new IllegalStateException(
                            "Expected HammerRequest but got "
                                    + request.getClass());
                }
            }
        };
        fiber.start();
        channel.subscribe(fiber, onReq);
    }

    /** Send a ping, unless we're done, in which case tell caller we are done. */
    private void ping(final Request hammerRequest) throws Exception {
        final Callback<Integer> onReply = new Callback<Integer>() {
            @Override
            public void onMessage(final Integer response) {
                pongs++;
                if (response.intValue() != pongs) {
                    throw new IllegalStateException("Expected " + pongs
                            + " but got " + response);
                }
                if (pongs < count) {
                    try {
                        ping(hammerRequest);
                    } catch (final Exception e) {
                        e.printStackTrace();
                        hammerRequest.reply(pongs);
                    }
                } else {
                    hammerRequest.reply(pongs);
                }
            }
        };
        ponger.ping(pongs, onReply);
    }

    /** Tells the pinger to hammer the Ponger. Blocks and returns the result. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Integer hammer(final JetlangPonger ponger, final int _count)
            throws Exception {
        final AtomicReference response = new AtomicReference();
        final HammerRequest req = new HammerRequest(ponger, _count);
        final CountDownLatch done = new CountDownLatch(1);
        final Callback onReply = new Callback() {
            @Override
            public void onMessage(final Object message) {
                // TODO Can we safely access the pinger's state from here?!?
                response.set(message);
                done.countDown();
            }
        };
        AsyncRequest.withOneReply(fiber, channel, req, onReply);
        done.await(60, TimeUnit.SECONDS);
        return (Integer) response.get();
    }
}
