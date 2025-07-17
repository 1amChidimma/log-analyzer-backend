package com.project.log_analyzer.service;

import com.project.log_analyzer.model.EndpointStats;
import com.project.log_analyzer.util.LogParser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogSchedulerService {

    private List<EndpointStats> latestStats = new ArrayList<>();

    @Scheduled(fixedRate = 300000) // 5mins * 60seconds * 1000 milliseconds
    public void analyzeLogs(){
        Path logPath = Paths.get("C:", "Users", "CHIDIMMA", "Documents", "log_analyzer", "access.log");
        File logFile = logPath.toFile();

        try {
            latestStats = LogParser.parse(logFile); // calling log parser - my util package - read log file
            System.out.println("Log analysis updated");
        } catch (IOException e) {
            System.err.println("Failed to read log file: " + e.getMessage());
        }
    }

    public List<EndpointStats> getLatestStats(){
        return latestStats;
    }
}

