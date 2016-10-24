package helper;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;

public class DynamoHelper {

	private static final Logger logger = LoggerFactory.getLogger(DynamoHelper.class);

	public void loadDataIntoTable(DynamoSchema schema, PropertiesProvider propertiesProvider) {

		DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(
				new BasicAWSCredentials(propertiesProvider.getAwsAccessKey(), propertiesProvider.getAwsSecretKey())));

		String dynamoDBTable = propertiesProvider.getDynamodbTable();

		Table table = dynamoDB.getTable(dynamoDBTable);

		try {
			logger.info("Adding data to " + dynamoDBTable);

			Item item = new Item()
					.withPrimaryKey("title", schema.getTitle())
					.withString("timestamp",schema.getTimestamp())
					.withString("searchTerm", schema.getSearchTerm())
					.withString("source", schema.getSource())
					.withString("type", schema.getType())
					.withNumber("hourTotal", schema.getHourTotal());
			if(schema.getDeltaLastHour() != null) {
				table.putItem(item.withNumber("deltaLastHour", schema.getDeltaLastHour()));
			} else {
				table.putItem(item);
			}

			logger.debug("Finished adding data to " + dynamoDBTable);


		} catch (Exception e) {
			logger.error("Failed to create item in {} ",dynamoDBTable , e.getMessage());
			e.printStackTrace();
		}

	}


	public Integer getItemsFromTable(String title, String timestamp, PropertiesProvider propertiesProvider) {
		DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(
				new BasicAWSCredentials(propertiesProvider.getAwsAccessKey(), propertiesProvider.getAwsSecretKey())));

		String dynamoDBTable = propertiesProvider.getDynamodbTable();

		Table table = dynamoDB.getTable(dynamoDBTable);

		GetItemSpec spec = new GetItemSpec().withPrimaryKey("title", title, "timestamp", timestamp);
		Item item = table.getItem(spec);

		if(item != null) {
			String jsonItem = item.toJSON();
			Integer previousViews = (Integer) new JSONObject(jsonItem).get("hourTotal");
			return previousViews;
		}

		return null;

	}

}
