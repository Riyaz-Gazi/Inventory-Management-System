package com.quarks.ecommerce.inventory_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quarks.ecommerce.inventory_service.dto.CreateSupplyRequestDto;
import com.quarks.ecommerce.inventory_service.dto.InventoryDto;
import com.quarks.ecommerce.inventory_service.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private InventoryService inventoryService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /inventory - should create or update supply")
    void createOrUpdateSupply() throws Exception {
        CreateSupplyRequestDto request = new CreateSupplyRequestDto(1L,"Mobile",10);
        InventoryDto responseDto = new InventoryDto(1L,1L,"Mobile",10,0,0L);

        when(inventoryService.createOrUpdateSupply(any()))
                .thenReturn(responseDto);

        mockMvc.perform(post("/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Supply updated for item ID 1, available quantity: 10"));
    }

    @Test
    @DisplayName("POST /inventory/reserve - should reserve item and return token")
    void reserveItem() throws Exception {
        String token = "reservation-token-123";

        when(inventoryService.reserveItem(eq(1L), eq(5), eq("testUser")))
                .thenReturn(token);

        mockMvc.perform(post("/inventory/reserve")
                        .param("itemId", "1")
                        .param("quantity", "5")
                        .param("reservedBy", "testUser"))
                .andExpect(status().isOk())
                .andExpect(content().string(token));
    }

    @Test
    @DisplayName("POST /inventory/reserve - should return 400 if inventory insufficient")
    void reserveItem_insufficientInventory() throws Exception {
        when(inventoryService.reserveItem(eq(1L), eq(100), eq("testUser")))
                .thenReturn(null);

        mockMvc.perform(post("/inventory/reserve")
                        .param("itemId", "1")
                        .param("quantity", "100")
                        .param("reservedBy", "testUser"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Insufficient Inventory"));
    }

    @Test
    @DisplayName("POST /inventory/cancel - should cancel reservation")
    void cancelReservation() throws Exception {
        when(inventoryService.cancelReservation(eq("reservation-token-123")))
                .thenReturn("Reservation cancelled");

        mockMvc.perform(post("/inventory/cancel")
                        .param("token", "reservation-token-123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reservation cancelled"));
    }

    @Test
    @DisplayName("GET /inventory/{itemId}/availability - should return available quantity")
    void testGetAvailability() throws Exception {
        when(inventoryService.getAvailability(1L)).thenReturn(25);

        mockMvc.perform(get("/inventory/1/availability"))
                .andExpect(status().isOk())
                .andExpect(content().string("25"));
    }
}
