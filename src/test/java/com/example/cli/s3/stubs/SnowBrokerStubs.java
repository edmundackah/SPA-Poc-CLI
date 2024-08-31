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
    }

}
