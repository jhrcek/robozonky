/*
 * Copyright 2019 The RoboZonky Project
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

package com.github.robozonky.app.configuration;

import java.util.function.Supplier;

import com.github.robozonky.api.SessionInfo;
import com.github.robozonky.app.daemon.DaemonInvestmentMode;
import com.github.robozonky.app.events.Events;
import com.github.robozonky.app.events.SessionEvents;
import com.github.robozonky.app.runtime.Lifecycle;
import com.github.robozonky.app.tenant.PowerTenant;
import com.github.robozonky.app.tenant.TenantBuilder;
import com.github.robozonky.internal.extensions.ListenerServiceLoader;
import com.github.robozonky.internal.secrets.SecretProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class OperatingMode {

    private static final Logger LOGGER = LogManager.getLogger(OperatingMode.class);

    private final Supplier<Lifecycle> lifecycle;

    OperatingMode(final Supplier<Lifecycle> lifecycle) {
        this.lifecycle = lifecycle;
    }

    private static PowerTenant getTenant(final CommandLine cli, final Supplier<Lifecycle> lifecycle,
                                         final SecretProvider secrets) {
        final TenantBuilder b = new TenantBuilder();
        if (cli.isDryRunEnabled()) {
            LOGGER.info("RoboZonky is doing a dry run. It will not invest any real money.");
            b.dryRun();
        }
        return b.withSecrets(secrets)
                .withStrategy(cli.getStrategyLocation())
                .withAvailabilityFrom(lifecycle)
                .named(cli.getName())
                .build();
    }

    private static void configureNotifications(final CommandLine cli, final PowerTenant tenant) {
        // unregister if registered
        final SessionInfo session = tenant.getSessionInfo();
        ListenerServiceLoader.unregisterConfiguration(session);
        // register if needed
        cli.getNotificationConfigLocation().ifPresent(cfg -> ListenerServiceLoader.registerConfiguration(session, cfg));
        // create event handler for this session, otherwise session-less notifications will not be sent
        final SessionEvents e = Events.forSession(tenant);
        LOGGER.debug("Notification subsystem initialized: {}.", e);
    }

    public InvestmentMode configure(final CommandLine cli, final SecretProvider secrets) {
        final PowerTenant tenant = getTenant(cli, lifecycle, secrets);
        configureNotifications(cli, tenant);
        // and now initialize the chosen mode of operation
        return new DaemonInvestmentMode(t -> lifecycle.get().resumeToFail(t), tenant
        );
    }
}
