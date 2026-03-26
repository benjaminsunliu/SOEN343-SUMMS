package com.thehorselegend.summs.application.mapper;

import com.thehorselegend.summs.api.dto.RentalAnalyticsMetricDto;
import com.thehorselegend.summs.infrastructure.persistence.RentalAnalyticsMetricEntity;
import org.springframework.stereotype.Component;

@Component
public class RentalAnalyticsMetricMapper {

    public RentalAnalyticsMetricDto entityToDto(RentalAnalyticsMetricEntity entity) {
        return new RentalAnalyticsMetricDto(
                entity.getMetricName(),
                entity.getDimension(),
                entity.getCount()
        );
    }

    public RentalAnalyticsMetricEntity dtoToEntity(RentalAnalyticsMetricDto dto) {
        return new RentalAnalyticsMetricEntity(
                dto.getMetricName(),
                dto.getDimension(),
                dto.getCount()
        );
    }
}
