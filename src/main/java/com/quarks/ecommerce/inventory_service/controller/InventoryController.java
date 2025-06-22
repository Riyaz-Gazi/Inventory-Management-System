package com.quarks.ecommerce.inventory_service.controller;

import com.quarks.ecommerce.inventory_service.dto.CreateSupplyRequestDto;
import com.quarks.ecommerce.inventory_service.dto.InventoryDto;
import com.quarks.ecommerce.inventory_service.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<String> createOrUpdateSupply(@RequestBody CreateSupplyRequestDto createSupplyRequestDto) {
        InventoryDto inventoryDto = inventoryService.createOrUpdateSupply(createSupplyRequestDto);
        return ResponseEntity.ok("Supply updated for item ID " + inventoryDto.getItemId() + ", available quantity: " + inventoryDto.getTotalQuantity());
    }

    @PostMapping("/reserve")
    public ResponseEntity<String> reserveItem(@RequestParam Long itemId, @RequestParam int quantity, @RequestParam String reservedBy) {
        String token = inventoryService.reserveItem(itemId, quantity, reservedBy);
        if (token == null) {
            return ResponseEntity.badRequest().body("Insufficient Inventory");
        }
        return ResponseEntity.ok(token);
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelReservation(@RequestParam String token) {
        return ResponseEntity.ok(inventoryService.cancelReservation(token));
    }

    @GetMapping("/{itemId}/availability")
    public ResponseEntity<Integer> getAvailability(@PathVariable Long itemId) {
        int getAvailableQuantity = inventoryService.getAvailability(itemId);
        return ResponseEntity.ok(getAvailableQuantity);
    }

}
