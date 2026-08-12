package com.example.green.api.mapper;

import com.example.green.api.dto.request.WasteLogRequestDto;
import com.example.green.api.dto.response.WasteLogResponseDto;
import com.example.green.domain.entity.EcoPointContainer;
import com.example.green.domain.entity.User;
import com.example.green.domain.entity.WasteLog;
import org.springframework.stereotype.Component;

@Component
public class WasteLogMapper {
    public WasteLog toEntity(WasteLogRequestDto dto, User user, EcoPointContainer ecoPointContainer) {
        return WasteLog.builder()
                .user(user)
                .ecoPointContainer(ecoPointContainer)
                .scannedAt(dto.getScannedAt())
                .ecoCoinsEarned(dto.getEcoCoinsEarned())
                .build();
    }

    public void updateEntity(WasteLog entity, WasteLogRequestDto dto, User user, EcoPointContainer ecoPointContainer) {
        entity.setUser(user);
        entity.setEcoPointContainer(ecoPointContainer);
        entity.setScannedAt(dto.getScannedAt());
        entity.setEcoCoinsEarned(dto.getEcoCoinsEarned());
    }

    public WasteLogResponseDto toDto(WasteLog entity) {
        return WasteLogResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .ecoPointContainerId(entity.getEcoPointContainer().getId())
                .scannedAt(entity.getScannedAt())
                .ecoCoinsEarned(entity.getEcoCoinsEarned())
                .build();
    }
}
