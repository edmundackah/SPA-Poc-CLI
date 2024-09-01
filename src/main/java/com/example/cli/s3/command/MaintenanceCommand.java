package com.example.cli.s3.command;

import com.example.cli.s3.constants.HelpMessages;
import com.example.cli.s3.enums.FlagState;
import com.example.cli.s3.enums.TargetServer;
import com.example.cli.s3.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

@Slf4j
@RequiredArgsConstructor
@Command(group = "Toggle Maintenance Flags")
public class MaintenanceCommand {

    @Autowired
    private final MaintenanceService maintenanceService;

    @Command(command = "deploy-maintenance")
    public void deployMaintenance(@Option(required = true, description = HelpMessages.FLAGS) String flags,
                                  @Option(required = true, description = HelpMessages.FLAG_STATE) FlagState state,
                                  @Option(required = true, description = HelpMessages.BUCKET_NAME) String bucketName,
                                  @Option(required = true, description = HelpMessages.TARGET_SERVER) TargetServer targetServer,
                                  @Option(description = HelpMessages.ADD_IF_MISSING) Boolean addIfMissing,
                                  @Option(description = HelpMessages.CHANGE_RECORD) String changeRecord) {

        maintenanceService.updateStates(bucketName, flags, state, addIfMissing, changeRecord, targetServer);
    }

    @Command(command = "verify-maintenance")
    public String verifyMaintenance(@Option(description = HelpMessages.CHANGE_RECORD) String changeRecord,
                                    @Option(required = true, description = HelpMessages.BUCKET_NAME) String bucketName,
                                    @Option(required = true, description = HelpMessages.TARGET_SERVER) TargetServer targetServer) {

        return maintenanceService.displayStates(bucketName, changeRecord, targetServer);
    }
}
