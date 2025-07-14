package com.project.log_analyzer.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EndpointStats {
    private String endpoint;
    private int success;
    private int failure;
    private double successRate;
    private double failureRate;
}
