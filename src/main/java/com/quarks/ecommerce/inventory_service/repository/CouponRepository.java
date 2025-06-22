package com.quarks.ecommerce.inventory_service.repository;

import com.quarks.ecommerce.inventory_service.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon,Long> {
}
