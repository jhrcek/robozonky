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

package com.github.robozonky.strategy.natural;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The purpose of this class is so that all possible reasons for a strategy to reject/accept a loan are properly logged
 * using the same logger. This will help find these messages in the usually very long logs.
 */
class Decisions {

    private static final Logger LOGGER = LoggerFactory.getLogger(Decisions.class);

    public static void report(final Consumer<Logger> log) {
        log.accept(LOGGER);
    }
}
