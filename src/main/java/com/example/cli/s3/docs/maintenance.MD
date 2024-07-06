# Maintenance Commands and Examples

## Command: `deploy-maintenance`
**Description**: Deploys maintenance changes to the `maintenance.json` file stored in the specified S3 bucket. Optionally adds flags if they are missing from the file. Creates the `maintenance.json` file if it does not exist. Validates the change record if the bucket is the production bucket.

### Usage
```
deploy-maintenance --bucketName <bucket-name> --flags <flags> --state <state> --addIfMissing <addIfMissing> [--changeRecord <changeRecord>]
```

**Parameters**:
- `bucketName` (required): The name of the S3 bucket where `maintenance.json` is stored.
    - Help: The name of the S3 bucket where maintenance.json is stored.
- `flags` (required): Comma-separated list of flags to update.
    - Help: Comma-separated list of flags to update.
- `state` (required): The desired state (`ON` or `OFF`).
    - Help: The desired state (ON or OFF).
- `addIfMissing` (optional, default: `true`): Whether to add the flags if they are missing from the file.
    - Help: Whether to add the flags if they are missing from the file (default: true).
- `changeRecord` (optional unless `bucketName` is "prd"): The change record identifier to validate.
    - Help: The change record identifier to validate.

**Examples**:

**Example 1**: Deploy maintenance changes and add missing flags
```
deploy-maintenance --bucketName my-bucket --flags mss:adbo,napoli --state ON --addIfMissing true
```
**Output**:
```
INFO: Successfully updated the maintenance file in bucket: my-bucket
```

**Example 2**: Deploy maintenance changes without adding missing flags
```
deploy-maintenance --bucketName my-bucket --flags mss:adbo,xo --state OFF --addIfMissing false
```
**Output**:
```
ERROR: The following flags were not found in the maintenance file: xo
```

**Example 3**: Create the `maintenance.json` file if it does not exist
```
deploy-maintenance --bucketName my-bucket --flags mss:adbo --state ON --add

IfMissing true
```
**Output**:
```
INFO: File maintenance.json does not exist in bucket my-bucket. Creating a new file.
INFO: Successfully created the maintenance file in bucket: my-bucket
```

**Example 4**: Deploy maintenance changes to the production bucket (requires a change record)
```
deploy-maintenance --bucketName prd --flags mss:adbo --state ON --addIfMissing true --changeRecord CR12345
```
**Output**:
```
INFO: Successfully updated the maintenance file in bucket: prd
```

## Command: `verify-maintenance`
**Description**: Verifies the states of all flags in the `maintenance.json` file stored in the specified S3 bucket and displays the results in an ASCII table format. Creates the `maintenance.json` file if it does not exist. Validates the change record if the bucket is the production bucket.

**Usage**:
```
verify-maintenance --bucketName <bucket-name> [--changeRecord <changeRecord>]
```

**Parameters**:
- `bucketName` (required): The name of the S3 bucket where `maintenance.json` is stored.
    - Help: The name of the S3 bucket where maintenance.json is stored.
- `changeRecord` (optional unless `bucketName` is "prd"): The change record identifier to validate.
    - Help: The change record identifier to validate.

**Example**:
```
verify-maintenance --bucketName my-bucket
```
**Output**:
```
Flag   | State
----------------
mss:adbo | on   
napoli   | on   
xo       | on   
```

**Example 2**: Verify maintenance states in the production bucket (requires a change record)
```
verify-maintenance --bucketName prd --changeRecord CR12345
```
**Output**:
```
Flag   | State
----------------
mss:adbo | on   
napoli   | on   
xo       | on   
```

# Important Notes

- **Valid Change Record**: A valid change record is required to perform any updates on the `maintenance.json` file if the bucket name is "prd" (production bucket). This ensures that all changes are properly authorized and tracked.
- **State Validation**: The `state` parameter only accepts `ON` or `OFF`. Any other values will result in an error.
- **Default Behavior**: By default, the `addIfMissing` parameter is set to `true`, meaning any missing flags will be added to the `maintenance.json` file unless specified otherwise.