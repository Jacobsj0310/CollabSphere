package com.CollabSphere.CollabSphere.Service;

import com.CollabSphere.CollabSphere.DTO.FileStorageDTO;
import com.CollabSphere.CollabSphere.Entity.FileStorage;
import com.CollabSphere.CollabSphere.Entity.Team;
import com.CollabSphere.CollabSphere.Entity.User;
import com.CollabSphere.CollabSphere.Interface.FileStorageInterface;
import com.CollabSphere.CollabSphere.Repository.FileStorageRepository;
import com.CollabSphere.CollabSphere.Repository.TeamMemberRepository;
import com.CollabSphere.CollabSphere.Repository.TeamRepository;
import com.CollabSphere.CollabSphere.Repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FileStorageService implements FileStorageInterface {

    private final FileStorageRepository fileStorageRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository; // for membership check

    // configure these appropriately (or load from application.properties via @Value)
    private final String bucketName = "your-collabsphere-bucket"; // CHANGE
    private final Region awsRegion = Region.AP_SOUTH_1; // CHANGE

    // lazily create clients (or @Bean them in a @Configuration)
    private S3Client s3Client;
    private S3Presigner s3Presigner;

    public FileStorageService(FileStorageRepository fileStorageRepository, UserRepository userRepository, TeamRepository teamRepository, TeamMemberRepository teamMemberRepository) {
        this.fileStorageRepository = fileStorageRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    private synchronized S3Client s3() {
        if (s3Client == null) {
            s3Client = S3Client.builder()
                    .region(awsRegion)
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }
        return s3Client;
    }

    private synchronized S3Presigner presigner() {
        if (s3Presigner == null) {
            s3Presigner = S3Presigner.builder()
                    .region(awsRegion)
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        }
        return s3Presigner;
    }

    // Upload
    @Override
    @Transactional
    public FileStorageDTO.FileUploadResponseDTO uploadFile(Long teamId, MultipartFile file, String requesterEmail) throws Exception {

        User uploader = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalArgumentException("Uploader not found"));

        Team team = null;
        if (teamId != null) {
            team = teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("Team not found"));
            // permission check: owner/member/admin
            boolean isOwner = team.getOwner() != null && team.getOwner().getId().equals(uploader.getId());
            boolean isMember = teamMemberRepository.existsByTeamIdAndUserId(teamId, uploader.getId());
            boolean isAdmin = uploader.getRoleType() != null && uploader.getRoleType().name().equals("ADMIN");
            if (!(isOwner || isMember || isAdmin)) {
                throw new SecurityException("Not authorized to upload to this team");
            }
        }

        // Create unique S3 key
        String ext = "";
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        int idx = original.lastIndexOf('.');
        if (idx >= 0) ext = original.substring(idx);
        String uniqueKey = "files/" + UUID.randomUUID().toString() + "_" + URLEncoder.encode(original, StandardCharsets.UTF_8) + ext;

        // Put object to S3
        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueKey)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .acl(ObjectCannedACL.PRIVATE)
                .build();

        //s3().putObject(putReq, RequestBody.fromInputStream().fromInputStream(file.getInputStream(), file.getSize()));\
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(file.getName())
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        // Persist metadata
        FileStorage meta = FileStorage.builder()
                .fileName(original)
                .s3Key(uniqueKey)
                .contentType(file.getContentType())
                .size(file.getSize())
                .uploader(uploader)
                .team(team)
                .deleted(false)
                .build();

        FileStorage saved = fileStorageRepository.save(meta);

        FileStorageDTO.FileUploadResponseDTO resp = new FileStorageDTO.FileUploadResponseDTO();
        resp.setId(saved.getId());
        resp.setFileName(saved.getFileName());
        resp.setContentType(saved.getContentType());
        resp.setSize(saved.getSize());
        resp.setTeamId(saved.getTeam() != null ? saved.getTeam().getId() : null);
        resp.setUploaderEmail(uploader.getEmail());
        resp.setCreatedAt(saved.getCreatedAt());

        return resp;
    }

    // Presigned download URL
    @Override
    public String getPresignedDownloadUrl(Long fileId, String requesterEmail, int validitySeconds) throws Exception {
        FileStorage file = fileStorageRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        if (file.isDeleted()) throw new IllegalArgumentException("File not found");

        // check permissions: uploader or team member or admin
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalArgumentException("Requester not found"));

        boolean allowed = false;
        if (file.getUploader() != null && file.getUploader().getId().equals(requester.getId())) allowed = true;
        if (file.getTeam() != null) {
            boolean isOwner = file.getTeam().getOwner() != null && file.getTeam().getOwner().getId().equals(requester.getId());
            boolean isMember = teamMemberRepository.existsByTeamIdAndUserId(file.getTeam().getId(), requester.getId());
            if (isOwner || isMember) allowed = true;
        }
        if (!allowed) {
            boolean isAdmin = requester.getRoleType() != null && requester.getRoleType().name().equals("ADMIN");
            if (isAdmin) allowed = true;
        }
        if (!allowed) throw new SecurityException("Not authorized to download this file");

        // Build presigned GET request
        software.amazon.awssdk.services.s3.model.GetObjectRequest getReq = software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                .bucket(bucketName)
                .key(file.getS3Key())
                .build();

        GetObjectRequest presignGetReq = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(file.getS3Key())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(presignGetReq)
                .signatureDuration(Duration.ofSeconds(Math.max(60, validitySeconds)))
                .build();

        String url = presigner().presignGetObject(presignRequest).url().toString();
        return url;
    }

    // -------------------------
    // Server-side stream download (optional)
    // -------------------------
    @Override
    public InputStream downloadFileStream(Long fileId, String requesterEmail) throws Exception {
        FileStorage file = fileStorageRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        if (file.isDeleted()) throw new IllegalArgumentException("File not found");

        // permission checks same as presigned method
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalArgumentException("Requester not found"));

        boolean allowed = false;
        if (file.getUploader() != null && file.getUploader().getId().equals(requester.getId())) allowed = true;
        if (file.getTeam() != null) {
            boolean isOwner = file.getTeam().getOwner() != null && file.getTeam().getOwner().getId().equals(requester.getId());
            boolean isMember = teamMemberRepository.existsByTeamIdAndUserId(file.getTeam().getId(), requester.getId());
            if (isOwner || isMember) allowed = true;
        }
        if (!allowed) {
            boolean isAdmin = requester.getRoleType() != null && requester.getRoleType().name().equals("ADMIN");
            if (isAdmin) allowed = true;
        }
        if (!allowed) throw new SecurityException("Not authorized to download this file");

        // get object from S3 and return InputStream (caller must close)
        GetObjectRequest getReq = GetObjectRequest.builder().bucket(bucketName).key(file.getS3Key()).build();
        return s3().getObject(getReq);
    }

    // -------------------------
    // Delete
    // -------------------------
    @Override
    @Transactional
    public void deleteFile(Long fileId, String requesterEmail) throws Exception {
        FileStorage file = fileStorageRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalArgumentException("Requester not found"));

        boolean isUploader = file.getUploader() != null && file.getUploader().getId().equals(requester.getId());
        boolean allowed = isUploader;

        if (file.getTeam() != null) {
            boolean isOwner = file.getTeam().getOwner() != null && file.getTeam().getOwner().getId().equals(requester.getId());
            boolean isMember = teamMemberRepository.existsByTeamIdAndUserId(file.getTeam().getId(), requester.getId());
            if (isOwner || isMember) allowed = true;
        }

        if (!allowed) {
            boolean isAdmin = requester.getRoleType() != null && requester.getRoleType().name().equals("ADMIN");
            if (isAdmin) allowed = true;
        }

        if (!allowed) throw new SecurityException("Not authorized to delete this file");

        // delete object from S3 (best-effort) and soft-delete metadata
        try {
            DeleteObjectRequest del = DeleteObjectRequest.builder().bucket(bucketName).key(file.getS3Key()).build();
            s3().deleteObject(del);
        } catch (Exception ex) {
            // log and continue to soft-delete metadata
        }

        file.setDeleted(true);
        fileStorageRepository.save(file);
    }

    // -------------------------
    // List files for team
    // -------------------------
    @Override
    @Transactional(readOnly = true)
    public List<FileStorageDTO.FileMetadataOnlyDTO> listFilesForTeam(Long teamId) {
        List<FileStorage> list = fileStorageRepository.findByTeamIdAndDeletedFalseOrderByCreatedAtDesc(teamId);
        return list.stream().map(f -> {
            FileStorageDTO.FileMetadataOnlyDTO d = new FileStorageDTO.FileMetadataOnlyDTO();
            d.setId(f.getId());
            d.setFileName(f.getFileName());
            d.setContentType(f.getContentType());
            d.setSize(f.getSize());
            d.setTeamId(f.getTeam() != null ? f.getTeam().getId() : null);
            d.setUploaderEmail(f.getUploader() != null ? f.getUploader().getEmail() : null);
            d.setCreatedAt(f.getCreatedAt());
            return d;
        }).collect(Collectors.toList());
    }
}
