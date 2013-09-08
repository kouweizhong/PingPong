/*
 * Copyright 2011 Bill La Forge
 *
 * This file is part of AgileWiki and is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (LGPL) as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * or navigate to the following url http://www.gnu.org/licenses/lgpl-2.1.txt
 *
 * Note however that only Scala, Java and JavaScript files are being covered by LGPL.
 * All other files are covered by the Common Public License (CPL).
 * A copy of this license is also included and can be
 * found as well at http://www.opensource.org/licenses/cpl1.0.txt
 */
package com.blockwithme.pingpong.throughput.jactor2;

import org.agilewiki.jactor2.core.ActorBase;
import org.agilewiki.jactor2.core.messaging.AsyncRequest;
import org.agilewiki.jactor2.core.messaging.AsyncResponseProcessor;
import org.agilewiki.jactor2.core.messaging.ResponseCounter;
import org.agilewiki.jactor2.core.processing.MessageProcessor;

/**
 * Supports parallel request processing.
 */
final public class JActor2Parallel extends ActorBase implements
        JActor2RealRequestReceiver {
    /**
     * The actors to be run in parallel.
     */
    public JActor2RealRequestReceiver[] actors;

    /**
     * Returns a response only when the expected number of responses are received.
     */
    private ResponseCounter responseCounter;

    public void runParallel(final AsyncRequest[] requests,
            final AsyncResponseProcessor rd1) throws Exception {
        final int p = actors.length;
        responseCounter = new ResponseCounter(p, (Object) null, rd1);
        int i = 0;

        if (requests.length != p)
            throw new IllegalArgumentException(
                    "Request and actor arrays not the same length");
        final MessageProcessor mb = getMessageProcessor();
        while (i < p) {
            requests[i].send(mb, responseCounter);
            i += 1;
        }
    }

    @Override
    public void processRequest(final JActor2RealRequest request,
            final AsyncResponseProcessor rp) throws Exception {
        final int p = actors.length;
        responseCounter = new ResponseCounter(p, (Object) null, rp);
        int i = 0;

        final MessageProcessor mb = getMessageProcessor();
        while (i < p) {
//            request.send(mb, actors[i], responseCounter);
            i += 1;
        }
    }
}
