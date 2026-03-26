package com.thehorselegend.summs.infrastructure.adapter;

import com.thehorselegend.summs.api.dto.ParkingFacilityDTO;
import com.thehorselegend.summs.api.dto.ParkingSearchRequestDTO;
import java.util.List;
public interface IParkingService {
    List<ParkingFacilityDTO> searchFacilities(ParkingSearchRequestDTO request);
}
