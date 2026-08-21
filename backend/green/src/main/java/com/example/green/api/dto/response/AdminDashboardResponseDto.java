package com.example.green.api.dto.response;

import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminDashboardResponseDto {

    private Long totalUsers;
    private Long students;
    private Long employees;
    private Long hrManagers;

    private Long totalContainers;
    private Long activeContainers;
    private Long criticalContainers;

    private Long totalCompanies;
    private Long partnerCompanies;

    private Long totalVacancies;
    private Long activeVacancies;
    private Long totalApplications;

    private Long totalEcoTransactions;
}