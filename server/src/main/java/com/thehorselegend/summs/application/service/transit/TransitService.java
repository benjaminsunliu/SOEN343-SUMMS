package com.thehorselegend.summs.application.service.transit;

import com.thehorselegend.summs.api.dto.TransitLineStatusDTO;
import com.thehorselegend.summs.api.dto.TransitRouteDTO;
import com.thehorselegend.summs.api.dto.TransitSearchRequestDTO;

import java.util.List;
public interface TransitService {
    /** Search routes matching origin → destination at a given date/time. */
    List<TransitRouteDTO> searchRoutes(TransitSearchRequestDTO request);

    /** Return live status for all monitored lines. */
    List<TransitLineStatusDTO> getLineStatuses();
}
