/*
 * Copyright 2018 The RoboZonky Project
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

package com.github.robozonky.app.runtime;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MainControlTest {

    @Test
    void operation() throws InterruptedException, ExecutionException {
        final ExecutorService e = Executors.newFixedThreadPool(1);
        final MainControl mainControl = new MainControl();
        final AtomicBoolean started = new AtomicBoolean(false);
        final Future<?> f = e.submit(() -> {
            started.set(true);
            try {
                mainControl.waitUntilTriggered();
            } catch (final InterruptedException e1) {
                throw new IllegalStateException(e1);
            }
        });
        org.junit.jupiter.api.Assertions.assertTimeout(Duration.ofSeconds(5), () -> {
            while (!started.get()) {
                Thread.sleep(1);
            }
        });
        mainControl.valueUnset(null);
        assertThatThrownBy(() -> f.get(1, TimeUnit.SECONDS))
                .isInstanceOf(TimeoutException.class);  // nothing will happen
        final ApiVersion v = mock(ApiVersion.class);
        mainControl.valueSet(v);
        f.get(); // make sure task finished
        assertThat(mainControl.getApiVersion()).contains(v);
    }
}
