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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * Hand-written simple actor based on using an ExecutorService.
 */
public abstract class ExecutorServiceActor implements Runnable {

    /** Associate a message with it's sender. */
    private static final class Msg {
        Object msg;
        ExecutorServiceActor sender;
    }

    /** The message queue */
    private final ArrayBlockingQueue<Msg> messages = new ArrayBlockingQueue<Msg>(
            10);

    /** Our ExecutorService */
    private final ExecutorService executorService;

    /**
     * Is this Actor Dead? We need this, because since we are not a Thread,
     * we don't have a state that represents being dead, as a simple Runnable.
     */
    private volatile boolean dead;

    /** Thread I'm running on? */
    private volatile Thread thread;

    /** Constructor */
    protected ExecutorServiceActor(final ExecutorService _executorService) {
        executorService = _executorService;
    }

    /**
     * run() processes exactly one message, unless it is already dead, then no
     * message is processed.
     */
    @Override
    public final void run() {
        if (!dead) {
            try {
                thread = Thread.currentThread();
                final Msg msg;
                try {
                    msg = messages.take();
                } finally {
                    thread = null;
                }
                try {
                    processMessage(msg.msg, msg.sender);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            } catch (final InterruptedException e1) {
                dead = true;
            }
        }
    }

    /** Enques a message to this actor. */
    public final void queueMessage(final Object message,
            final ExecutorServiceActor sender) throws InterruptedException {
        if (dead) {
            throw new InterruptedException();
        }
        final Msg msg = new Msg();
        msg.msg = message;
        msg.sender = sender;
        messages.put(msg);
        executorService.execute(this);
    }

    /** Terminates this actor. */
    public final void kill() {
        if (!dead) {
            dead = true;
            // This is bad, unsafe code, but it'll do for this test.
            final Thread t = thread;
            if (t != null) {
                t.interrupt();
            }
        }
    }

    /** Is the actor dead? */
    public final boolean dead() {
        return dead;
    }

    /** Reacts to unhandled messages. */
    protected final void unhandled(final Object unhandled) {
        System.out.println("Unhandled message: " + unhandled);
        Thread.dumpStack();
    }

    /** Must be implemented by the actor to react to all incoming messages, including replies. */
    protected abstract void processMessage(final Object message,
            final ExecutorServiceActor sender) throws Exception;
}
