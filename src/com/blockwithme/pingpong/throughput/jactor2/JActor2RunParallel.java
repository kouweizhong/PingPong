package com.blockwithme.pingpong.throughput.jactor2;

import org.agilewiki.jactor2.core.messaging.Request;
import org.agilewiki.jactor2.core.processing.MessageProcessor;

/**
 * A wrapper of an array of requests to be sent to multiple actors.
 */
public class JActor2RunParallel extends Request<Object> {
    /**
     * The wrapped requests;
     */
    private final Request[] requests;

    private final JActor2Parallel targetActor;

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
    public JActor2RunParallel(final MessageProcessor targetMailbox,
            final JActor2Parallel _targetActor, final Request[] requests) {
        super(targetMailbox);
        this.requests = requests;
        targetActor = _targetActor;
    }

    @Override
    public void processRequest() throws Exception {
        targetActor.runParallel(requests, this);
    }
}
