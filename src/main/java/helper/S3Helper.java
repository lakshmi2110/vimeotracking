package helper;

import java.nio.file.Path;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

public class S3Helper {
 
    //TODO this can be stored in folders named by date/month/year for easier retrieval
    private String folderName = "vimeotracking";

    public PutObjectResult uploadObject(Path filePath, PropertiesProvider propertiesProvider) throws InterruptedException {
        //The AWS credentials MUST NOT be hardcoded here. They should ideally be part of an IAM role and retrieved at run time

    	AmazonS3 s3Client = new AmazonS3Client(
    			new BasicAWSCredentials(propertiesProvider.getAwsAccessKey(), propertiesProvider.getAwsSecretKey()));   

        PutObjectResult s3Put = s3Client.putObject(
                new PutObjectRequest(propertiesProvider.getS3Bucket(), folderName + "/" + filePath.toFile().getName(), filePath.toFile()));
        return s3Put;

    }

  }

