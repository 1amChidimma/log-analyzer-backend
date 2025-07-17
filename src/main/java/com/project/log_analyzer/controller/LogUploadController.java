package com.project.log_analyzer.controller;

import com.project.log_analyzer.model.EndpointStats;
import com.project.log_analyzer.service.LogAnalyzerService;
import com.project.log_analyzer.service.LogSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/analyze-log")
@RequiredArgsConstructor
public class LogUploadController {

    private final LogAnalyzerService logAnalyzerService;
    private final LogSchedulerService logSchedulerService;

    @PostMapping("/analysis")
    public ResponseEntity<List<EndpointStats>> uploadLogFile(@RequestParam("file")MultipartFile file){
        try{
            List<EndpointStats> stats = logAnalyzerService.analyzeLogFile(file);
            return ResponseEntity.ok(stats);
        }
        catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    // Explanations underway:
    // Tells spring boot to handle post requests at this endpoint:
    @PostMapping("/upload-csv")
    public ResponseEntity<InputStreamResource> uploadCsv(@RequestBody List<EndpointStats> stats){
        // Note that response entity is used for http response containing the actual response
        // Request body reds JSON array sent by frontend and converts into a  List of EndpointStats (objects), called stats
        // Thus each object in frontend must match the back end entity/class.
        try {
            InputStreamResource resource = logAnalyzerService.generateCsvFile(stats);

            HttpHeaders headers = new HttpHeaders(); //used to customize response headers
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=log-analysis.csv");
            // Tells the browser that this is a downloadable file and suggests a file name.
            // Without this file will open in browser instead of downloading

            return ResponseEntity.ok() //http status code: 200 OK
                    .headers(headers) //to add custom header
                    .contentType(MediaType.parseMediaType("text/csv")) //tells browser it is a csv file
                    .body(resource); // sets actual file content
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }

    }


    @GetMapping("/scheduled-stats")
    public ResponseEntity<List<EndpointStats>> getStats(){
        List<EndpointStats> response = logSchedulerService.getLatestStats();
        return ResponseEntity.ok(response);
    }
}
