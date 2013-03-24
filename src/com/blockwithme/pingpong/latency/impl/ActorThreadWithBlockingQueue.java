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

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Hand-written simple actor based on a single thread.
 * Uses a blocking queue to exchange messages.
 */
public abstract class ActorThreadWithBlockingQueue extends Thread {

    /** We need to associate each message with a sender, to get two-way messaging. */
    private static final class Msg {
        Object msg;
        ActorThreadWithBlockingQueue sender;
    }

    /** The blocking message queue */
    private final ArrayBlockingQueue<Msg> messages = new ArrayBlockingQueue<Msg>(
            10);

    /** Process messages until killed. */
    @Override
    public final void run() {
        while (true) {
            try {
                final Msg msg = messages.take();
                try {
                    processMessage(msg.msg, msg.sender);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            } catch (final InterruptedException e1) {
                return;
            }
        }
    }

    /** Enqueues a message for later processing. */
    public final void queueMessage(final Object message,
            final ActorThreadWithBlockingQueue sender)
            throws InterruptedException {
        final Msg msg = new Msg();
        msg.msg = message;
        msg.sender = sender;
        messages.put(msg);
    }

    /** Terminates this actor. */
    public final void kill() {
        interrupt();
    }

    /** Reacts to unhandled messages. */
    protected final void unhandled(final Object unhandled) {
        System.out.println("Unhandled message: " + unhandled);
        Thread.dumpStack();
    }

    /** Must be implemented by the actor to react to incoming messages. */
    protected abstract void processMessage(final Object message,
            final ActorThreadWithBlockingQueue sender) throws Exception;
}
