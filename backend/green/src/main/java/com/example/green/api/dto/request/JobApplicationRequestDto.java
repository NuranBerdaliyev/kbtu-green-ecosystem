package com.example.green.api.dto.request;

import com.example.green.domain.enums.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobApplicationRequestDto {
    @NotNull
    private Long vacancyId;

    @NotNull
    private Long studentId;

    @NotNull
    private LocalDateTime appliedAt;

    @NotBlank
    @Size(min = 10, max = 5000)
    private String coverLetter;

    @NotNull
    private JobStatus jobStatus;
}
