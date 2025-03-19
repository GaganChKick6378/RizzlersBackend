package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.in.RoomTypeImageRequest;
import com.kdu.rizzlers.dto.out.RoomTypeImageResponse;

import java.util.List;

public interface RoomTypeImageService {
    RoomTypeImageResponse createRoomTypeImage(RoomTypeImageRequest request);
    RoomTypeImageResponse getRoomTypeImageById(Long id);
    List<RoomTypeImageResponse> getAllRoomTypeImages();
    List<RoomTypeImageResponse> getRoomTypeImagesByTenantId(Integer tenantId);
    List<RoomTypeImageResponse> getRoomTypeImagesByPropertyId(Integer propertyId);
    List<RoomTypeImageResponse> getRoomTypeImagesByRoomTypeId(Integer roomTypeId);
    List<RoomTypeImageResponse> getRoomTypeImagesByTenantIdAndPropertyId(Integer tenantId, Integer propertyId);
    List<RoomTypeImageResponse> getRoomTypeImagesByTenantIdAndPropertyIdAndRoomTypeId(
            Integer tenantId, Integer propertyId, Integer roomTypeId);
    RoomTypeImageResponse getRoomTypeImageByTenantIdAndPropertyIdAndRoomTypeIdAndDisplayOrder(
            Integer tenantId, Integer propertyId, Integer roomTypeId, Integer displayOrder);
    RoomTypeImageResponse updateRoomTypeImage(Long id, RoomTypeImageRequest request);
    void deleteRoomTypeImage(Long id);
} 