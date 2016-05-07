/*
 *
 *  * Copyright 2016 Lukáš Petrovický
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 * /
 */

package net.petrovicky.zonkybot.app;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ZonkyBotIT {

    @Test
    public void testGetZonkyBotVersion() {
        final String version = ZonkyBot.getZonkyBotVersion();
        Assertions.assertThat(version).isNotEqualTo(ZonkyBot.ZONKY_VERSION_UNDETECTED);
        Assertions.assertThat(version).isNotEqualTo(ZonkyBot.ZONKY_VERSION_UNKNOWN);
    }

}