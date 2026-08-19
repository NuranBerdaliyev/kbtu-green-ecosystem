package com.example.green.api.dto.response;

import com.example.green.domain.enums.JobStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CandidateResponseDto {
    private Long applicationId;
    private Long studentId;
    private String fullName;
    private Integer esgRating;
    private Long ecoCoinsBalance;
    private BigDecimal totalCo2Saved;
    private String coverLetter;
    private LocalDateTime appliedAt;
    private JobStatus jobStatus;
}