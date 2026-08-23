package com.example.green.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WasteDepositRequestDto {
    @NotBlank
    private String qrCodeToken;

    // 垃圾投递参数：重量(克)，用于供Gamification系统计算积分与碳减排
    @NotNull
    @Min(1)
    private Integer wasteWeightGrams;
}