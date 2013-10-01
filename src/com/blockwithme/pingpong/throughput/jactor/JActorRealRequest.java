package com.blockwithme.pingpong.throughput.jactor;

import org.agilewiki.jactor.Actor;
import org.agilewiki.jactor.RP;
import org.agilewiki.jactor.lpc.JLPCActor;
import org.agilewiki.jactor.lpc.Request;

public class JActorRealRequest extends Request<Object, JActorRealRequestReceiver> {
    public final static JActorRealRequest req = new JActorRealRequest();

    @Override
    public boolean isTargetType(Actor targetActor) {
        return targetActor instanceof JActorRealRequestReceiver;
    }

    @Override
    public void processRequest(JLPCActor targetActor, RP rp) throws Exception {
        JActorRealRequestReceiver smDriver = (JActorRealRequestReceiver) targetActor;
        smDriver.processRequest(this, rp);
    }
}
