package com.yaniv.cli.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import picocli.CommandLine;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@CommandLine.Command(
        name = "download",
        description = "Present the entire downloads for each asset"
)
public class DownloadCommand extends GitCommandTemplate implements Runnable {

    public static final String COL_WIDTH = "| %-20s | %-50s | %16s |%n";


    @Override
    public void run() {
        validateInput();
        setOutput();
        HttpResponse<String> gitHubResponse;
        try {
            gitHubResponse = fetchData();
            if (gitHubResponse.statusCode() < 400) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode arrNode = mapper.readTree(gitHubResponse.body());
                getOutputFormat(arrNode);
            } else {
                throw new IOException(String.format("Github returned status code %s at repo %s could not be fetched", gitHubResponse.statusCode(), repo));
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            System.out.println("Error getting repo downloads for each asset");
            System.out.println(e.getMessage());
        }
    }

    private void getOutputFormat(JsonNode arrNode) throws IOException {
        if (output != null) {
//            write to a text file
            PrintStream ps = new PrintStream(output);
            System.setOut(ps);
        }
        if (arrNode != null && arrNode.isArray() && arrNode.size() > 0) {
            getHeader().forEach(System.out::printf);
            getDataLines(arrNode).forEach(System.out::printf);
        }
        else{
            System.out.println("no asset for this repository");
        }

    }

    private List<String> getDataLines(JsonNode arrNode) {
        String border = "+----------------------------------------------------------------------------------------------+%n";
        final int[] totalDownloads = {0};
        List<String> body = new ArrayList<>();
        ArrayNode releases = (ArrayNode) arrNode;
        releases.forEach(release -> {
            String name = release.get("name").asText();
            ArrayNode assets = (ArrayNode) release.get("assets");
            assets.forEach(asset -> {
                body.add(String.format(COL_WIDTH, name, asset.get("name"), asset.get("download_count")));
                totalDownloads[0] += (asset.get("download_count").asInt());
            });
            body.add(border);
        });

        body.add(String.format(COL_WIDTH, "", "Total", totalDownloads[0]));
        body.add(border);

        return body;

    }

    private List<String> getHeader() {

        List<String> header = new ArrayList<>();
        String border = "+----------------------------------------------------------------------------------------------+%n";
        String format = String.format(COL_WIDTH, "RELEASE NAME", "DISTRIBUTIONS", "DOWNLOAD COUNT");
        header.add(border);
        header.add(format);
        header.add(border);
        return header;
    }

    private HttpResponse<String> fetchData() throws IOException, InterruptedException, ExecutionException {
        return fetchDataAsync(getReposUri()).get();

    }

    private URI getReposUri() {
        String strRepo = String.format("https://api.github.com/repos/%s/releases", repo);
        return URI.create(strRepo);
    }


}
