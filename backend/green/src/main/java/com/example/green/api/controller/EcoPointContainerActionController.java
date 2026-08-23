package com.example.green.api.controller;

import com.example.green.api.dto.request.WasteDepositRequestDto;
import com.example.green.api.dto.response.EcoPointContainerResponseDto;
import com.example.green.api.dto.response.WasteLogResponseDto;
import com.example.green.service.EcoPointsContainerActionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eco-points")
@RequiredArgsConstructor
@Validated
public class EcoPointContainerActionController {
    private final EcoPointsContainerActionService actionService;

    // 获取所有激活状态的回收箱列表及实时满溢度
    @GetMapping
    public List<EcoPointContainerResponseDto> getActiveContainers() {
        return actionService.getActiveContainers();
    }

    // 接收 qrCodeToken 与投递参数进行积分计算并更新数据
    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    public WasteLogResponseDto deposit(@Valid @RequestBody WasteDepositRequestDto request) {
        return actionService.processDeposit(request);
    }
}