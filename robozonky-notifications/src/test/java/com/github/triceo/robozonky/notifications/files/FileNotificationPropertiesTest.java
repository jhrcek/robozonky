/*
 * Copyright 2017 Lukáš Petrovický
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

package com.github.triceo.robozonky.notifications.files;

import java.util.Properties;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

public class FileNotificationPropertiesTest {

    @Test
    public void noListenerEnabled() {
        final Properties p = new Properties();
        final FileNotificationProperties properties = new FileNotificationProperties(p);
        SoftAssertions.assertSoftly(softly ->
                Stream.of(SupportedFileListener.values()).forEach(l ->
                        softly.assertThat(properties.isListenerEnabled(l)).isFalse()));
    }

    @Test
    public void allListenersEnabled() {
        final Properties p = new Properties();
        Stream.of(SupportedFileListener.values()).forEach(l -> p.setProperty(l.getLabel() + ".enabled", "true"));
        final FileNotificationProperties properties = new FileNotificationProperties(p);
        SoftAssertions.assertSoftly(softly ->
                Stream.of(SupportedFileListener.values()).forEach(l ->
                        softly.assertThat(properties.isListenerEnabled(l)).isTrue()));
    }

    @Test
    public void noCounter() {
        final Properties p = new Properties();
        final FileNotificationProperties properties = new FileNotificationProperties(p);
        Assertions.assertThat(properties.getGlobalHourlyLimit()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void negativeCounter() {
        final Properties p = new Properties();
        p.setProperty(FileNotificationProperties.HOURLY_LIMIT, "-1");
        final FileNotificationProperties properties = new FileNotificationProperties(p);
        Assertions.assertThat(properties.getGlobalHourlyLimit()).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    public void properCounter() {
        final Properties p = new Properties();
        p.setProperty(FileNotificationProperties.HOURLY_LIMIT, "0");
        final FileNotificationProperties properties = new FileNotificationProperties(p);
        Assertions.assertThat(properties.getGlobalHourlyLimit()).isEqualTo(0);
    }

}