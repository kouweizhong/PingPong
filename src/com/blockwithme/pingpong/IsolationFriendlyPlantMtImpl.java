/*
 * Copyright (C) 2014 Sebastien Diot.
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

import org.agilewiki.jactor2.core.impl.mtPlant.PlantConfiguration;
import org.agilewiki.jactor2.core.impl.mtPlant.PlantMtImpl;
import org.agilewiki.jactor2.core.impl.mtReactors.IsolationReactorMtImpl;
import org.agilewiki.jactor2.core.reactors.NonBlockingReactor;
import org.agilewiki.jactor2.core.reactors.impl.ReactorImpl;

/**
 * @author monster
 *
 */
public class IsolationFriendlyPlantMtImpl extends PlantMtImpl {
    /**
     * @param _threadCount
     * @throws Exception
     */
    public IsolationFriendlyPlantMtImpl(final int _threadCount)
            throws Exception {
        super(_threadCount);
    }

    /**
     * Create the singleton with the given configuration.
     *
     * @param _plantConfiguration The configuration to be used by the singleton.
     */
    public IsolationFriendlyPlantMtImpl(
            final PlantConfiguration _plantConfiguration) throws Exception {
        super(_plantConfiguration);
    }

    @Override
    public ReactorImpl createIsolationReactorImpl(
            final NonBlockingReactor _parentReactor,
            final int _initialOutboxSize, final int _initialLocalQueueSize) {
        return new IsolationReactorMtImpl(_parentReactor, _initialOutboxSize,
                _initialLocalQueueSize) {
            @Override
            public boolean isCommonReactor() {
                return true;
            }
        };
    }
}
