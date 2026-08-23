package com.example.green.api.dto.request;
import com.example.green.domain.enums.JobStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobApplicationStatusRequestDto {
    @NotNull
    private JobStatus status;
}