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

package com.github.robozonky.notifications.email;

import java.time.OffsetDateTime;

import com.github.robozonky.api.SessionInfo;
import com.github.robozonky.api.remote.entities.sanitized.Investment;
import com.github.robozonky.common.state.TenantState;
import com.github.robozonky.internal.api.Defaults;

enum DelinquencyTracker {

    INSTANCE; // fast thread-safe singleton

    private static String toId(final Investment investment) {
        return String.valueOf(investment.getLoanId());
    }

    public void setDelinquent(final SessionInfo sessionInfo, final Investment investment) {
        if (this.isDelinquent(sessionInfo, investment)) {
            return;
        }
        TenantState.of(sessionInfo)
                .in(DelinquencyTracker.class)
                .reset(b -> b.put(toId(investment), OffsetDateTime.now(Defaults.ZONE_ID).toString()));
    }

    public void unsetDelinquent(final SessionInfo sessionInfo, final Investment investment) {
        if (!this.isDelinquent(sessionInfo, investment)) {
            return;
        }
        TenantState.of(sessionInfo)
                .in(DelinquencyTracker.class)
                .reset(b -> b.remove(toId(investment)));
    }

    public boolean isDelinquent(final SessionInfo sessionInfo, final Investment investment) {
        return TenantState.of(sessionInfo)
                .in(DelinquencyTracker.class)
                .getValue(toId(investment))
                .isPresent();
    }

}
