package com.example.cli.s3.constants;

public interface Constants {

    String BUCKET_NAME = "Target env (e.g. dev, staging, prod)";
    String FOLDER_PATH = "Directory containing your static assets (e.g. dist)";
    String PREFIX = "The route to your SPA (e.g. 'servicing/customer')";
    String ARTIFACT_URL = "The url to the artifact you want to deploy";

    String FLAGS = "Comma-separated list of flags to update.";
    String FLAG_STATE = "The desired state (ON or OFF).";
    String ADD_IF_MISSING = "Whether to add the flags if they are missing from the file (default: true).";

    String CHANGE_RECORD = "The change record identifier to validate.";
    String TARGET_SERVER = "The target S3 instance (e.g. ECS_S3, ECS_S3_PROD, AWS_S3 & AWS_S3_PROD)";

    String MAINTENANCE_FILE = "maintenance.json";

    String NOT_BLANK = "must not be blank";
    String NO_TRAILING_SLASH = "Prefix must not end with a slash";
    String INVALID_URL = "Invalid URL format";
    String NOT_NULL = "must be specified";

    String NO_TRAILING_SLASH_REGEX = ".*[^/]$";
    String VALID_URL_REGEX = "^(https|http)://[^\\s/$.?#].[^\\s]*$";
}
