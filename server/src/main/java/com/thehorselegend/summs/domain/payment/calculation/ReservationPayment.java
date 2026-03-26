package com.thehorselegend.summs.domain.payment.calculation;

import com.thehorselegend.summs.domain.reservation.Reservation;

import java.time.Duration;

/**
 * Base payment for a reservation before any extra fees/tax/discount.
 */
public class ReservationPayment implements Payment {

    private static final double DEFAULT_RATE_PER_MINUTE = 0.50;
    private final double baseAmount;

    public ReservationPayment(Reservation reservation) {
        this.baseAmount = calculateBaseCost(reservation);
    }

    public ReservationPayment(double baseAmount) {
        if (baseAmount < 0) {
            throw new IllegalArgumentException("Base amount cannot be negative");
        }
        this.baseAmount = baseAmount;
    }

    @Override
    public double getAmount() {
        return baseAmount;
    }

    @Override
    public String getDescription() {
        return "Base reservation payment";
    }

    // Simple base-cost rule for the project: reservation duration x fixed rate.
    private double calculateBaseCost(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation is required");
        }

        long minutes = Duration.between(reservation.getStartDate(), reservation.getEndDate()).toMinutes();
        if (minutes <= 0) {
            minutes = 1;
        }

        return minutes * DEFAULT_RATE_PER_MINUTE;
    }
}
