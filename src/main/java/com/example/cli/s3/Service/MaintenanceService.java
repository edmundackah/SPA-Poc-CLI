package com.example.cli.s3.Service;

import com.example.cli.s3.client.SnowBrokerClient;
import com.example.cli.s3.enums.FlagState;
import com.example.cli.s3.models.MaintenanceFlag;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceService {

    @Autowired
    private final S3Service s3Service;

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private final SnowBrokerClient snowBrokerClient;


    public void updateStates(String bucketName, String flags, FlagState state, boolean addIfMissing, String changeRecord) throws RuntimeException {

        snowBrokerClient.validateChangeRecord(bucketName, changeRecord);

        JsonNode rootNode = s3Service.getMaintenanceFile(bucketName);
        boolean fileExists = !rootNode.isEmpty();

        ObjectNode rootNodeObject = (ObjectNode) rootNode;
        Set<String> missingFlags = new HashSet<>();
        Arrays.stream(flags.split(","))
                .forEach(flag -> {
                    if (rootNodeObject.has(flag)) {
                        rootNodeObject.put(flag, state.getValue());
                    } else if (addIfMissing) {
                        rootNodeObject.put(flag, state.getValue());
                    } else {
                        missingFlags.add(flag);
                    }
                });

        if (!missingFlags.isEmpty()) {
            log.error("The following flags were not found in the maintenance file: {}", String.join(", ", missingFlags));
            return;
        }

        s3Service.saveMaintenanceFile(bucketName, rootNode);

        if (!fileExists) {
            log.info("Successfully created the maintenance file in bucket: {}", bucketName);
        }
    }

    public String displayStates(String bucketName, String changeRecord) throws RuntimeException {
        snowBrokerClient.validateChangeRecord(bucketName, changeRecord);

        JsonNode rootNode = s3Service.getMaintenanceFile(bucketName);

        List<MaintenanceFlag> flags = mapMaintenanceFlags(rootNode);

        // Create an ASCII table from the JSON data
        return AsciiTable.getTable(flags, Arrays.asList(
                new Column().header("Flag").with(MaintenanceFlag::getFlag),
                new Column().header("State").with(MaintenanceFlag::getState)
        ));
    }

    private List<MaintenanceFlag> mapMaintenanceFlags(JsonNode jsonNode) {
        List<MaintenanceFlag> maintenanceFlags = new ArrayList<>();

        // Iterate through all the fields of the JSON object
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String flag = field.getKey();
            String state = field.getValue().asText(); // Convert JsonNode to String
            maintenanceFlags.add(new MaintenanceFlag(flag, state));
        }

        return maintenanceFlags;
    }
}
