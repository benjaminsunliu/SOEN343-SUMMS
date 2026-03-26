package com.thehorselegend.summs.application.mapper;

import com.thehorselegend.summs.api.dto.RentalAnalyticsMetricDto;
import com.thehorselegend.summs.infrastructure.persistence.RentalAnalyticsMetricEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for RentalAnalyticsMetricMapper.
 * Tests DTO/Entity conversion logic.
 */
class RentalAnalyticsMetricMapperTest {

    private RentalAnalyticsMetricMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RentalAnalyticsMetricMapper();
    }

    @Test
    void testEntityToDto() {
        // Arrange
        RentalAnalyticsMetricEntity entity = new RentalAnalyticsMetricEntity(
                "ACTIVE_RENTALS",
                "CAR",
                25L
        );

        // Act
        RentalAnalyticsMetricDto dto = mapper.entityToDto(entity);

        // Assert
        assertNotNull(dto);
        assertEquals("ACTIVE_RENTALS", dto.getMetricName());
        assertEquals("CAR", dto.getDimension());
        assertEquals(25L, dto.getCount());
    }

    @Test
    void testDtoToEntity() {
        // Arrange
        RentalAnalyticsMetricDto dto = new RentalAnalyticsMetricDto(
                "ACTIVE_RENTALS",
                "BICYCLE",
                12L
        );

        // Act
        RentalAnalyticsMetricEntity entity = mapper.dtoToEntity(dto);

        // Assert
        assertNotNull(entity);
        assertEquals("ACTIVE_RENTALS", entity.getMetricName());
        assertEquals("BICYCLE", entity.getDimension());
        assertEquals(12L, entity.getCount());
    }
}
