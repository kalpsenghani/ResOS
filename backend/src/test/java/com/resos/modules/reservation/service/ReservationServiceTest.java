package com.resos.modules.reservation.service;

import com.resos.modules.audit.service.AuditLogService;
import com.resos.modules.reservation.domain.Reservation;
import com.resos.modules.reservation.domain.ReservationStatus;
import com.resos.modules.reservation.domain.RestaurantTable;
import com.resos.modules.reservation.dto.AvailabilityResponse;
import com.resos.modules.reservation.dto.CreateReservationRequest;
import com.resos.modules.reservation.dto.UpdateReservationStatusRequest;
import com.resos.modules.reservation.repository.ReservationRepository;
import com.resos.modules.reservation.repository.RestaurantTableRepository;
import com.resos.modules.restaurant.repository.RestaurantRepository;
import com.resos.shared.exception.BusinessException;
import com.resos.shared.security.UserPrincipal;
import com.resos.shared.tenant.TenantContext;
import com.resos.shared.tenant.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private RestaurantTableRepository tableRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private UserPrincipal principal;

    @InjectMocks
    private ReservationService reservationService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();
    private final UUID tableId = UUID.randomUUID();
    private final UUID reservationId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContextHolder.set(new TenantContext(tenantId, false));
        when(principal.getId()).thenReturn(userId);
        when(restaurantRepository.findByIdAndTenantIdAndDeletedAtIsNull(restaurantId, tenantId))
                .thenReturn(Optional.of(com.resos.modules.restaurant.domain.Restaurant.builder()
                        .id(restaurantId)
                        .tenantId(tenantId)
                        .build()));
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void createReservationAssignsTableWhenAvailable() {
        RestaurantTable table = RestaurantTable.builder()
                .id(tableId)
                .tenantId(tenantId)
                .restaurantId(restaurantId)
                .tableNumber("T1")
                .capacity(4)
                .active(true)
                .build();

        when(tableRepository.findByIdAndTenantId(tableId, tenantId)).thenReturn(Optional.of(table));
        when(reservationRepository.existsOverlapping(
                        eq(tableId), any(), any(), any(), any(), isNull()))
                .thenReturn(false);
        when(reservationRepository.save(any())).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            reservation.setId(reservationId);
            return reservation;
        });
        when(tableRepository.findAllById(any())).thenReturn(List.of(table));

        var request = new CreateReservationRequest(
                restaurantId,
                tableId,
                "Jane Doe",
                "+1234567890",
                "jane@example.com",
                4,
                LocalDate.now().plusDays(1),
                LocalTime.of(19, 0),
                null,
                "Window seat");

        var response = reservationService.createReservation(request, principal);

        assertThat(response.guestName()).isEqualTo("Jane Doe");
        assertThat(response.tableId()).isEqualTo(tableId);
        assertThat(response.status()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(auditLogService).log(any(), eq("Reservation"), eq(reservationId), isNull(), any(), eq(userId));
    }

    @Test
    void createReservationFailsWhenTableOverlaps() {
        RestaurantTable table = RestaurantTable.builder()
                .id(tableId)
                .tenantId(tenantId)
                .restaurantId(restaurantId)
                .tableNumber("T1")
                .capacity(4)
                .active(true)
                .build();

        when(tableRepository.findByIdAndTenantId(tableId, tenantId)).thenReturn(Optional.of(table));
        when(reservationRepository.existsOverlapping(
                        eq(tableId), any(), any(), any(), any(), isNull()))
                .thenReturn(true);

        var request = new CreateReservationRequest(
                restaurantId,
                tableId,
                "Jane Doe",
                null,
                null,
                2,
                LocalDate.now().plusDays(1),
                LocalTime.of(19, 0),
                null,
                null);

        assertThatThrownBy(() -> reservationService.createReservation(request, principal))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void checkAvailabilityReturnsSuggestedTables() {
        RestaurantTable table = RestaurantTable.builder()
                .id(tableId)
                .tenantId(tenantId)
                .restaurantId(restaurantId)
                .tableNumber("T4")
                .capacity(4)
                .active(true)
                .build();

        when(tableRepository.findByTenantIdAndRestaurantIdAndActiveTrueOrderByTableNumberAsc(tenantId, restaurantId))
                .thenReturn(List.of(table));
        when(reservationRepository.findBookedTableIds(
                        eq(tenantId), eq(restaurantId), any(), any(), any(), any()))
                .thenReturn(List.of());

        AvailabilityResponse response = reservationService.checkAvailability(
                restaurantId, LocalDate.now().plusDays(1), 4, LocalTime.of(19, 0));

        assertThat(response.available()).isTrue();
        assertThat(response.suggestedTables()).hasSize(1);
        assertThat(response.suggestedTables().get(0).tableNumber()).isEqualTo("T4");
    }

    @Test
    void updateStatusEnforcesTransitions() {
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .tenantId(tenantId)
                .restaurantId(restaurantId)
                .guestName("Guest")
                .partySize(2)
                .reservationDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 0))
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findByIdAndTenantId(reservationId, tenantId))
                .thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = reservationService.updateStatus(
                reservationId, new UpdateReservationStatusRequest(ReservationStatus.SEATED), principal);

        assertThat(response.status()).isEqualTo(ReservationStatus.SEATED);
    }

    @Test
    void countTodayReservationsUsesActiveStatuses() {
        when(reservationRepository.countByTenantIdAndRestaurantIdAndReservationDateAndStatusIn(
                        eq(tenantId), eq(restaurantId), any(), anyList()))
                .thenReturn(3L);

        assertThat(reservationService.countTodayReservations(restaurantId)).isEqualTo(3L);
    }
}
