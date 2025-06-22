package com.quarks.ecommerce.inventory_service.dto;

public class InventoryDto {
    private Long id;
    private Long itemId;
    private String name;
    private int totalQuantity;
    private int reservedQuantity;
    private Long version;

    public InventoryDto() {
    }

    public InventoryDto(Long id, Long itemId, String name, int totalQuantity, int reservedQuantity, Long version) {
        this.id = id;
        this.itemId = itemId;
        this.name = name;
        this.totalQuantity = totalQuantity;
        this.reservedQuantity = reservedQuantity;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(int reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
