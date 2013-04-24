package com.blockwithme.pingpong.throughput.pactor;

import org.agilewiki.pactor.Mailbox;
import org.agilewiki.pactor.RequestBase;
import org.agilewiki.pactor.ResponseProcessor;
import org.agilewiki.pactor.Transport;

public class PActorSimpleRequest extends RequestBase<Object> {

    private final PActorSimpleRequestReceiver target;

    public PActorSimpleRequest(final Mailbox targetMailbox,
            final PActorSimpleRequestReceiver _target) {
        super(targetMailbox);
        target = _target;
    }

    @Override
    public void processRequest(final Transport<Object> responseProcessor)
            throws Exception {
        target.processRequest(this, responseProcessor);
    }
}
