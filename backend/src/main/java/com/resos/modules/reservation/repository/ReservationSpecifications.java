package com.resos.modules.reservation.repository;

import com.resos.modules.reservation.domain.Reservation;
import com.resos.modules.reservation.domain.ReservationStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public final class ReservationSpecifications {

    private ReservationSpecifications() {}

    public static Specification<Reservation> forTenant(UUID tenantId) {
        return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
    }

    public static Specification<Reservation> forRestaurant(UUID restaurantId) {
        return (root, query, cb) -> cb.equal(root.get("restaurantId"), restaurantId);
    }

    public static Specification<Reservation> forDate(LocalDate date) {
        return (root, query, cb) -> cb.equal(root.get("reservationDate"), date);
    }

    public static Specification<Reservation> forDateRange(LocalDate start, LocalDate end) {
        return (root, query, cb) -> cb.between(root.get("reservationDate"), start, end);
    }

    public static Specification<Reservation> forStatus(ReservationStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Reservation> searchGuest(String term) {
        String pattern = "%" + term.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("guestName")), pattern),
                cb.like(cb.lower(cb.coalesce(root.get("guestPhone"), "")), pattern),
                cb.like(cb.lower(cb.coalesce(root.get("guestEmail"), "")), pattern));
    }
}
