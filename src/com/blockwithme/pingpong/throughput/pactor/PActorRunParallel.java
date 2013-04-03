package com.blockwithme.pingpong.throughput.pactor;

import org.agilewiki.pactor.Mailbox;
import org.agilewiki.pactor.Request;
import org.agilewiki.pactor.RequestBase;
import org.agilewiki.pactor.ResponseProcessor;

/**
 * A wrapper of an array of requests to be sent to multiple actors.
 */
public class PActorRunParallel extends RequestBase<Object> {
    /**
     * The wrapped requests;
     */
    private final Request[] requests;

    private final PActorParallel targetActor;

    /**
     * Returns the wrapped requests.
     *
     * @return The wrapped requests.
     */
    public Request[] getRequests() {
        return requests;
    }

    /**
     * Create the request.
     *
     * @param requests the requests to be run in parallel.
     */
    public PActorRunParallel(final Mailbox targetMailbox,
            final PActorParallel _targetActor, final Request[] requests) {
        super(targetMailbox);
        this.requests = requests;
        targetActor = _targetActor;
    }

    @Override
    public void processRequest(final ResponseProcessor<Object> responseProcessor)
            throws Exception {
        targetActor.runParallel(requests, responseProcessor);
    }
}