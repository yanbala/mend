package com.yaniv.cli.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import picocli.CommandLine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@CommandLine.Command(
        name = "stats",
        description = "Present the stats of the repo (stars, forks, language, contributors)"
)
public class GitStatCommand extends GitCommandTemplate implements Runnable {
    HttpResponse<String> getStatsResponse;
    HttpResponse<String> getContributorsResponse;

    public static final String COL_WIDTH = "| %-15s | %-10s |%n";
    private String border = "+------------------------------+%n";


    @Override
    public void run() {
        validateInput();
        setOutput();
        try {
            fetchData();
            if (getStatsResponse.statusCode() < 400 && getContributorsResponse.statusCode() < 400) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode arrNodeStats = mapper.readTree(getStatsResponse.body());
                JsonNode arrNodeContributors = mapper.readTree(getContributorsResponse.body());
                getOutputFormat(arrNodeStats, arrNodeContributors);
            } else {
                throw new IOException("Github returned bad status for stats and contributors requests");
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            System.out.println("Error getting repo downloads for each asset");
            System.out.println(e.getMessage());
        }
    }

    private void getOutputFormat(JsonNode arrNodeStats, JsonNode arrNodeContributors) throws FileNotFoundException {
        int numContributors = 0;
        int forks = 0;
        String language = "";
        int stars = 0;
        List<String> body = new ArrayList<>();
        if (output != null) {
            // write to a text file
            PrintStream ps = new PrintStream(output);
            System.setOut(ps);
        }
        if (arrNodeContributors != null) {
            numContributors = arrNodeContributors.size();
        }
        if (arrNodeStats != null) {
            forks = arrNodeStats.get("forks").asInt();
            stars = arrNodeStats.get("stargazers_count").asInt();
            language = arrNodeStats.get("language").asText();
        }
        getHeader().forEach(System.out::printf);
        System.out.printf(String.format(COL_WIDTH, "Stars", stars));
        System.out.printf(String.format(COL_WIDTH, "Forks", forks));
        System.out.printf(String.format(COL_WIDTH, "Contributors", numContributors));
        System.out.printf(String.format(COL_WIDTH, "Language", language));
        System.out.printf(border);

    }
    private List<String> getHeader() {

        List<String> header = new ArrayList<>();

        String format = String.format(COL_WIDTH, "STAT", "VALUE");
        header.add(border);
        header.add(format);
        header.add(border);
        return header;
    }
    private void fetchData() throws IOException, InterruptedException, ExecutionException {
        CompletableFuture<HttpResponse<String>> httpResponseCompletableFuture = fetchDataAsync(getStatsUri());
        CompletableFuture<HttpResponse<String>> httpResponseCompletableFutureContributors = fetchDataAsync(getContributorsUri());
        CompletableFuture.allOf(httpResponseCompletableFuture, httpResponseCompletableFutureContributors).get();
        getContributorsResponse = httpResponseCompletableFutureContributors.get();
        getStatsResponse = httpResponseCompletableFuture.get();
    }

    private URI getStatsUri() {
        String strRepo = String.format("https://api.github.com/repos/%s", repo);
        return URI.create(strRepo);
    }

    private URI getContributorsUri() {
        String strRepo = String.format("https://api.github.com/repos/%s/contributors", repo);
        return URI.create(strRepo);
    }
}
