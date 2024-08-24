package com.example.cli.s3.command;

import com.example.cli.s3.constants.HelpMessages;
import com.example.cli.s3.enums.FlagState;
import com.example.cli.s3.enums.TargetServer;
import com.example.cli.s3.service.MaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@Slf4j
@ShellComponent
@RequiredArgsConstructor
@ShellCommandGroup("Toggle Maintenance Flags")
public class MaintenanceCommand {

    @Autowired
    private final MaintenanceService maintenanceService;

    @ShellMethod(key = "deploy-maintenance")
    public void deployMaintenance(@ShellOption(help = HelpMessages.FLAGS) String flags,
                                  @ShellOption(help = HelpMessages.FLAG_STATE) FlagState state,
                                  @ShellOption(help = HelpMessages.BUCKET_NAME) String bucketName,
                                  @ShellOption(help = HelpMessages.TARGET_SERVER) TargetServer targetServer,
                                  @ShellOption(defaultValue = "true", help = HelpMessages.ADD_IF_MISSING) Boolean addIfMissing,
                                  @ShellOption(defaultValue = "", help = HelpMessages.CHANGE_RECORD) String changeRecord) {

        maintenanceService.updateStates(bucketName, flags, state, addIfMissing, changeRecord, targetServer);
    }

    @ShellMethod(key = "verify-maintenance")
    public String verifyMaintenance(@ShellOption(help = HelpMessages.BUCKET_NAME) String bucketName,
                                    @ShellOption(help = HelpMessages.TARGET_SERVER) TargetServer targetServer,
                                    @ShellOption(defaultValue = "", help = HelpMessages.CHANGE_RECORD) String changeRecord) {

        return maintenanceService.displayStates(bucketName, changeRecord, targetServer);
    }
}
