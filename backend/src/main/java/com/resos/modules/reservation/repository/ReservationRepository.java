package com.resos.modules.reservation.repository;

import com.resos.modules.reservation.domain.Reservation;
import com.resos.modules.reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID>, JpaSpecificationExecutor<Reservation> {

    Optional<Reservation> findByIdAndTenantId(UUID id, UUID tenantId);

    long countByTenantIdAndRestaurantIdAndReservationDateAndStatusIn(
            UUID tenantId,
            UUID restaurantId,
            LocalDate reservationDate,
            Collection<ReservationStatus> statuses);

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Reservation r
            WHERE r.tableId = :tableId
              AND r.reservationDate = :date
              AND r.status NOT IN ('CANCELLED', 'NO_SHOW', 'COMPLETED')
              AND (:excludeId IS NULL OR r.id <> :excludeId)
              AND r.startTime < :endTime
              AND COALESCE(r.endTime, :defaultEndTime) > :startTime
            """)
    boolean existsOverlapping(
            @Param("tableId") UUID tableId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("defaultEndTime") LocalTime defaultEndTime,
            @Param("excludeId") UUID excludeId);

    @Query("""
            SELECT r.tableId FROM Reservation r
            WHERE r.tenantId = :tenantId
              AND r.restaurantId = :restaurantId
              AND r.reservationDate = :date
              AND r.tableId IS NOT NULL
              AND r.status NOT IN ('CANCELLED', 'NO_SHOW', 'COMPLETED')
              AND r.startTime < :endTime
              AND COALESCE(r.endTime, :defaultEndTime) > :startTime
            """)
    List<UUID> findBookedTableIds(
            @Param("tenantId") UUID tenantId,
            @Param("restaurantId") UUID restaurantId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("defaultEndTime") LocalTime defaultEndTime);

    long countByTenantIdAndRestaurantIdAndTableIdAndStatusNotIn(
            UUID tenantId, UUID restaurantId, UUID tableId, Collection<ReservationStatus> statuses);
}
