package com.example.green.api.controller;

import com.example.green.api.dto.request.WasteDepositRequestDto;
import com.example.green.api.dto.response.EcoPointContainerResponseDto;
import com.example.green.api.dto.response.WasteLogResponseDto;
import com.example.green.service.EcoPointsActionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eco-points")
@RequiredArgsConstructor
@Validated
public class EcoPointsActionController {
    private final EcoPointsActionService actionService;

    // 获取所有激活状态的回收箱列表及实时满溢度
    @GetMapping
    public List<EcoPointContainerResponseDto> getActiveContainers() {
        return actionService.getActiveContainers();
    }

    // 接收 qrCodeToken 与投递参数进行积分计算并更新数据
    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public WasteLogResponseDto deposit(@Valid @RequestBody WasteDepositRequestDto request) {
        // JwtAuthenticationFilter 在拦截时已经将解析得到的 userId 作为 Principal 存入上下文
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return actionService.processDeposit(userId, request);
    }
}