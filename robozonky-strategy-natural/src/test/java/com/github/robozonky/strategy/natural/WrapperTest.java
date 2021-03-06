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

package com.github.robozonky.strategy.natural;

import java.math.BigDecimal;

import com.github.robozonky.api.remote.entities.sanitized.Investment;
import com.github.robozonky.api.remote.entities.sanitized.Loan;
import com.github.robozonky.api.remote.entities.sanitized.MarketplaceLoan;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.SoftAssertions.*;

class WrapperTest {

    @Test
    void fromInvestment() {
        final Loan loan = Loan.custom()
                .setId(1)
                .setAmount(100_000)
                .build();
        final int invested = 200;
        final Investment investment = Investment.fresh((MarketplaceLoan) loan, invested).build();
        final Wrapper w = new Wrapper(investment, loan);
        assertSoftly(softly -> {
            softly.assertThat(w.getLoanId()).isEqualTo(loan.getId());
            softly.assertThat(w.getStory()).isEqualTo(loan.getStory());
            softly.assertThat(w.getRegion()).isEqualTo(loan.getRegion());
            softly.assertThat(w.getRating()).isEqualTo(loan.getRating());
            softly.assertThat(w.getOriginalAmount()).isEqualTo(loan.getAmount());
            softly.assertThat(w.getInterestRate()).isEqualTo(loan.getInterestRate());
            softly.assertThat(w.getRemainingTermInMonths()).isEqualTo(investment.getRemainingMonths());
            softly.assertThat(w.getRemainingAmount()).isEqualTo(BigDecimal.valueOf(invested));
            softly.assertThat(w.getIdentifier()).isNotNull();
        });
    }

    @Test
    void equality() {
        final Loan loan = Loan.custom()
                .setId(1)
                .setAmount(2)
                .build();
        final Wrapper w = new Wrapper(loan);
        assertSoftly(softly -> {
            softly.assertThat(w).isEqualTo(w);
            softly.assertThat(w).isNotEqualTo(null);
            softly.assertThat(w).isNotEqualTo("");
        });
        final Wrapper w2 = new Wrapper(loan);
        assertSoftly(softly -> {
            softly.assertThat(w).isEqualTo(w2);
            softly.assertThat(w2).isEqualTo(w);
        });
    }
}
