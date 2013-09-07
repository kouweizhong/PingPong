package com.blockwithme.pingpong.throughput.jactor2;

import org.agilewiki.jactor2.core.ActorBase;
import org.agilewiki.jactor2.core.messaging.ResponseProcessor;

/**
 * Test code.
 */
public class JActor2Sender extends ActorBase implements
        JActor2RealRequestReceiver {

    private final JActor2SimpleRequestReceiver echo;
    private final int count;
    private final int burst;

    public JActor2Sender(final JActor2SimpleRequestReceiver echo,
            final int _count, final int _buffer) {
        this.echo = echo;
//        echo.setInitialBufferCapacity(b + 10);
        count = _count;
        burst = _buffer;
    }

    @Override
    public void processRequest(final JActor2RealRequest unwrappedRequest,
            final ResponseProcessor rd1) throws Exception {
        final boolean real = unwrappedRequest != null;
//        (new JAIterator() {
//            int i;
//
//            @Override
//            public void process(final ResponseProcessor rd2) throws Exception {
//                if (i > count)
//                    rd2.processResponse(this);
//                else {
//                    i += 1;
//                    final ResponseProcessor rd3 = new ResponseProcessor() {
//                        int r = burst;
//
//                        @Override
//                        public void processResponse(
//                                final Object unwrappedResponse)
//                                throws Exception {
//                            r -= 1;
//                            if (r == 0) {
//                                rd2.processResponse(null);
//                            }
//                        }
//                    };
//                    int j = 0;
//                    while (j < burst) {
//                        new JActor2SimpleRequest(echo.getMessageProcessor(),
//                                echo).send(getMessageProcessor(), rd3);
//                        j += 1;
//                    }
//                }
//            }
//        }).iterate(rd1);
    }
}
