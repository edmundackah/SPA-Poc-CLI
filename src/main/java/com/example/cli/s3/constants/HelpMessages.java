package com.example.cli.s3.constants;

public interface HelpMessages {

    String BUCKET_NAME = "Target env (e.g. dev, staging, prod)";
    String FOLDER_PATH = "Directory containing your static assets (e.g. dist)";
    String PREFIX = "The route to your SPA (homepage path)";
    String ARTIFACT_URL = "The url to the artifact you want to deploy";

    String FLAGS = "Comma-separated list of flags to update.";
    String FLAG_STATE = "The desired state (ON or OFF).";
    String ADD_IF_MISSING = "Whether to add the flags if they are missing from the file (default: true).";

    String CHANGE_RECORD = "The change record identifier to validate.";
    String TARGET_SERVER = "The target S3 instance (e.g. ECS_S3, ECS_S3_PROD, AWS_S3 & AWS_S3_PROD)";

    String PROD_BUCKET_INVALID_CHANGE_RECORD = "Valid change record required to access PROD buckets";
}
