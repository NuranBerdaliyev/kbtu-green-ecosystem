package com.example.green.api.mapper;

import com.example.green.api.dto.response.WasteLogResponseDto;
import com.example.green.domain.entity.WasteLog;
import org.springframework.stereotype.Component;

@Component
public class WasteLogMapper {
    public WasteLogResponseDto toDto(WasteLog entity) {
        return WasteLogResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .ecoPointContainerId(entity.getEcoPointContainer().getId())
                .scannedAt(entity.getScannedAt())
                .ecoCoinsEarned(entity.getEcoCoinsEarned())
                .wasteWeightGrams(entity.getWasteWeightGrams())
                .build();
    }
}
