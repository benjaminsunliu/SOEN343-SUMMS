package com.thehorselegend.summs.api.dto;

import java.util.Map;

public record ProviderPaymentAnalyticsResponse(
        int totalTransactions,
        int successfulTransactions,
        int failedTransactions,
        double totalRevenue,
        double successRatePercentage,
        Map<String, Double> revenueByPaymentMethod
) {
}
