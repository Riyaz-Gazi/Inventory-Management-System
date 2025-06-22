package com.quarks.ecommerce.inventory_service.service;

import com.quarks.ecommerce.inventory_service.dto.CreateSupplyRequestDto;
import com.quarks.ecommerce.inventory_service.dto.InventoryDto;
import org.reactivestreams.Publisher;

public interface InventoryService {

    InventoryDto createOrUpdateSupply(CreateSupplyRequestDto createSupplyRequestDto);

    String reserveItem(Long itemId,int quantity,String reservedBy);

    String cancelReservation(String reservationToken);

    int getAvailability(Long itemId);
}
