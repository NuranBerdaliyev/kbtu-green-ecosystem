package com.example.green.api.dto.response;

import com.example.green.domain.enums.JobStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobApplicationResponseDto {
    private Long id;
    private Long vacancyId;
    private Long studentId;
    private LocalDateTime appliedAt;
    private String coverLetter;
    private JobStatus jobStatus;
}
