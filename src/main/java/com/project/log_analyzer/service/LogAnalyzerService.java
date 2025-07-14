package com.project.log_analyzer.service;

import com.project.log_analyzer.model.EndpointStats;
import com.project.log_analyzer.util.LogParser;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Service
public class LogAnalyzerService
{
    public List<EndpointStats> analyzeLogFile(MultipartFile file) throws IOException{

        //save to temporary file
        File tempFile = File.createTempFile("uploaded-", ".log");
        file.transferTo(tempFile);

        //parser utility
        LogParser parser = new LogParser();
        List<EndpointStats> stats = parser.parse(tempFile);

        return stats;

    }

    public InputStreamResource generateCsvFile(List<EndpointStats> stats) throws IOException{
        // Create a temporary file
        File csvFile = File.createTempFile("log-analysis-", ".csv");

        try (PrintWriter writer = new PrintWriter(csvFile)){
            // Write header
            writer.println("Endpoint,Success,Failure,SuccessRate,FailureRate");

            //Write data rows
            for (EndpointStats stat : stats){
                writer.printf("%s,%d,%d,%.2f,%.2f%n",
                        stat.getEndpoint(),
                        stat.getSuccess(),
                        stat.getFailure(),
                        stat.getSuccessRate(),
                        stat.getFailureRate());
            }
        }
        return new InputStreamResource(new FileInputStream(csvFile));
    }
}

