package com.project.log_analyzer.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EndpointStats {
    private String endpoint;
    private int totalRequests;
    private double averageResponseTime;
    private LocalDateTime lastRequestTime;
    private int success;
    private int failure;
    private double successRate;
    private double failureRate;
    private int uniqueIps;

}
