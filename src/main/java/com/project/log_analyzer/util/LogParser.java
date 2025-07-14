package com.project.log_analyzer.util;

import com.project.log_analyzer.model.EndpointStats;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;


public class LogParser {

    private static final Pattern pattern =
            Pattern.compile("\"(GET|POST|PUT|DELETE) (/[^ ]*) HTTP.*?\" (\\d{3})");
    // private: pattern accessible only inside this class
    // static: belongs to this class, not any other instance. Can be used inside the static method parse(...) without needing an object. Since parse() is static (a utility method), the regex must also be static to be usable inside it.
    // final: cannot be reassigned after created


    // parse is a method in LogParser class
    public static List<EndpointStats> parse(File logFile) throws IOException{

        Map<String, int[]> endpointCounts = new HashMap<>();// a hashmap (java dictionary) with key = endpoint; mapping to an array of integers (success and failure)

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) { // reader is a new instance of buffered reader of file reader reading log file
            String line;
            while ((line = reader.readLine()) != null) {  // while line is not empty
                Matcher matcher = pattern.matcher(line); // find the pattern in the line
                if (matcher.find()) { // if a match is found
                    String method = matcher.group(1);      // GET, POST, etc.
                    String endpoint = matcher.group(2);    // e.g. /api/login
                    int statusCode = Integer.parseInt(matcher.group(3)); // e.g. 200; Has to be changed from string to int tho

                    endpointCounts.putIfAbsent(endpoint, new int[]{0, 0}); //if nothing, do this?

                    if (statusCode >= 200 && statusCode < 300) {
                        endpointCounts.get(endpoint)[0]++; // success
                    } else {
                        endpointCounts.get(endpoint)[1]++; // failure
                    }
                }
            }
        }

        //collecting all stats into one list using '.collect' and 'Collectors'
        return endpointCounts.entrySet()
                .stream()
                .map(entry -> {
                    String endpoint = entry.getKey(); //endpoint is hashmap key
                    int success = entry.getValue()[0]; // first index in value array
                    int failure = entry.getValue()[1]; // second index in value array
                    int total = success + failure;

                    // calculating rates of success and failure
                    double successRate = total > 0 ? (success * 100.0 / total) : 0.0;//
                    double failureRate = 100.0 - successRate;

                    return new EndpointStats(endpoint, success, failure, successRate, failureRate);
                })
                .collect(Collectors.toList());//
    }
}
