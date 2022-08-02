package com.yaniv.cli.commands;

import picocli.CommandLine;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class GitCommandTemplate {

    public GitCommandTemplate() {
        System.setOut(System.out);

    }

    @CommandLine.Option(names = {"-r", "--repo"}, description = "The repository to analyze")
    protected String repo;

    @CommandLine.Option(names = {"-o", "--output"}, description = "The output path of the txt file")
    protected String output;

    @CommandLine.Option(names = {"-h", "--help"}, description = "Print information about each command")
    protected String help;


    protected void validateInput() {
        if (repo == null) {
            throw new RuntimeException("Expected to get repo/owner name");
        }
        boolean valid = repo.split("/").length == 2;
        if (!valid) {
            throw new RuntimeException(String.format("Bad -r argument '%s'. Expected to get repo/owner name", repo));
        }
    }

    protected CompletableFuture<HttpResponse<String>> fetchDataAsync(URI uri) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest getRequest = HttpRequest.newBuilder(uri).build();
        return httpClient.sendAsync(getRequest, HttpResponse.BodyHandlers.ofString());

    }
    protected void setOutput() {
        if (this.repo != null)
            System.out.printf("Fetching repo %s to %s%n", repo, output == null ? "console" : output);
        else {
            System.out.print("Missing repo parameter");
        }
    }
}
