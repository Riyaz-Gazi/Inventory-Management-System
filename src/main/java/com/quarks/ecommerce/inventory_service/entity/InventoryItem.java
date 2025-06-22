package com.quarks.ecommerce.inventory_service.entity;

import jakarta.persistence.*;
@Entity
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private Long itemId;
    private String name;
    private int totalQuantity;
    private int reservedQuantity;
    @Version
    private Long version;

    public int getAvailableQuantity() {
        int availableQuantity = totalQuantity - reservedQuantity;
        return Math.max(0, availableQuantity);
    }

    public void addSupply(int quantity) {
        this.totalQuantity += quantity;
    }

    public boolean reserve(int quantity) {
        if (getAvailableQuantity() >= quantity) {
            this.reservedQuantity += quantity;
            return true;
        }
        return false;
    }

    public void cancelReservation(int quantity) {
        this.reservedQuantity -= quantity;
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
