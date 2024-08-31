package com.example.cli.s3.stubs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class ArtifactoryStubs {

    public static void tarFile() throws IOException {
        // Load the file you want to serve from the local file system
        byte[] fileContent = Files.readAllBytes(Paths.get("src/test/resources/files/prod-payload.tgz"));

        stubFor(get(urlEqualTo("/download/prod-payload.tgz"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/zip")
                        .withHeader("Content-Disposition", "attachment; filename=prod-payload.tgz")
                        .withBody(fileContent)));
    }
}
