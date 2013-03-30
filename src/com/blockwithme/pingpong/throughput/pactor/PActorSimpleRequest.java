package com.blockwithme.pingpong.throughput.pactor;

import org.agilewiki.pactor.Mailbox;
import org.agilewiki.pactor.RequestBase;
import org.agilewiki.pactor.ResponseProcessor;

public class PActorSimpleRequest extends RequestBase<Object> {

    private final PActorSimpleRequestReceiver target;

    public PActorSimpleRequest(final Mailbox targetMailbox,
            final PActorSimpleRequestReceiver _target) {
        super(targetMailbox);
        target = _target;
    }

    @Override
    public void processRequest(final ResponseProcessor<Object> responseProcessor)
            throws Exception {
        target.processRequest(this, responseProcessor);
    }
}
