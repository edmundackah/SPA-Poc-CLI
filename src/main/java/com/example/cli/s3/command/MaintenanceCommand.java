package com.example.cli.s3.command;

import com.example.cli.s3.constants.Constants;
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
    public String deployMaintenance(@Option(description = Constants.CHANGE_RECORD) String changeRecord,
                                    @Option(required = true, description = Constants.FLAGS) String flags,
                                    @Option(required = true, description = Constants.FLAG_STATE) FlagState state,
                                    @Option(required = true, description = Constants.BUCKET_NAME) String bucketName,
                                    @Option(required = true, description = Constants.TARGET_SERVER) TargetServer server) {

        return maintenanceService.updateFlags(bucketName, flags, state, changeRecord, server);
    }

    @Command(command = "verify-maintenance")
    public String verifyMaintenance(@Option(description = Constants.CHANGE_RECORD) String changeRecord,
                                    @Option(required = true, description = Constants.BUCKET_NAME) String bucketName,
                                    @Option(required = true, description = Constants.TARGET_SERVER) TargetServer server) {

        return maintenanceService.displayFlags(bucketName, changeRecord, server);
    }
}
