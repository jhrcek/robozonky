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

package com.github.robozonky.api.remote.entities.sanitized;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.OptionalInt;

import com.github.robozonky.api.remote.entities.InsurancePolicyPeriod;
import com.github.robozonky.api.remote.entities.RawInvestment;
import com.github.robozonky.api.remote.enums.InvestmentStatus;
import com.github.robozonky.api.remote.enums.PaymentStatus;
import com.github.robozonky.api.remote.enums.Rating;
import com.github.robozonky.internal.api.ToStringBuilder;
import org.eclipse.collections.impl.list.mutable.FastList;

final class MutableInvestmentImpl implements InvestmentBuilder {

    private int loanId, id, currentTerm, originalTerm, remainingMonths;
    private Integer daysPastDue;
    private OffsetDateTime nextPaymentDate, investmentDate;
    private boolean canBeOffered, isOnSmp, isInsuranceActive, areInstalmentsPostponed;
    private Boolean isInWithdrawal;
    private BigDecimal originalPrincipal, interestRate, paidPrincipal, duePrincipal, paidInterest, dueInterest,
            expectedInterest, paidPenalty, remainingPrincipal, smpFee, smpSoldFor;
    private Rating rating;
    private InvestmentStatus status;
    private PaymentStatus paymentStatus;
    private Collection<InsurancePolicyPeriod> insuranceHistory = Collections.emptyList();

    MutableInvestmentImpl() {
    }

    MutableInvestmentImpl(final RawInvestment investment) {
        this.loanId = investment.getLoanId();
        this.id = investment.getId();
        this.currentTerm = investment.getCurrentTerm();
        this.originalTerm = investment.getLoanTermInMonth();
        this.remainingMonths = investment.getRemainingMonths();
        this.daysPastDue = investment.getLegalDpd();
        this.nextPaymentDate = investment.getNextPaymentDate();
        this.canBeOffered = investment.isCanBeOffered();
        this.isOnSmp = investment.isOnSmp();
        this.originalPrincipal = Util.cacheBigDecimal(investment.getPurchasePrice()); // likely to be multiples of 200
        this.interestRate = Util.cacheBigDecimal(investment.getInterestRate()); // only a handful of these exist
        this.paidPrincipal = investment.getPaidPrincipal();
        this.duePrincipal = investment.getDuePrincipal();
        this.paidInterest = investment.getPaidInterest();
        this.dueInterest = investment.getDueInterest();
        this.expectedInterest = investment.getExpectedInterest();
        this.paidPenalty = investment.getPaidPenalty();
        this.remainingPrincipal = investment.getRemainingPrincipal();
        this.smpFee = investment.getSmpFee();
        this.smpSoldFor = investment.getSmpSoldFor();
        this.rating = investment.getRating();
        this.isInWithdrawal = investment.isInWithdrawal();
        this.status = investment.getStatus();
        this.paymentStatus = investment.getPaymentStatus();
        this.isInsuranceActive = investment.isInsuranceActive();
        this.areInstalmentsPostponed = investment.isInstalmentPostponement();
        setInsuranceHistory(investment.getInsuranceHistory());
    }

    // TODO should calculate expected interest somehow
    MutableInvestmentImpl(final MarketplaceLoan loan, final BigDecimal originalPrincipal) {
        loan.getMyInvestment().ifPresent(i -> {
            this.id = i.getId();
            this.investmentDate = i.getTimeCreated();
        });
        this.loanId = loan.getId();
        this.currentTerm = loan.getTermInMonths();
        this.originalTerm = loan.getTermInMonths();
        this.remainingMonths = loan.getTermInMonths();
        this.daysPastDue = 0;
        this.canBeOffered = false;
        this.isOnSmp = false;
        this.originalPrincipal = Util.cacheBigDecimal(originalPrincipal); // likely to be multiples of 200
        this.interestRate = loan.getInterestRate();
        this.paidPrincipal = BigDecimal.ZERO;
        this.duePrincipal = BigDecimal.ZERO;
        this.paidInterest = BigDecimal.ZERO;
        this.dueInterest = BigDecimal.ZERO;
        this.paidPenalty = BigDecimal.ZERO;
        this.remainingPrincipal = originalPrincipal;
        this.rating = loan.getRating();
        this.isInWithdrawal = false;
        this.status = InvestmentStatus.ACTIVE;
        this.paymentStatus = PaymentStatus.NOT_COVERED;
        this.isInsuranceActive = loan.isInsuranceActive();
        this.areInstalmentsPostponed = false;
        this.setInsuranceHistory(loan.getInsuranceHistory());
    }

    @Override
    public InvestmentBuilder setLoanId(final int loanId) {
        this.loanId = loanId;
        return this;
    }

    @Override
    public InvestmentBuilder setAmountInvested(final BigDecimal amountInvested) {
        this.originalPrincipal = amountInvested;
        return this;
    }

    @Override
    public InvestmentBuilder setId(final int id) {
        this.id = id;
        return this;
    }

    @Override
    public InvestmentBuilder setInterestRate(final BigDecimal interestRate) {
        this.interestRate = interestRate;
        return this;
    }

    @Override
    public InvestmentBuilder setRating(final Rating rating) {
        this.rating = rating;
        return this;
    }

    @Override
    public InvestmentBuilder setOriginalTerm(final int originalTerm) {
        this.originalTerm = originalTerm;
        return this;
    }

    @Override
    public InvestmentBuilder setPaidPrincipal(final BigDecimal paidPrincipal) {
        this.paidPrincipal = paidPrincipal;
        return this;
    }

    @Override
    public InvestmentBuilder setDuePrincipal(final BigDecimal duePrincipal) {
        this.duePrincipal = duePrincipal;
        return this;
    }

    @Override
    public InvestmentBuilder setPaidInterest(final BigDecimal paidInterest) {
        this.paidInterest = paidInterest;
        return this;
    }

    @Override
    public InvestmentBuilder setDueInterest(final BigDecimal dueInterest) {
        this.dueInterest = dueInterest;
        return this;
    }

    @Override
    public InvestmentBuilder setExpectedInterest(final BigDecimal expectedInterest) {
        this.expectedInterest = expectedInterest;
        return this;
    }

    @Override
    public InvestmentBuilder setPaidPenalty(final BigDecimal paidPenalty) {
        this.paidPenalty = paidPenalty;
        return this;
    }

    @Override
    public InvestmentBuilder setCurrentTerm(final int currentTerm) {
        this.currentTerm = currentTerm;
        return this;
    }

    @Override
    public InvestmentBuilder setRemainingMonths(final int remainingMonths) {
        this.remainingMonths = remainingMonths;
        return this;
    }

    @Override
    public InvestmentBuilder setDaysPastDue(final int daysPastDue) {
        this.daysPastDue = daysPastDue;
        return this;
    }

    @Override
    public InvestmentBuilder setRemainingPrincipal(final BigDecimal remainingPrincipal) {
        this.remainingPrincipal = remainingPrincipal;
        return this;
    }

    @Override
    public InvestmentBuilder setSmpFee(final BigDecimal smpFee) {
        this.smpFee = smpFee;
        return this;
    }

    @Override
    public InvestmentBuilder setNextPaymentDate(final OffsetDateTime nextPaymentDate) {
        this.nextPaymentDate = nextPaymentDate;
        return this;
    }

    @Override
    public InvestmentBuilder setSmpSoldFor(final BigDecimal smpSoldFor) {
        this.smpSoldFor = smpSoldFor;
        return this;
    }

    @Override
    public InvestmentBuilder setOnSmp(final boolean isOnSmp) {
        this.isOnSmp = isOnSmp;
        return this;
    }

    @Override
    public InvestmentBuilder setOfferable(final boolean canBeOffered) {
        this.canBeOffered = canBeOffered;
        return this;
    }

    @Override
    public InvestmentBuilder setInvestmentDate(final OffsetDateTime investmentDate) {
        this.investmentDate = investmentDate;
        return this;
    }

    @Override
    public InvestmentBuilder setStatus(final InvestmentStatus investmentStatus) {
        this.status = investmentStatus;
        return this;
    }

    @Override
    public InvestmentBuilder setPaymentStatus(final PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
        return this;
    }

    @Override
    public InvestmentBuilder setInWithdrawal(final boolean isInWithdrawal) {
        this.isInWithdrawal = isInWithdrawal;
        return this;
    }

    @Override
    public InvestmentBuilder setInsuranceActive(final boolean insuranceActive) {
        this.isInsuranceActive = insuranceActive;
        return this;
    }

    @Override
    public InvestmentBuilder setInstalmentsPostponed(final boolean instalmentsPostponed) {
        this.areInstalmentsPostponed = instalmentsPostponed;
        return this;
    }

    @Override
    public InvestmentBuilder setInsuranceHistory(final Collection<InsurancePolicyPeriod> insurancePolicyPeriods) {
        final boolean isEmpty = insurancePolicyPeriods == null || insurancePolicyPeriods.isEmpty();
        this.insuranceHistory = isEmpty ? Collections.emptyList() : new FastList<>(insurancePolicyPeriods);
        return this;
    }

    @Override
    public int getLoanId() {
        return loanId;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getCurrentTerm() {
        return currentTerm;
    }

    @Override
    public int getOriginalTerm() {
        return originalTerm;
    }

    @Override
    public int getRemainingMonths() {
        return remainingMonths;
    }

    @Override
    public Optional<OffsetDateTime> getInvestmentDate() {
        return Optional.ofNullable(investmentDate);
    }

    @Override
    public OptionalInt getDaysPastDue() {
        return daysPastDue == null ? OptionalInt.empty() : OptionalInt.of(daysPastDue);
    }

    @Override
    public Optional<OffsetDateTime> getNextPaymentDate() {
        return Optional.ofNullable(nextPaymentDate);
    }

    @Override
    public boolean canBeOffered() {
        return canBeOffered;
    }

    @Override
    public boolean isInsuranceActive() {
        return isInsuranceActive;
    }

    @Override
    public boolean areInstalmentsPostponed() {
        return areInstalmentsPostponed;
    }

    @Override
    public Collection<InsurancePolicyPeriod> getInsuranceHistory() {
        return Collections.unmodifiableCollection(insuranceHistory);
    }

    @Override
    public boolean isOnSmp() {
        return isOnSmp;
    }

    @Override
    public BigDecimal getOriginalPrincipal() {
        return originalPrincipal;
    }

    @Override
    public BigDecimal getInterestRate() {
        return interestRate;
    }

    @Override
    public BigDecimal getPaidPrincipal() {
        return paidPrincipal;
    }

    @Override
    public BigDecimal getDuePrincipal() {
        return duePrincipal;
    }

    @Override
    public BigDecimal getPaidInterest() {
        return paidInterest;
    }

    @Override
    public BigDecimal getDueInterest() {
        return dueInterest;
    }

    @Override
    public BigDecimal getExpectedInterest() {
        return expectedInterest;
    }

    @Override
    public BigDecimal getPaidPenalty() {
        return paidPenalty;
    }

    @Override
    public BigDecimal getRemainingPrincipal() {
        if (remainingPrincipal != null) {
            return remainingPrincipal;
        } else {
            return originalPrincipal.subtract(paidPrincipal);
        }
    }

    @Override
    public Optional<BigDecimal> getSmpFee() {
        return Optional.ofNullable(smpFee);
    }

    @Override
    public Optional<BigDecimal> getSmpSoldFor() {
        return Optional.ofNullable(smpSoldFor);
    }

    @Override
    public InvestmentStatus getStatus() {
        return status;
    }

    @Override
    public Optional<PaymentStatus> getPaymentStatus() {
        return Optional.ofNullable(paymentStatus);
    }

    @Override
    public Optional<Boolean> isInWithdrawal() {
        return Optional.ofNullable(isInWithdrawal);
    }

    @Override
    public Rating getRating() {
        return rating;
    }

    @Override
    public final String toString() {
        return new ToStringBuilder(this).toString();
    }
}
