package com.example.cli.s3.command;

import com.example.cli.s3.Service.MaintenanceService;
import com.example.cli.s3.constants.HelpMessages;
import com.example.cli.s3.context.TenantContext;
import com.example.cli.s3.enums.EnvironmentEnums;
import com.example.cli.s3.enums.FlagState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class MaintenanceCommand {

    @Autowired
    private final MaintenanceService maintenanceService;

    @Autowired
    private final TenantContext tenantContext;


    @ShellMethod("deploy-maintenance")
    public void deployMaintenance(
            @ShellOption(help = HelpMessages.ENV) String env,
            @ShellOption(help = HelpMessages.FLAGS) String flags,
            @ShellOption(help = HelpMessages.FLAG_STATE) FlagState state,
            @ShellOption(defaultValue = "true", help = HelpMessages.ADD_IF_MISSING) Boolean addIfMissing,
            @ShellOption(defaultValue = "", help = HelpMessages.CHANGE_RECORD) String changeRecord) {

        String bucketName = tenantContext.getBucketName(EnvironmentEnums.fromString(env));

        maintenanceService.updateStates(bucketName, flags, state, addIfMissing, changeRecord);
    }

    @ShellMethod("verify-maintenance")
    public String verifyMaintenance(
            @ShellOption(help = HelpMessages.ENV) String env,
            @ShellOption(defaultValue = "", help = HelpMessages.CHANGE_RECORD) String changeRecord) {

        String bucketName = tenantContext.getBucketName(EnvironmentEnums.fromString(env));

        return maintenanceService.displayStates(bucketName, changeRecord);
    }
}
