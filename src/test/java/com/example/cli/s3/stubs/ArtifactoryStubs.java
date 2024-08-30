package com.example.cli.s3.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class ArtifactoryStubs {

    public static void tarFile() {
        stubFor(get(urlEqualTo("/download/prod-payload.tgz"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/zip")
                        .withHeader("Content-Disposition", "attachment; filename=prod-payload.tgz")
                        .withBodyFile("src/test/resources/files/prod-payload.tgz")));
    }
}
