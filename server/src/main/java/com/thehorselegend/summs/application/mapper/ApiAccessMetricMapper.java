package com.thehorselegend.summs.application.mapper;

import com.thehorselegend.summs.api.dto.ApiAccessMetricDto;
import com.thehorselegend.summs.infrastructure.persistence.ApiAccessMetricEntity;
import org.springframework.stereotype.Component;

@Component
public class ApiAccessMetricMapper {

    public ApiAccessMetricDto entityToDto(ApiAccessMetricEntity entity) {
        return new ApiAccessMetricDto(
                entity.getEndpoint(),
                entity.getTimeWindow(),
                entity.getAccessCount()
        );
    }

    public ApiAccessMetricEntity dtoToEntity(ApiAccessMetricDto dto) {
        return new ApiAccessMetricEntity(
                dto.getEndpoint(),
                dto.getTimeWindow(),
                dto.getAccessCount()
        );
    }
}
