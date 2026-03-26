package com.thehorselegend.summs.domain.payment.calculation;

/**
 * Base payment abstraction used by concrete payments and decorators.
 */
public interface Payment {

    double getAmount();

    String getDescription();
}
