package com.example.green.api.mapper;

import com.example.green.api.dto.request.EcoPointContainerRequestDto;
import com.example.green.api.dto.response.EcoPointContainerResponseDto;
import com.example.green.domain.entity.EcoPointContainer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EcoPointContainerMapper {
    private final GeometryMapper geometryMapper;

    public EcoPointContainer toEntity(EcoPointContainerRequestDto dto) {
        return EcoPointContainer.builder()
                .title(dto.getTitle())
                .location(geometryMapper.fromWkt(dto.getLocationWkt()))
                .wasteType(dto.getWasteType())
                .fullnessPercentage(dto.getFullnessPercentage())
                .isActive(dto.getIsActive())
                .qrCodeToken(dto.getQrCodeToken())
                .build();
    }

    public void updateEntity(EcoPointContainer entity, EcoPointContainerRequestDto dto) {
        entity.setTitle(dto.getTitle());
        entity.setLocation(geometryMapper.fromWkt(dto.getLocationWkt()));
        entity.setWasteType(dto.getWasteType());
        entity.setFullnessPercentage(dto.getFullnessPercentage());
        entity.setIsActive(dto.getIsActive());
        entity.setQrCodeToken(dto.getQrCodeToken());
    }

    public EcoPointContainerResponseDto toDto(EcoPointContainer entity) {
        return EcoPointContainerResponseDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .locationWkt(geometryMapper.toWkt(entity.getLocation()))
                .wasteType(entity.getWasteType())
                .fullnessPercentage(entity.getFullnessPercentage())
                .isActive(entity.getIsActive())
                .qrCodeToken(entity.getQrCodeToken())
                .build();
    }
}
