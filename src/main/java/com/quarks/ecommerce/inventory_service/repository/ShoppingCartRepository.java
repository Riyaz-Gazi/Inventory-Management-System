package com.quarks.ecommerce.inventory_service.repository;

import com.quarks.ecommerce.inventory_service.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart,Long> {
}
