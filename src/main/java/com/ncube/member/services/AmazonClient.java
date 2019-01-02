package com.ncube.member.services;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.ncube.member.MemberRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

@Service
public class AmazonClient implements ImagesStoreService<MultipartFile> {

    private AmazonS3Client s3client;

    @Value("${amazon.endpoint.region}")
    private String endpointRegion;
    @Value("${amazon.bucket.name}")
    private String bucketName;
    @Value("${amazon.access.key}")
    private String accessKey;
    @Value("${amazon.secret.key}")
    private String secretKey;

    @PostConstruct
    private void initializeAmazon() {
        AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
        this.s3client = (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withRegion(endpointRegion)
                .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }

    @Override
    public String uploadFile(MultipartFile multipartFile, String uploadFileName) {
        String fileUrl;
        try {
            File file = convertMultiPartToFile(multipartFile);
            fileUrl = uploadFileTos3bucket(uploadFileName, file);
            file.delete();
        } catch (IOException e) {
            throw new MemberRequestException(String.format("Error during saving image : %s", e.getMessage()));
        }
        return fileUrl;
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    private String uploadFileTos3bucket(String fileName, File file) {
        s3client.putObject(new PutObjectRequest(bucketName, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return s3client.getResourceUrl(bucketName, fileName);
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl != null) {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
        }
    }
}