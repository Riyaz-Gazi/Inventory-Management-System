package com.quarks.ecommerce.inventory_service.service.impl;

import com.quarks.ecommerce.inventory_service.dto.CreateSupplyRequestDto;
import com.quarks.ecommerce.inventory_service.dto.InventoryDto;
import com.quarks.ecommerce.inventory_service.entity.InventoryItem;
import com.quarks.ecommerce.inventory_service.entity.Reservation;
import com.quarks.ecommerce.inventory_service.entity.enums.ReservationStatus;
import com.quarks.ecommerce.inventory_service.exceptions.ResourceNotFoundException;
import com.quarks.ecommerce.inventory_service.repository.InventoryItemRepository;
import com.quarks.ecommerce.inventory_service.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {
    private InventoryItemRepository inventoryItemRepository;
    private ReservationRepository reservationRepository;
    @Spy
    private ModelMapper modelMapper;
    private RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOperations;
    private InventoryServiceImpl inventoryService;

    @BeforeEach
    public void setUp() {
        inventoryItemRepository = mock(InventoryItemRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        modelMapper = new ModelMapper();
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        inventoryService = new InventoryServiceImpl(inventoryItemRepository, reservationRepository, modelMapper, redisTemplate);
    }

    @Test
    public void testCreateOrUpdateSupply_NewItem() {
        CreateSupplyRequestDto dto = new CreateSupplyRequestDto(1L, "Test Item", 10);

        when(inventoryItemRepository.findByItemId(1L)).thenReturn(Optional.empty());

        InventoryItem savedItem = new InventoryItem();
        savedItem.setItemId(1L);
        savedItem.setName("Test Item");
        savedItem.setTotalQuantity(10);
        savedItem.setReservedQuantity(0);
        when(inventoryItemRepository.save(any())).thenReturn(savedItem);

        InventoryDto result = inventoryService.createOrUpdateSupply(dto);

        assertThat(result.getItemId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Item");
        verify(redisTemplate.opsForValue()).set("inventory_availability:1", 10);
    }

    @Test
    public void testReserveItem_Success() {
        InventoryItem item = new InventoryItem();
        item.setItemId(1L);
        item.setTotalQuantity(10);
        item.setReservedQuantity(0);

        when(inventoryItemRepository.findByItemId(1L)).thenReturn(Optional.of(item));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(inventoryItemRepository.saveAndFlush(any())).thenReturn(item);

        String token = inventoryService.reserveItem(1L, 5, "user1");

        assertThat(token).isNotNull();
        verify(redisTemplate.opsForValue()).set("inventory_availability:1", 5);
    }

    @Test
    public void testReserveItem_InsufficientQuantity() {
        InventoryItem item = new InventoryItem();
        item.setItemId(1L);
        item.setTotalQuantity(5);
        item.setReservedQuantity(5);

        when(inventoryItemRepository.findByItemId(1L)).thenReturn(Optional.of(item));

        String token = inventoryService.reserveItem(1L, 5, "user1");

        assertThat(token).isNull();
    }

    @Test
    public void testCancelReservation_Success() {
        Reservation reservation = new Reservation();
        reservation.setReservationToken("token123");
        reservation.setReservationStatus(ReservationStatus.RESERVED);
        reservation.setItemId(1L);
        reservation.setQuantity(5);

        InventoryItem item = new InventoryItem();
        item.setItemId(1L);
        item.setTotalQuantity(10);
        item.setReservedQuantity(5);

        when(reservationRepository.findByReservationToken("token123")).thenReturn(Optional.of(reservation));
        when(inventoryItemRepository.findByItemId(1L)).thenReturn(Optional.of(item));
        when(inventoryItemRepository.save(any())).thenReturn(item);

        String response = inventoryService.cancelReservation("token123");

        assertThat(response).isEqualTo("Reservation cancelled");
        verify(reservationRepository).save(reservation);
        verify(redisTemplate.opsForValue()).set("inventory_availability:1", 10);
    }

    @Test
    public void testCancelReservation_AlreadyCancelled() {
        Reservation reservation = new Reservation();
        reservation.setReservationToken("token123");
        reservation.setReservationStatus(ReservationStatus.CANCELLED);

        when(reservationRepository.findByReservationToken("token123")).thenReturn(Optional.of(reservation));

        String result = inventoryService.cancelReservation("token123");

        assertThat(result).isEqualTo("Reservation already cancelled");
        verify(inventoryItemRepository, never()).save(any());
    }

    @Test
    public void testGetAvailability_FromCache() {
        when(valueOperations.get("inventory_availability:1")).thenReturn(20);

        int availability = inventoryService.getAvailability(1L);

        assertThat(availability).isEqualTo(20);
        verify(redisTemplate.opsForValue()).get("inventory_availability:1");
    }

    @Test
    public void testGetAvailability_FromDatabase() {
        when(valueOperations.get("inventory_availability:1")).thenReturn(null);

        InventoryItem item = new InventoryItem();
        item.setItemId(1L);
        item.setTotalQuantity(10);
        item.setReservedQuantity(4);

        when(inventoryItemRepository.findByItemId(1L)).thenReturn(Optional.of(item));

        int availability = inventoryService.getAvailability(1L);

        assertThat(availability).isEqualTo(6);
        verify(redisTemplate.opsForValue()).set("inventory_availability:1", 6);
    }

    @Test
    public void testGetAvailability_ItemNotFound() {
        when(valueOperations.get("inventory_availability:1")).thenReturn(null);
        when(inventoryItemRepository.findByItemId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getAvailability(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Inventory item is not found with id " + 1L);
    }

}