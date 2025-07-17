package com.project.log_analyzer.util;

import com.project.log_analyzer.model.EndpointStats;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;


public class LogParser {

    private static final Pattern pattern = Pattern.compile( "^(\\d+\\.\\d+\\.\\d+\\.\\d+) - - \\[(\\d{2}/[A-Za-z]{3}/\\d{4}):([\\d:]+) [^]]+] \"(?:GET|POST|PUT|DELETE|PATCH) (\\S+) HTTP/[^\"]+\" (\\d{3}) \\d+");
    // private: pattern accessible only inside this class
    // static: belongs to this class, not any other instance. Can be used inside the static method parse(...) without needing an object. Since parse() is static (a utility method), the regex must also be static to be usable inside it.
    // final: cannot be reassigned after created



    private static class EndpointAggregator {
        int success = 0;
        int failure = 0;
        int total = 0;
        long totalResponseTime = 0;
        LocalDateTime lastRequestTime;
        Set<String> uniqueIps = new HashSet<>();
    }



    // parse is a method in LogParser class
    public static List<EndpointStats> parse(File logFile) throws IOException{

        Map<String, EndpointAggregator> endpointCounts = new HashMap<>();// a hashmap (java dictionary) with key = endpoint; mapping to an array of integers (success and failure)

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) { // reader is a new instance of buffered reader of file reader reading log file
            String line;
            while ((line = reader.readLine()) != null) {  // while line is not empty
                Matcher matcher = pattern.matcher(line); // find the pattern in the line
                if (matcher.find()) { // if a match is found
                    String ip = matcher.group(1);
                    String date = matcher.group(2);
                    String time = matcher.group(3);
                    String endpoint = matcher.group(4);    // e.g. /api/login
                    int statusCode = Integer.parseInt(matcher.group(5)); // e.g. 200;

                    //Convert to local-date-time
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy HH:mm:ss", Locale.ENGLISH);
                    LocalDateTime requestTime = LocalDateTime.parse(date + " " + time, formatter);

                    endpointCounts.putIfAbsent(endpoint, new EndpointAggregator()); //if nothing, do this?

                    EndpointAggregator aggregator = endpointCounts.get(endpoint);
                    aggregator.total++;

                    if (statusCode >= 200 && statusCode < 300) {
                        aggregator.success++;
                    } else {
                        aggregator.failure++;
                    }
                    aggregator.lastRequestTime = requestTime;
                    aggregator.uniqueIps.add(ip);
                }
            }
        }

        //collecting all stats into one list using '.collect' and 'Collectors'
        return endpointCounts.entrySet()
                .stream()
                .map(entry -> {

                    String endpoint = entry.getKey(); //endpoint is hashmap key
                    EndpointAggregator aggregator = entry.getValue();
                    int total = aggregator.total;
                    double successRate = total > 0 ? (aggregator.success * 100.0 / total) : 0.0;
                    double failureRate = 100.0 - successRate;
                    double avgResponseTime = total > 0 ? (aggregator.totalResponseTime * 1.0 / total) : 0.0;

                    return new EndpointStats(
                            endpoint,
                            total,
                            avgResponseTime,
                            aggregator.lastRequestTime,
                            aggregator.success,
                            aggregator.failure,
                            successRate,
                            failureRate,
                            aggregator.uniqueIps.size()
                    );
                })
                .collect(Collectors.toList());//
    }
}
