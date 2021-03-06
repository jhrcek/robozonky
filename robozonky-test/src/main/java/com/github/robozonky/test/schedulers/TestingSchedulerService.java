/*
 * Copyright 2017 The RoboZonky Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.robozonky.test.schedulers;

import java.util.concurrent.ThreadFactory;

import com.github.robozonky.util.PausableScheduledExecutorService;
import com.github.robozonky.util.SchedulerService;
import org.mockito.Mockito;

public class TestingSchedulerService implements SchedulerService {

    public static final PausableScheduledExecutorService MOCK_SERVICE =
            Mockito.spy(new TestingScheduledExecutorService());

    @Override
    public PausableScheduledExecutorService newScheduledExecutorService(final int parallelism,
                                                                        final ThreadFactory threadFactory) {
        return MOCK_SERVICE;
    }
}
