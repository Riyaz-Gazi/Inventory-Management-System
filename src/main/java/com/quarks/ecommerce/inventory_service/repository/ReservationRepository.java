package com.quarks.ecommerce.inventory_service.repository;

import com.quarks.ecommerce.inventory_service.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation,Long> {
    Optional<Reservation> findByReservationToken(String token);
}
