package helper;

public class PropertiesProvider {

	private String awsAccessKey;
	
	private String awsSecretKey;
	
	private String s3Bucket;
	
	private String dynamodbTable;

	public String getAwsAccessKey() {
		return awsAccessKey;
	}

	public void setAwsAccessKey(String awsAccessKey) {
		this.awsAccessKey = awsAccessKey;
	}

	public String getAwsSecretKey() {
		return awsSecretKey;
	}

	public void setAwsSecretKey(String awsSecretKey) {
		this.awsSecretKey = awsSecretKey;
	}

	public String getS3Bucket() {
		return s3Bucket;
	}

	public void setS3Bucket(String s3Bucket) {
		this.s3Bucket = s3Bucket;
	}

	public String getDynamodbTable() {
		return dynamodbTable;
	}

	public void setDynamodbTable(String dynamodbTable) {
		this.dynamodbTable = dynamodbTable;
	}
	
	
}
