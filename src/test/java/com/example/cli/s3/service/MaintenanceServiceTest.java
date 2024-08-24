package com.example.cli.s3.service;

import com.example.cli.s3.enums.FlagState;
import com.example.cli.s3.enums.TargetServer;
import com.example.cli.s3.utils.S3Util;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock
    private S3Util s3Util;

    @InjectMocks
    private MaintenanceService maintenanceService;

    @Test
    void testUpdateStates_WhenFileExistsAndFlagsPresent() {
        // Given
        String bucketName = "test-bucket";
        String flags = "flag1,flag2";
        FlagState state = FlagState.ON;  // Use the ON enum value
        String changeRecord = "change-record";
        TargetServer server = TargetServer.AWS_S3; // Use the TargetServer enum

        ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
        rootNode.put("flag1", "off");
        rootNode.put("flag2", "off");

        when(s3Util.getMaintenanceFile(bucketName, server, changeRecord)).thenReturn(rootNode);

        maintenanceService.updateStates(bucketName, flags, state, true, changeRecord, server);

        assertThat(rootNode.get("flag1").asText()).isEqualTo(state.getValue());
        assertThat(rootNode.get("flag2").asText()).isEqualTo(state.getValue());
        verify(s3Util, times(1)).saveMaintenanceFile(bucketName, rootNode, server, changeRecord);
    }

    @Test
    void testUpdateStates_WhenFileDoesNotExistAndAddIfMissingTrue() {
        String bucketName = "test-bucket";
        String flags = "flag1,flag3";
        FlagState state = FlagState.OFF;
        String changeRecord = "change-record";
        TargetServer server = TargetServer.AWS_S3_PROD;

        ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
        rootNode.put("flag1", "on");

        when(s3Util.getMaintenanceFile(bucketName, server, changeRecord)).thenReturn(rootNode);

        maintenanceService.updateStates(bucketName, flags, state, true, changeRecord, server);

        assertThat(rootNode.get("flag1").asText()).isEqualTo(state.getValue());
        assertThat(rootNode.get("flag3").asText()).isEqualTo(state.getValue());
        verify(s3Util, times(1)).saveMaintenanceFile(bucketName, rootNode, server, changeRecord);
    }

    @Test
    void testUpdateStates_WhenMissingFlagsAndAddIfMissingFalse() {
        // Given
        String bucketName = "test-bucket";
        String flags = "flag1,flag3";
        FlagState state = FlagState.OFF;
        String changeRecord = "change-record";
        TargetServer server = TargetServer.ECS_S3; // Use the TargetServer enum

        ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
        rootNode.put("flag1", "on");

        when(s3Util.getMaintenanceFile(bucketName, server, changeRecord)).thenReturn(rootNode);

        maintenanceService.updateStates(bucketName, flags, state, false, changeRecord, server);

        assertThat(rootNode.has("flag3")).isFalse();
        verify(s3Util, never()).saveMaintenanceFile(bucketName, rootNode, server, changeRecord);
    }

    @Test
    void testDisplayStates() {
        String bucketName = "test-bucket";
        String changeRecord = "change-record";
        TargetServer server = TargetServer.ECS_S3_PROD; // Use the TargetServer enum

        ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
        rootNode.put("flag1", "on");
        rootNode.put("flag2", "off");

        when(s3Util.getMaintenanceFile(bucketName, server, changeRecord)).thenReturn(rootNode);

        String result = maintenanceService.displayStates(bucketName, changeRecord, server);

        assertThat(result).contains("Flag").contains("State");
        assertThat(result).contains("flag1", "on");
        assertThat(result).contains("flag2", "off");
    }
}