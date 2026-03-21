package com.thehorselegend.summs.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehorselegend.summs.api.dto.LocationDto;
import com.thehorselegend.summs.api.dto.ReservationRequest;
import com.thehorselegend.summs.application.service.ReservationService;
import com.thehorselegend.summs.domain.vehicle.Reservation;
import com.thehorselegend.summs.domain.vehicle.ReservationStatus;
import com.thehorselegend.summs.infrastructure.persistence.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession session;
    private UserEntity user;

    @BeforeEach
    void setup() {
        user = new UserEntity();
        user.setId(1L);

        session = new MockHttpSession();
        session.setAttribute("user", user);
    }

    @Test
    void testReserveVehicle_success() throws Exception {

        LocationDto start = new LocationDto(45.5017, -73.5673);
        LocationDto end = new LocationDto(45.5088, -73.5540);

        ReservationRequest request = new ReservationRequest(
                start, end, "Montreal",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1)
        );

        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationService.reserveVehicle(any(), anyLong(), any(), any(), anyString(), any(), any()))
                .thenReturn(reservation);

        mockMvc.perform(post("/api/vehicles/1/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void testCancelReservation_success() throws Exception {
        Reservation cancelled = new Reservation();
        cancelled.setStatus(ReservationStatus.CANCELLED);

        when(reservationService.cancelReservation(1L, user))
                .thenReturn(cancelled);

        mockMvc.perform(post("/api/reservations/1/cancel").session(session))
                .andExpect(status().isOk());
    }
}