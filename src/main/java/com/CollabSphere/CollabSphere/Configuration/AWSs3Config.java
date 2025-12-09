package com.CollabSphere.CollabSphere.Configuration;

import lombok.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;   // (AWS SDK v2 – CORRECT)

import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.time.Duration;

/**
 * AWS S3 configuration (AWS SDK v2)
 */
@Builder
@Configuration
public class AwsS3Config {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.s3.endpoint:}")
    private String s3Endpoint;

    @Value("${aws.s3.path-style-access:false}")
    private boolean pathStyleAccess;

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        return DefaultCredentialsProvider.create();
    }

    @Bean
    public S3Client s3Client(AwsCredentialsProvider credentialsProvider) {
        Region region = Region.of(awsRegion);

        // Build client
        S3Client.Builder b = S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .httpClientBuilder(UrlConnectionHttpClient.builder());

        // Optional endpoint override (MinIO / localstack)
        if (s3Endpoint != null && !s3Endpoint.isBlank()) {
            b = b.endpointOverride(URI.create(s3Endpoint));

            // If you use a local S3-compatible server (MinIO/localstack) and your SDK supports it,
            // you can enable path-style access like this (uncomment when your SDK version supports):
            //
            // builder.serviceConfiguration(cfg -> cfg.pathStyleAccessEnabled(true));
            //
            // If your SDK version does NOT have serviceConfiguration(), leave it commented.
        }

        ClientOverrideConfiguration override = ClientOverrideConfiguration.builder()
                .apiCallAttemptTimeout(Duration.ofSeconds(60))
                .apiCallTimeout(Duration.ofSeconds(120))
                .build();

        builder.overrideConfiguration(override);
        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(AwsCredentialsProvider credentialsProvider) {
        S3Presigner.Builder presignerBuilder = S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(awsRegion));

        if (s3Endpoint != null && !s3Endpoint.isBlank()) {
            presignerBuilder.endpointOverride(URI.create(s3Endpoint));
        }

        return presignerBuilder.build();
    }
}