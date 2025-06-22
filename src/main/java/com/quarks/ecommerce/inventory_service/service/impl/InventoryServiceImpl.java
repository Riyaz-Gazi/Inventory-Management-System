package com.quarks.ecommerce.inventory_service.service.impl;

import com.quarks.ecommerce.inventory_service.dto.CreateSupplyRequestDto;
import com.quarks.ecommerce.inventory_service.dto.InventoryDto;
import com.quarks.ecommerce.inventory_service.entity.InventoryItem;
import com.quarks.ecommerce.inventory_service.entity.Reservation;
import com.quarks.ecommerce.inventory_service.entity.enums.ReservationStatus;
import com.quarks.ecommerce.inventory_service.exceptions.ResourceNotFoundException;
import com.quarks.ecommerce.inventory_service.repository.InventoryItemRepository;
import com.quarks.ecommerce.inventory_service.repository.ReservationRepository;
import com.quarks.ecommerce.inventory_service.service.InventoryService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final String INVENTORY_CACHE_PREFIX = "inventory_availability:";

    private final InventoryItemRepository inventoryItemRepository;
    private final ReservationRepository reservationRepository;
    private final ModelMapper modelMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public InventoryServiceImpl(InventoryItemRepository inventoryItemRepository, ReservationRepository reservationRepository, ModelMapper modelMapper, RedisTemplate<String, Object> redisTemplate) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.reservationRepository = reservationRepository;
        this.modelMapper = modelMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public InventoryDto createOrUpdateSupply(CreateSupplyRequestDto createSupplyRequestDto) {
        Optional<InventoryItem> optionalInventoryItem = inventoryItemRepository.findByItemId(createSupplyRequestDto.getItemId());
        InventoryItem inventoryItem;
        if (optionalInventoryItem.isPresent()) {
            inventoryItem = optionalInventoryItem.get();
            if (createSupplyRequestDto.getVersion() != null && !createSupplyRequestDto.getVersion().equals(inventoryItem.getVersion())) {
                throw new OptimisticLockingFailureException("Version mismatch. Item might have been updated by someone else.");
            }
            inventoryItem.addSupply(createSupplyRequestDto.getQuantity());
        } else {
            inventoryItem = new InventoryItem();
            inventoryItem.setItemId(createSupplyRequestDto.getItemId());
            inventoryItem.setName(createSupplyRequestDto.getName());
            inventoryItem.setTotalQuantity(createSupplyRequestDto.getQuantity());
            inventoryItem.setReservedQuantity(0);
        }

        InventoryItem savedInventoryItem = inventoryItemRepository.save(inventoryItem);

        String cacheKey = INVENTORY_CACHE_PREFIX + savedInventoryItem.getItemId();
        int availableQuantity = savedInventoryItem.getAvailableQuantity();
        redisTemplate.opsForValue().set(cacheKey, availableQuantity);

        return modelMapper.map(savedInventoryItem, InventoryDto.class);
    }

    @Override
    @Transactional
    public String reserveItem(Long itemId, int quantity, String reservedBy) {
        int maxRetries = 3;
        int attempts = 0;

        while (attempts < maxRetries) {
            try {
                InventoryItem inventoryItem = inventoryItemRepository.findByItemId(itemId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + itemId));

                if (inventoryItem.reserve(quantity)) {
                    Reservation reservation = new Reservation();
                    reservation.setItemId(itemId);
                    reservation.setQuantity(quantity);
                    reservation.setReservationStatus(ReservationStatus.RESERVED);
                    reservation.setCreatedAt(LocalDateTime.now());
                    reservation.setReservedBy(reservedBy);
                    reservation.setReservationToken(UUID.randomUUID().toString());

                    reservationRepository.save(reservation);
                    inventoryItemRepository.saveAndFlush(inventoryItem);

                    String cacheKey = INVENTORY_CACHE_PREFIX + itemId;
                    int updatedAvailableQuantity = inventoryItem.getAvailableQuantity();
                    redisTemplate.opsForValue().set(cacheKey, updatedAvailableQuantity);

                    return reservation.getReservationToken();
                } else {
                    return null;
                }

            } catch (ObjectOptimisticLockingFailureException e) {
                attempts++;
                if (attempts == maxRetries) {
                    throw new RuntimeException("Reservation failed due to concurrent updates. Please try again.");
                }
            }
        }

        return null;
    }

    @Override
    @Transactional
    public String cancelReservation(String reservationToken) {
        Reservation reservation = reservationRepository.findByReservationToken(reservationToken).orElseThrow(() -> new RuntimeException("token is not valid"));
        if (reservation.getReservationStatus().equals(ReservationStatus.RESERVED)) {
            InventoryItem inventoryItem = inventoryItemRepository.findByItemId(reservation.getItemId()).orElseThrow(() -> new ResourceNotFoundException("product is not found with item id " + reservation.getItemId()));
            inventoryItem.cancelReservation(reservation.getQuantity());
            reservation.setReservationStatus(ReservationStatus.CANCELLED);
            inventoryItemRepository.save(inventoryItem);
            reservationRepository.save(reservation);

            String cacheKey = INVENTORY_CACHE_PREFIX + reservation.getItemId();
            int updatedAvailableQuantity = inventoryItem.getAvailableQuantity();
            redisTemplate.opsForValue().set(cacheKey, updatedAvailableQuantity);

            return "Reservation cancelled";
        }
        return "Reservation already cancelled";
    }

    @Override
    public int getAvailability(Long itemId) {
        String cacheKey = INVENTORY_CACHE_PREFIX + itemId;

        // Try getting from Redis cache
        Integer cachedAvailability = (Integer) redisTemplate.opsForValue().get(cacheKey);
        if (cachedAvailability != null) {
            return cachedAvailability;
        }
        Optional<InventoryItem> optionalInventoryItem = inventoryItemRepository.findByItemId(itemId);
        if (!optionalInventoryItem.isPresent()) {
            throw new ResourceNotFoundException("Inventory item is not found with id " + itemId);
        }
        InventoryItem inventoryItem = optionalInventoryItem.get();
        int availableItemQuantity = inventoryItem.getAvailableQuantity();
        redisTemplate.opsForValue().set(cacheKey, availableItemQuantity);

        return availableItemQuantity;
    }

}
