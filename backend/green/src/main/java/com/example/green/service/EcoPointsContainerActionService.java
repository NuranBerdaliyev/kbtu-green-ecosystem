package com.example.green.service;

import com.example.green.api.dto.request.WasteDepositRequestDto;
import com.example.green.api.dto.response.EcoPointContainerResponseDto;
import com.example.green.api.dto.response.WasteLogResponseDto;
import com.example.green.api.error.ForbiddenException;
import com.example.green.api.error.ResourceNotFoundException;
import com.example.green.api.mapper.EcoPointContainerMapper;
import com.example.green.api.mapper.WasteLogMapper;
import com.example.green.config.GamificationProperties;
import com.example.green.domain.entity.EcoPointContainer;
import com.example.green.domain.entity.User;
import com.example.green.domain.entity.WasteLog;
import com.example.green.domain.enums.Role;
import com.example.green.domain.enums.WasteDepositStatus;
import com.example.green.domain.model.RewardResult;
import com.example.green.domain.repository.EcoPointContainerRepository;
import com.example.green.domain.repository.UserRepository;
import com.example.green.domain.repository.WasteLogRepository;
import com.example.green.service.event.EcoPointContainerChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EcoPointsContainerActionService {
    private final EcoPointContainerRepository containerRepository;
    private final WasteLogRepository wasteLogRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final GamificationService gamificationService;
    private final EcoPointContainerMapper containerMapper;
    private final WasteLogMapper wasteLogMapper;
    private final GamificationProperties properties;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<EcoPointContainerResponseDto> getActiveContainers() {
        return containerRepository
                .findByIsActiveTrue()
                .stream()
                .map(containerMapper::toDto)
                .toList();
    }

    /*
     * Пользователь только создаёт заявку.
     * Контейнер и Gamification здесь не изменяются.
     */
    @Transactional
    public WasteLogResponseDto processDeposit(WasteDepositRequestDto request) {
        User user = currentUserService.getCurrentUserOrThrow();

        validateWeight(request.getWasteWeightGrams());

        EcoPointContainer container = containerRepository
                    .findByQrCodeTokenForUpdate(request.getQrCodeToken())
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Invalid QR code or container not found")
                        );

        requireActiveContainer(container);
        validateClaimFitsCurrentCapacity(container, request.getWasteWeightGrams());

        WasteLog log = WasteLog.builder()
                .user(user)
                .ecoPointContainer(container)
                .scannedAt(LocalDateTime.now())
                .ecoCoinsEarned(0)
                .wasteWeightGrams(request.getWasteWeightGrams())
                .wasteType(container.getWasteType())
                .fullnessDeltaPercentage(0)
                .status(WasteDepositStatus.PENDING)
                .reviewedBy(null)
                .reviewedAt(null)
                .build();

        return wasteLogMapper.toDto(wasteLogRepository.save(log));
    }

    /*
     * Только здесь физически меняется виртуальный контейнер
     * и начисляется награда.
     */
    @Transactional
    public WasteLogResponseDto approveDeposit(Long wasteLogId) {
        User admin = requireAdmin();
        WasteLog log = wasteLogRepository
                .findByIdForUpdate(wasteLogId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("WasteLog not found: id=" + wasteLogId)
                );

        if (log.getStatus() != WasteDepositStatus.PENDING) {
            throw new IllegalStateException("Only PENDING waste deposit can be approved");
        }
        validateWeight(log.getWasteWeightGrams());

        /*
         * Блокируем пользователя: два ADMIN не смогут
         * параллельно обойти его дневной лимит через разные
         * заявки.
         */
        User depositor = userRepository
                .findByIdForUpdate(log.getUser().getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Deposit user not found")
                );

        validateDailyApprovedLimit(depositor.getId(), log.getWasteWeightGrams());

        EcoPointContainer container =
                containerRepository
                        .findByIdForUpdate(log.getEcoPointContainer().getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException("EcoPointContainer not found")
                        );

        requireActiveContainer(container);

        /*
         * Если ADMIN успел очистить контейнер и изменить его
         * тип, старая заявка не должна применяться к новому
         * типу отходов.
         */
        if (container.getWasteType() != log.getWasteType()) {
            throw new IllegalStateException("Container waste type changed after deposit request");
        }

        int previousFullness = container.getFullnessPercentage();

        /*
         * Здесь повторно проверяется вместимость.
         * Между созданием заявки и approval контейнер мог
         * заполниться другими подтверждёнными депозитами.
         */
        container.acceptWaste(log.getWasteWeightGrams());

        int currentFullness = container.getFullnessPercentage();

        int fullnessDelta = currentFullness - previousFullness;

        containerRepository.save(container);

        RewardResult reward = gamificationService.rewardForWasteDeposit(
                        depositor.getId(),
                        log.getId(),
                        log.getWasteWeightGrams(),
                        log.getWasteType()
                );

        LocalDateTime reviewedAt = LocalDateTime.now();

        log.approve(
                admin,
                fullnessDelta,
                Math.toIntExact(reward.ecoCoinsEarned()),
                reviewedAt
        );

        WasteLog savedLog = wasteLogRepository.save(log);
        EcoPointContainerResponseDto containerDto = containerMapper.toDto(container);

        /*
         * Listener отправит WebSocket только после commit.
         */
        eventPublisher.publishEvent(
                new EcoPointContainerChangedEvent(
                        containerDto,
                        previousFullness,
                        reviewedAt
                )
        );

        return wasteLogMapper.toDto(savedLog);
    }

    @Transactional
    public WasteLogResponseDto rejectDeposit(Long wasteLogId) {
        User admin = requireAdmin();

        WasteLog log = wasteLogRepository.findByIdForUpdate(wasteLogId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("WasteLog not found: id=" + wasteLogId)
                );

        if (log.getStatus() != WasteDepositStatus.PENDING) {
            throw new IllegalStateException("Only PENDING waste deposit can be rejected");
        }

        log.reject(admin, LocalDateTime.now());

        return wasteLogMapper.toDto(wasteLogRepository.save(log));
    }

    private void validateWeight(int weightGrams) {
        if (weightGrams <= 0) {
            throw new IllegalArgumentException("Waste weight must be greater than zero");
        }

        int maximum = properties
                .getWaste()
                .getMaxWeightPerDepositGrams();

        if (weightGrams > maximum) {
            throw new IllegalStateException("Waste deposit exceeds maximum weight of "
                            + maximum
                            + " grams"
            );
        }
    }

    private void validateDailyApprovedLimit(Long userId, int requestedWeightGrams) {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = today.plusDays(1).atStartOfDay();

        Long approvedWeight =
                wasteLogRepository.sumWeightByUserAndStatusBetween(
                                userId,
                                WasteDepositStatus.APPROVED,
                                from,
                                to
                        );

        long currentWeight = approvedWeight == null
                        ? 0L
                        : approvedWeight;

        long newDailyWeight = Math.addExact(currentWeight, requestedWeightGrams);

        int maximum = properties
                .getWaste()
                .getMaxDailyApprovedWeightGrams();

        if (newDailyWeight > maximum) {
            throw new IllegalStateException("Daily approved waste limit exceeded");
        }
    }

    private void validateClaimFitsCurrentCapacity(EcoPointContainer container, int weightGrams) {
        int currentWeight = container.getCurrentWeightGrams() == null
                        ? 0
                        : container.getCurrentWeightGrams();

        if (currentWeight >= container.getCapacityGrams()) {
            throw new IllegalStateException("Container is full");
        }

        long claimedWeight = (long) currentWeight + weightGrams;

        if (claimedWeight > container.getCapacityGrams()) {
            throw new IllegalStateException("Waste weight exceeds remaining container capacity");
        }
    }

    private void requireActiveContainer(EcoPointContainer container) {
        if (!Boolean.TRUE.equals(container.getIsActive())) {
            throw new IllegalStateException("Container is not active");
        }
    }

    private User requireAdmin() {
        User user = currentUserService.getCurrentUserOrThrow();

        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only administrators can review waste deposits");
        }
        return user;
    }
}