package com.example.green.service;

import com.example.green.api.dto.request.WasteDepositRequestDto;
import com.example.green.api.dto.response.EcoPointContainerResponseDto;
import com.example.green.api.dto.response.WasteLogResponseDto;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.EcoPointContainerMapper;
import com.example.green.api.mapper.WasteLogMapper;
import com.example.green.domain.entity.EcoPointContainer;
import com.example.green.domain.entity.User;
import com.example.green.domain.entity.WasteLog;
import com.example.green.domain.repository.EcoPointContainerRepository;
import com.example.green.domain.repository.UserRepository;
import com.example.green.domain.repository.WasteLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EcoPointsActionService {
    private final EcoPointContainerRepository containerRepo;
    private final WasteLogRepository wasteLogRepo;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket 推送模版
    private final EcoPointContainerMapper containerMapper;
    private final WasteLogMapper wasteLogMapper;

    @Transactional(readOnly = true)
    public List<EcoPointContainerResponseDto> getActiveContainers() {
        return containerRepo.findByIsActiveTrue()
                .stream()
                .map(containerMapper::toDto)
                .toList();
    }

    @Transactional
    public WasteLogResponseDto processDeposit(Long userId, WasteDepositRequestDto request) {
        // 1. 校验二维码与激活状态
        EcoPointContainer container = containerRepo.findByQrCodeToken(request.getQrCodeToken())
                .orElseThrow(() -> new ResourceNotFoundException("Неверный QR-код или контейнер не найден"));

        if (!container.getIsActive()) {
            throw new IllegalStateException("Контейнер не активен");
        }

        // 2. 更新满溢度
        int newFullness = container.getFullnessPercentage() + request.getAddedFullnessPercentage();
        if (newFullness > 100) newFullness = 100;
        container.setFullnessPercentage(newFullness);
        containerRepo.save(container);

        // 3. WebSocket 实时广播
        EcoPointContainerResponseDto containerDto = containerMapper.toDto(container);
        messagingTemplate.convertAndSend("/topic/eco-containers", containerDto);

        // 若满溢度超 90%，发送管理员阈值预警
        if (newFullness >= 90) {
            messagingTemplate.convertAndSend("/topic/admin/alerts",
                    "Внимание! Контейнер ID " + container.getId() + " заполнен на " + newFullness + "%!");
        }

        // 4. 计算奖励并发放积分与 ESG
        int earnedCoins = gamificationService.processDepositRewards(userId, container.getId(), request.getWasteWeightGrams());

        // 5. 记录投递日志
        User user = userRepository.getReferenceById(userId);
        WasteLog log = WasteLog.builder()
                .user(user)
                .ecoPointContainer(container)
                .scannedAt(LocalDateTime.now())
                .ecoCoinsEarned(earnedCoins)
                .build();
        wasteLogRepo.save(log);

        return wasteLogMapper.toDto(log);
    }
}