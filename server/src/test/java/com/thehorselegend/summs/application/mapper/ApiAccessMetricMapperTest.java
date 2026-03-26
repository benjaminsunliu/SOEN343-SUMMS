package com.thehorselegend.summs.application.mapper;

import com.thehorselegend.summs.api.dto.ApiAccessMetricDto;
import com.thehorselegend.summs.infrastructure.persistence.ApiAccessMetricEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for ApiAccessMetricMapper.
 * Tests DTO/Entity conversion logic.
 */
class ApiAccessMetricMapperTest {

    private ApiAccessMetricMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ApiAccessMetricMapper();
    }

    @Test
    void testEntityToDto() {
        // Arrange
        ApiAccessMetricEntity entity = new ApiAccessMetricEntity(
                "VEHICLE_RESERVATION",
                "24H",
                156L
        );

        // Act
        ApiAccessMetricDto dto = mapper.entityToDto(entity);

        // Assert
        assertNotNull(dto);
        assertEquals("VEHICLE_RESERVATION", dto.getEndpoint());
        assertEquals("24H", dto.getTimeWindow());
        assertEquals(156L, dto.getAccessCount());
    }

    @Test
    void testDtoToEntity() {
        // Arrange
        ApiAccessMetricDto dto = new ApiAccessMetricDto(
                "GET_TRANSIT_DETAILS",
                "WEEK",
                500L
        );

        // Act
        ApiAccessMetricEntity entity = mapper.dtoToEntity(dto);

        // Assert
        assertNotNull(entity);
        assertEquals("GET_TRANSIT_DETAILS", entity.getEndpoint());
        assertEquals("WEEK", entity.getTimeWindow());
        assertEquals(500L, entity.getAccessCount());
    }
}
