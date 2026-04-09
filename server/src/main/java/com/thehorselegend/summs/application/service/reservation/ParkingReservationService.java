package com.thehorselegend.summs.application.service.reservation;


import com.thehorselegend.summs.api.dto.CreateParkingReservationRequest;
import com.thehorselegend.summs.api.dto.ParkingReservationResponse;
import com.thehorselegend.summs.application.service.payment.PaymentApplicationService;
import com.thehorselegend.summs.application.service.payment.PaymentTransactionService;
import com.thehorselegend.summs.domain.payment.calculation.Payment;
import com.thehorselegend.summs.domain.payment.method.PaymentMethodDetails;
import com.thehorselegend.summs.domain.payment.method.PaymentResult;
import com.thehorselegend.summs.domain.reservation.ParkingReservation;
import com.thehorselegend.summs.domain.reservation.ReservationStatus;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationEntity;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationMapper;
import com.thehorselegend.summs.infrastructure.persistence.ParkingReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParkingReservationService extends ReservationCreationTemplate<ParkingReservationService.ParkingReservationSource, ParkingReservation> {
    private static final DateTimeFormatter CONFIRMED_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final long PARKING_PROVIDER_PLACEHOLDER_ID = 0L;
    private static final String MODIFY_NOT_AUTHORIZED_MESSAGE = "User not authorized to modify this parking reservation";

    private final ParkingReservationRepository reservationRepository;
    private final PaymentApplicationService paymentApplicationService;
    private final PaymentTransactionService paymentTransactionService;

    @Transactional
    public ParkingReservationResponse createReservation(
            CreateParkingReservationRequest request,
            Long userId
    ) {
        log.info("Creating parking reservation for facilityId={} userId={}",
                request.getFacilityId(), userId);

        LocalDateTime startDate = parseArrivalDateTime(request.getArrivalDate(), request.getArrivalTime());
        LocalDateTime endDate = startDate.plusHours(resolveDurationHours(request.getDurationHours()));

        PaymentApplicationService.PaymentOptions paymentOptions = buildPaymentOptions(request);
        Payment payment = paymentApplicationService.buildPayment(request.getTotalCost(), paymentOptions);
        PaymentResult paymentResult = paymentApplicationService.processReservationPayment(
                request.getTotalCost(),
                paymentOptions,
                request.getPaymentMethod(),
                buildPaymentMethodDetails(request)
        );

        if (!paymentResult.isSuccess()) {
            throw new IllegalArgumentException(paymentResult.getMessage());
        }

        ParkingReservation savedReservation = createReservation(
                new ParkingReservationSource(request, payment.getAmount()),
                userId,
                startDate,
                endDate
        );

        String paymentMethod = request.getPaymentMethod().toUpperCase(Locale.ROOT);
        String paymentAuthorizationCode = "PAY-" + paymentResult.getTransactionId();

        paymentTransactionService.recordTransaction(
                savedReservation.getId(),
                savedReservation.getUserId(),
                PARKING_PROVIDER_PLACEHOLDER_ID,
                paymentMethod,
                payment.getAmount(),
                true,
                paymentResult.getMessage(),
                paymentResult.getTransactionId(),
                paymentAuthorizationCode
        );

        log.info("Parking reservation created id={}", savedReservation.getId());

        ParkingReservationEntity savedEntity = reservationRepository.findById(savedReservation.getId())
                .orElseThrow(() -> new IllegalArgumentException("Parking reservation not found after save"));

        return toResponse(savedReservation, savedEntity.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<ParkingReservationResponse> getUserReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ParkingReservationMapper::toDomain)
                .map(reservation -> toResponse(reservation, LocalDateTime.now()))
                .toList();
    }

    @Transactional
    public void cancelReservation(Long reservationId, Long userId) {
        ParkingReservationEntity entity = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Parking reservation not found"));

        ParkingReservation reservation = ParkingReservationMapper.toDomain(entity);

        if (!reservation.getUserId().equals(userId)) {
            throw new IllegalStateException("User not authorized to cancel this parking reservation");
        }

        cancelDomainReservation(reservation);

        entity.setStatus(reservation.getStatus());
        reservationRepository.save(entity);
    }

    @Transactional
    public ParkingReservationResponse occupyReservation(Long reservationId, Long userId) {
        ParkingReservationEntity entity = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Parking reservation not found"));

        ParkingReservation reservation = ParkingReservationMapper.toDomain(entity);
        ensureReservationOwnedByUser(reservation, userId);

        activateDomainReservation(reservation);

        entity.setStatus(reservation.getStatus());
        reservationRepository.save(entity);

        return toResponse(reservation, entity.getCreatedAt());
    }

    @Transactional
    public ParkingReservationResponse checkoutReservation(Long reservationId, Long userId) {
        ParkingReservationEntity entity = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Parking reservation not found"));

        ParkingReservation reservation = ParkingReservationMapper.toDomain(entity);
        ensureReservationOwnedByUser(reservation, userId);

        completeDomainReservation(reservation);

        entity.setStatus(reservation.getStatus());
        reservationRepository.save(entity);

        return toResponse(reservation, entity.getCreatedAt());
    }

    @Override
    protected void validateAvailability(
            ParkingReservationSource source,
            LocalDateTime start,
            LocalDateTime end
    ) {
        CreateParkingReservationRequest request = source.request();

        if (request.getFacilityId() == null) {
            throw new IllegalArgumentException("Facility id is required");
        }

        if (request.getArrivalDate() == null || request.getArrivalTime() == null) {
            throw new IllegalArgumentException("Arrival date and time are required");
        }

        Integer durationHours = request.getDurationHours();
        if (durationHours == null || durationHours <= 0) {
            throw new IllegalArgumentException("Duration must be at least 1 hour");
        }

        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        Double totalCost = source.finalTotalCost();
        if (totalCost < 0) {
            throw new IllegalArgumentException("Parking total cost is required");
        }
    }

    @Override
    protected ParkingReservation buildReservation(
            ParkingReservationSource source,
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    ) {
        CreateParkingReservationRequest request = source.request();

        ParkingReservation reservation = new ParkingReservation(
                userId,
                request.getFacilityId(),
                start,
                end,
                request.getCity(),
                request.getFacilityName(),
                request.getFacilityAddress(),
                request.getDurationHours(),
                source.finalTotalCost()
        );

        reservation.confirm();
        return reservation;
    }

    private PaymentApplicationService.PaymentOptions buildPaymentOptions(CreateParkingReservationRequest request) {
        return new PaymentApplicationService.PaymentOptions(
                true,
                PaymentApplicationService.FIXED_SERVICE_FEE_AMOUNT,
                true,
                PaymentApplicationService.FIXED_TAX_RATE,
                request.isIncludeInsuranceFee(),
                request.getInsuranceFeeAmount(),
                request.isIncludeDiscount(),
                request.getDiscountAmount()
        );
    }

    private PaymentMethodDetails buildPaymentMethodDetails(CreateParkingReservationRequest request) {
        return new PaymentMethodDetails(
                request.getCreditCardNumber(),
                request.getPaypalEmail(),
                request.getPaypalPassword()
        );
    }

    @Override
    @Transactional
    protected ParkingReservation saveReservation(ParkingReservation reservation) {
        ParkingReservationEntity saved = reservationRepository.save(
                ParkingReservationMapper.toEntity(reservation)
        );
        return ParkingReservationMapper.toDomain(saved);
    }

    private int resolveDurationHours(Integer durationHours) {
        if (durationHours == null || durationHours <= 0) {
            throw new IllegalArgumentException("Duration must be at least 1 hour");
        }
        return durationHours;
    }

    private void cancelDomainReservation(ParkingReservation reservation) {
        try {
            reservation.cancel();
        } catch (IllegalStateException ex) {
            String message = ex.getMessage();
            if ("Reservation is already cancelled".equals(message)) {
                throw new IllegalStateException("Parking reservation is already cancelled");
            }
            if ("Expired reservations cannot be cancelled".equals(message)) {
                throw new IllegalStateException("Expired parking reservations cannot be cancelled");
            }
            if ("Active reservations cannot be cancelled".equals(message)) {
                throw new IllegalStateException("Parking reservation cannot be cancelled in its current state");
            }
            throw ex;
        }
    }

    private void activateDomainReservation(ParkingReservation reservation) {
        try {
            reservation.activate();
        } catch (IllegalStateException ex) {
            if ("Only confirmed reservations can be activated".equals(ex.getMessage())) {
                throw new IllegalStateException("Parking reservation must be confirmed before occupying the spot");
            }
            throw ex;
        }
    }

    private void completeDomainReservation(ParkingReservation reservation) {
        try {
            reservation.complete();
        } catch (IllegalStateException ex) {
            if ("Only active reservations can be completed".equals(ex.getMessage())) {
                throw new IllegalStateException("Parking reservation must be active before checkout");
            }
            throw ex;
        }
    }

    private void ensureReservationOwnedByUser(ParkingReservation reservation, Long userId) {
        if (!reservation.getUserId().equals(userId)) {
            throw new IllegalStateException(MODIFY_NOT_AUTHORIZED_MESSAGE);
        }
    }

    private LocalDateTime parseArrivalDateTime(String arrivalDate, String arrivalTime) {
        if (arrivalDate == null || arrivalTime == null) {
            throw new IllegalArgumentException("Arrival date and time are required");
        }

        try {
            return LocalDateTime.of(
                    LocalDate.parse(arrivalDate),
                    LocalTime.parse(arrivalTime)
            );
        } catch (DateTimeException ex) {
            throw new IllegalArgumentException("Invalid parking arrival date/time", ex);
        }
    }

    private ParkingReservationResponse toResponse(ParkingReservation reservation, LocalDateTime confirmedAt) {
        ReservationStatus status = reservation.getStatus() == null
                ? ReservationStatus.CONFIRMED
                : reservation.getStatus();

        return ParkingReservationResponse.builder()
                .reservationId(reservation.getId())
                .facilityName(reservation.getFacilityName())
                .facilityAddress(reservation.getFacilityAddress())
                .city(reservation.getCity())
                .arrivalDate(reservation.getStartDate().toLocalDate().toString())
                .arrivalTime(reservation.getStartDate().toLocalTime().format(TIME_FORMATTER))
                .durationHours(reservation.getDurationHours())
                .totalCost(reservation.getTotalCost())
                .status(status.name())
                .confirmedAt(confirmedAt.format(CONFIRMED_AT_FORMATTER))
                .build();
    }

    public static record ParkingReservationSource(
            CreateParkingReservationRequest request,
            double finalTotalCost
    ) {
    }
}
