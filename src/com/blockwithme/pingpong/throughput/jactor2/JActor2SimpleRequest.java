package com.blockwithme.pingpong.throughput.jactor2;

import org.agilewiki.jactor2.core.messaging.Request;
import org.agilewiki.jactor2.core.processing.MessageProcessor;

public class JActor2SimpleRequest extends Request<Object> {

    private final JActor2SimpleRequestReceiver target;

    public JActor2SimpleRequest(final MessageProcessor targetMailbox,
            final JActor2SimpleRequestReceiver _target) {
        super(targetMailbox);
        target = _target;
    }

    @Override
    public void processRequest() throws Exception {
        target.processRequest(this, this);
    }
}
