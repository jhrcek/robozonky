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
import java.time.OffsetDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.robozonky.app.ShutdownHook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

class LifecycleTest {

    @Test
    void failing() {
        final Throwable t = new IllegalStateException("Testing exception.");
        final CountDownLatch c = new CountDownLatch(1);
        final Lifecycle h = new Lifecycle(c);
        Assertions.assertTimeout(Duration.ofSeconds(1), () -> h.resumeToFail(t));
        assertSoftly(softly -> {
            softly.assertThat(h.getTerminationCause()).contains(t);
            softly.assertThat(c.getCount()).isEqualTo(0);
        });
    }

    @Test
    void waitUntilOnline() throws ExecutionException, InterruptedException, TimeoutException {
        final ExecutorService e = Executors.newCachedThreadPool();
        try {
            final MainControl c = new MainControl();
            final Future<Boolean> f = e.submit(() -> Lifecycle.waitUntilOnline(c));
            Assertions.assertThrows(TimeoutException.class, () -> f.get(1, TimeUnit.SECONDS)); // we are blocked
            c.valueSet(new ApiVersion("", "", "", OffsetDateTime.now(), "", ""));
            assertThat(f.get(1, TimeUnit.SECONDS)).isTrue(); // this will return now
            assertThat(f).isDone();
        } finally {
            e.shutdownNow();
        }
    }

    @Test
    void waitUntilOnlineInterrupted() {
        final ExecutorService e = Executors.newCachedThreadPool();
        try {
            final MainControl c = new MainControl();
            final Future<Boolean> f = e.submit(() -> {
                final boolean result = Lifecycle.waitUntilOnline(c);
                assertThat(result).isFalse();
                return result;
            });
            Assertions.assertThrows(TimeoutException.class, () -> f.get(1, TimeUnit.SECONDS)); // we are blocked
            f.cancel(true); // finish the runnable, checking the included assertion
        } finally {
            e.shutdownNow();
        }
    }

    @Test
    void create() {
        final Lifecycle h = new Lifecycle();
        assertSoftly(softly -> {
            softly.assertThat(h.getZonkyApiVersion()).isEmpty();
            softly.assertThat(h.getTerminationCause()).isEmpty();
            softly.assertThat(h.getShutdownHooks())
                    .hasSize(2)
                    .hasOnlyElementsOfType(ShutdownHook.Handler.class);
        });
    }
}
