package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.RoomTypeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomTypeImageRepository extends JpaRepository<RoomTypeImage, Long> {
    List<RoomTypeImage> findByTenantId(Integer tenantId);
    List<RoomTypeImage> findByPropertyId(Integer propertyId);
    List<RoomTypeImage> findByRoomTypeId(Integer roomTypeId);
    List<RoomTypeImage> findByTenantIdAndPropertyId(Integer tenantId, Integer propertyId);
    List<RoomTypeImage> findByTenantIdAndPropertyIdAndRoomTypeId(Integer tenantId, Integer propertyId, Integer roomTypeId);
    Optional<RoomTypeImage> findByTenantIdAndPropertyIdAndRoomTypeIdAndDisplayOrder(
            Integer tenantId, Integer propertyId, Integer roomTypeId, Integer displayOrder);
} 