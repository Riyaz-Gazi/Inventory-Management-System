package com.quarks.ecommerce.inventory_service.dto;

import lombok.Data;

public class CreateSupplyRequestDto {
    private Long itemId;
    private String name;
    private int quantity;
    private Long version;

    public CreateSupplyRequestDto() {
    }

    public CreateSupplyRequestDto(Long itemId, String name, int quantity) {
        this.itemId = itemId;
        this.name = name;
        this.quantity = quantity;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
