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

package com.github.robozonky.api.notifications;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import com.github.robozonky.api.remote.entities.RawInvestment;
import com.github.robozonky.api.remote.entities.sanitized.Development;
import com.github.robozonky.api.remote.entities.sanitized.Investment;
import com.github.robozonky.api.remote.entities.sanitized.Loan;

/**
 * Fired immediately after an {@link RawInvestment} is identified as no longer delinquent.
 */
public final class LoanNoLongerDelinquentEvent extends Event implements DelinquencyBased {

    private final Investment investment;
    private final Loan loan;
    private final LocalDate delinquentSince;
    private final Collection<Development> collectionActions;

    public LoanNoLongerDelinquentEvent(final Investment investment, final Loan loan, final LocalDate delinquentSince,
                                       final Collection<Development> collectionActions) {
        this.investment = investment;
        this.loan = loan;
        this.delinquentSince = delinquentSince;
        this.collectionActions = Collections.unmodifiableCollection(collectionActions);
    }

    @Override
    public LocalDate getDelinquentSince() {
        return delinquentSince;
    }

    @Override
    public Collection<Development> getCollectionActions() {
        return collectionActions;
    }

    @Override
    public Loan getLoan() {
        return loan;
    }

    @Override
    public Investment getInvestment() {
        return investment;
    }
}
