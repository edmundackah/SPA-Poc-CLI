package com.example.cli.s3.utils;

import com.example.cli.s3.enums.TargetServer;
import com.example.cli.s3.factory.S3ClientFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Slf4j
@Component
public class S3Util {

    @Value("${default.object.acl}")
    private String objectACL;

    @Autowired
    private S3ClientFactory s3ClientFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode getMaintenanceFile(String bucketName, TargetServer server, String changeRecord) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key("maintenance.json")
                    .build();

            byte[] content = s3ClientFactory.getS3Client(server, changeRecord).getObjectAsBytes(getObjectRequest).asByteArray();
            return objectMapper.readTree(new ByteArrayInputStream(content));
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                log.info("File maintenance.json does not exist in bucket {}. Creating a new file.", bucketName);
                return objectMapper.createObjectNode();
            } else {
                log.error("Failed to get the file from S3: {}", e.getMessage());
                throw e;
            }
        } catch (IOException e) {
            log.error("Failed to process the JSON content: {}", e.getMessage());
            throw new RuntimeException("Failed to process the JSON content", e);
        }
    }

    public void saveMaintenanceFile(String bucketName, JsonNode rootNode,
                                    TargetServer server, String changeRecord) {
        byte[] updatedContent = rootNode.toString().getBytes(StandardCharsets.UTF_8);

        RequestBody payload = RequestBody.fromBytes(updatedContent);
        log.info("Setting maintenance.json contentType to {} and ACL to {}", payload.contentType(), objectACL);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .contentType(payload.contentType())
                .key("maintenance.json")
                .acl(objectACL)
                .build();

        s3ClientFactory.getS3Client(server, changeRecord)
                .putObject(putObjectRequest, payload);
        log.info("Successfully updated the maintenance file in bucket: {}", bucketName);
    }

    public void uploadDirectoryToS3(File directory, String bucketName, String prefix,
                                    TargetServer server, String changeRecord) throws IOException {

        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            S3Client s3Client = s3ClientFactory.getS3Client(server, changeRecord);
            paths.filter(Files::isRegularFile)
                    .forEach(filePath -> uploadFileToS3(s3Client, bucketName, prefix, directory.toPath(), filePath));
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    private void uploadFileToS3(S3Client s3Client, String bucketName, String prefix, Path rootDir, Path filePath) {
        String key = prefix + "/" + rootDir.relativize(filePath).toString().replace("\\", "/");

        String contentType = Util.getContentType(filePath);
        log.info("Setting {} ContentType to {} and ACL to {}", filePath.getFileName(), contentType, objectACL);

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .contentType(contentType)
                .acl(objectACL)
                .key(key)
                .build();

        s3Client.putObject(putRequest, filePath);

        log.info("Uploaded: {} to s3://{}/{}", filePath, bucketName, key);
    }

}