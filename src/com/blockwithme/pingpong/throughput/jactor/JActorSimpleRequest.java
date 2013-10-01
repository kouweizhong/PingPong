package com.blockwithme.pingpong.throughput.jactor;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

public class JActorSimpleRequest extends Request<Object, JActorSimpleRequestReceiver> {
    public final static JActorSimpleRequest req = new JActorSimpleRequest();

    @Override
    public boolean isTargetType(final Actor targetActor) {
        return targetActor instanceof JActorSimpleRequestReceiver;
    }

    @Override
    public void processRequest(final JLPCActor targetActor, final RP rp)
            throws Exception {
        final JActorSimpleRequestReceiver smDriver = (JActorSimpleRequestReceiver) targetActor;
        smDriver.processRequest(this, rp);
    }
}
