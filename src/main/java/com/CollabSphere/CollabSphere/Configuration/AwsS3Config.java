package com.CollabSphere.CollabSphere.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.time.Duration;

@Configuration
public class AwsS3Config {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.s3.endpoint:}")
    private String s3Endpoint;

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        return DefaultCredentialsProvider.create();
    }

    @Bean
    public S3Client s3Client(AwsCredentialsProvider credentialsProvider) {

        ClientOverrideConfiguration override = ClientOverrideConfiguration.builder()
                .apiCallAttemptTimeout(Duration.ofSeconds(60))
                .apiCallTimeout(Duration.ofSeconds(120))
                .build();

        S3ClientBuilder builder = S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(awsRegion))
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .overrideConfiguration(override);

        // Optional for MinIO / Localstack
        if (s3Endpoint != null && !s3Endpoint.isBlank()) {
            builder = builder.endpointOverride(URI.create(s3Endpoint));
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(AwsCredentialsProvider credentialsProvider) {

        S3Presigner.Builder presignerBuilder = S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(awsRegion));

        if (s3Endpoint != null && !s3Endpoint.isBlank()) {
            presignerBuilder = presignerBuilder.endpointOverride(URI.create(s3Endpoint));
        }

        return presignerBuilder.build();
    }
}