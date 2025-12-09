package com.CollabSphere.CollabSphere.Service;


import com.CollabSphere.CollabSphere.Entity.FileStorage;
import com.CollabSphere.CollabSphere.Entity.Team;
import com.CollabSphere.CollabSphere.Entity.User;
import com.CollabSphere.CollabSphere.Interface.AwsS3ServiceInterface;
import com.CollabSphere.CollabSphere.Repository.FileStorageRepository;
import com.CollabSphere.CollabSphere.Repository.TeamRepository;
import com.CollabSphere.CollabSphere.Repository.UserRepository;
import com.CollabSphere.CollabSphere.Service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AwsS3Service implements AwsS3ServiceInterface {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final FileStorageRepository fileStorageRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private String sanitizeFilename(String name) {
        return name == null ? "file" : name.replaceAll("[\\\\/]+", "_");
    }

    private String buildS3Key(Long teamId, String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String filename = sanitizeFilename(originalFilename);
        if (teamId != null) {
            return String.format("teams/%d/%s_%s", teamId, uuid, filename);
        } else {
            return String.format("uploads/%s_%s", uuid, filename);
        }
    }

    @Override
    @Transactional
    public FileStorage uploadFile(MultipartFile file, Long teamId, Long uploaderId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new IllegalArgumentException("Uploader not found"));

        Team team = null;
        if (teamId != null) {
            team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new IllegalArgumentException("Team not found"));
            // Optionally: validate membership/permission here or rely on filter
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String key = buildS3Key(teamId, originalFilename);

        // Upload object to S3
        try (InputStream is = file.getInputStream()) {
            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .acl(ObjectCannedACL.PRIVATE)
                    .build();

            RequestBody rb = RequestBody.fromInputStream(is, file.getSize());
            s3Client.putObject(putReq, rb);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        } catch (S3Exception s3e) {
            throw new RuntimeException("S3 upload failed: " + s3e.awsErrorDetails().errorMessage(), s3e);
        }

        // Save metadata
        FileStorage meta = new FileStorage();
        meta.setFileName(originalFilename);
        meta.setS3Key(key);
        meta.setContentType(file.getContentType());
        meta.setSize(file.getSize());
        meta.setUploader(uploader);
        meta.setTeam(team);
        meta.setDeleted(false);
        meta.setCreatedAt(Instant.now());
        meta.setUpdatedAt(Instant.now());

        return fileStorageRepository.save(meta);
    }

    @Override
    public URL generatePresignedDownloadUrl(Long fileId, Duration expiry) {
        FileStorage fs = fileStorageRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        if (fs.isDeleted()) throw new IllegalArgumentException("File not available");

        GetObjectRequest getReq = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fs.getS3Key())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiry != null ? expiry : Duration.ofMinutes(15))
                .getObjectRequest(getReq)
                .build();

        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
        return presigned.url();
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId, String requesterEmail) {
        FileStorage fs = fileStorageRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        if (fs.isDeleted()) return; // already deleted

        // permission: uploader OR team owner OR admin
        boolean allowed = false;
        Optional<User> optReq = userRepository.findByEmail(requesterEmail);
        if (optReq.isPresent()) {
            User req = optReq.get();
            if (fs.getUploader() != null && req.getId().equals(fs.getUploader().getId())) allowed = true;
            if (!allowed && fs.getTeam() != null && fs.getTeam().getOwner() != null
                    && req.getId().equals(fs.getTeam().getOwner().getId())) allowed = true;
            if (!allowed && req.getRoleType() != null && req.getRoleType().name().equalsIgnoreCase("ADMIN")) allowed = true;
        }

        if (!allowed) throw new SecurityException("Not authorized to delete file");

        // delete from S3 best-effort
        try {
            DeleteObjectRequest del = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fs.getS3Key())
                    .build();
            s3Client.deleteObject(del);
        } catch (S3Exception ignored) {
            // log if you have logger
        }

        fs.setDeleted(true);
        fs.setUpdatedAt(Instant.now());
        fileStorageRepository.save(fs);
    }

    @Override
    public FileStorage getFileMetadata(Long fileId) {
        return fileStorageRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
    }

    @Override
    public List<FileStorage> listFilesForTeam(Long teamId) {
        return fileStorageRepository.findByTeamIdAndDeletedFalseOrderByCreatedAtDesc(teamId);
    }

    @Override
    public List<FileStorage> listFilesUploadedByUser(Long uploaderId) {
        return fileStorageRepository.findByUploaderIdAndDeletedFalseOrderByCreatedAtDesc(uploaderId);
    }
}