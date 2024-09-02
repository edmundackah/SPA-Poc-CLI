package com.example.cli.s3.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class SnowBrokerStubs {

    public static void validChangeRecordStub() {
        stubFor(get(urlEqualTo("/change/MCR18434340"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"isValid\": \"true\",\n" +
                                "  \"key\": {\n" +
                                "    \"accessKeyId\": \"CnIjsDcI\",\n" +
                                "    \"secretKey\": \"PV59ypmisU\"\n" +
                                "  }\n" +
                                "}")));

        stubFor(get(urlEqualTo("/incident/INC23434114"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"isValid\": \"true\",\n" +
                                "  \"key\": {\n" +
                                "    \"accessKeyId\": \"CnIjsDcI\",\n" +
                                "    \"secretKey\": \"PV59ypmisU\"\n" +
                                "  }\n" +
                                "}")));

        //TODO: Add response body from 404 request
        stubFor(get(urlEqualTo("/incident/INC000111"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        ));

        stubFor(get(urlEqualTo("/change/MCR000111"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                ));

        //Code 500 response
        stubFor(get(urlEqualTo("/change/MCR717000"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                ));

        stubFor(get(urlEqualTo("/incident/INC717000"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                ));
    }

}
