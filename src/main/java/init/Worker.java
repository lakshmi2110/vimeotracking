package init;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dao.MovieDaoImpl;
import dao.MovieSearch;
import helper.DynamoHelper;
import helper.DynamoSchema;
import helper.PropertiesProvider;
import helper.S3Helper;
import vimeo.VimeoSearch;

public class Worker implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Worker.class);

	public boolean running = false;
	private String movieTitle;
	private String searchTerm;
	private Path filePath;

	private static PropertiesProvider propertiesProvider = loadProperties();
	static SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm");


	public Worker(String movieTitle, String searchTerm, Path filePath) {
		this.movieTitle = movieTitle;
		this.searchTerm = searchTerm;
		this.filePath = filePath;
		Thread thread = new Thread(this);  
		thread.start();  
	}

	public static void main(String[] args) throws InterruptedException {

		MovieDaoImpl dao = new MovieDaoImpl();

		List<Worker> workers = new ArrayList<Worker>();

		Path filePath = Paths.get(new SimpleDateFormat("yyyyMMddhhmm'.txt'").format(new Date()));
		if (!Files.exists(filePath)) {
			try {
				Files.createFile(filePath);
			} catch (IOException e) {
				logger.error("Error when creating file " , e.getMessage());
			}
		}

		//		Query the MySQL database for the list of movies (endpoint details below)
		List<MovieSearch> movieList = dao.getMovieList();
		Date start = new Date();  

		for (int i = 0; i < movieList.size(); i++) {
			String movieTitle = movieList.get(i).getMovieTitle();
			String searchTerm = movieList.get(i).getMovieSearchTerm();
			searchTerm = searchTerm.replaceAll(" ", "%20");
			workers.add(new Worker(movieTitle, searchTerm, filePath));
		}

		logger.info("Workers size - "+workers.size());
		for (Worker worker : workers) {
			while (worker.running) {
				Thread.sleep(1);
			}
		}

		//Upload file to S3
		logger.info("Starting upload of file {} to S3", filePath.getFileName());
		S3Helper s3Helper = new S3Helper();
		s3Helper.uploadObject(filePath, propertiesProvider);
		logger.info("Finished upload of file {} to S3", filePath.getFileName());

		Date end = new Date();  
		long difference = end.getTime() - start.getTime();  
		logger.info("This whole process took: " + difference/1000 + " seconds."); 
	}



	@Override
	public void run() {
		this.running = true;
		logger.info(
				"This is currently running on a separate thread, " + "the id is: " + Thread.currentThread().getId());
		VimeoSearch vimeoSearch = new VimeoSearch();
		int vimeoData = vimeoSearch.getResponse(searchTerm, "1", filePath) 
				+ vimeoSearch.getResponse(searchTerm, "2", filePath) 
				+ vimeoSearch.getResponse(searchTerm, "3" , filePath);
		dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -1);
		cal.set(Calendar.MINUTE, 0);
		Date oneHourBack = cal.getTime();

		DynamoHelper dHelper = new DynamoHelper();
		Integer previousViews = dHelper.getItemsFromTable(movieTitle, dateFormatter.format(oneHourBack), propertiesProvider);

		DynamoSchema schema = new DynamoSchema();
		schema.setTitle(movieTitle);
		schema.setSearchTerm(searchTerm);

		Calendar calCurrent = Calendar.getInstance();
		calCurrent.set(Calendar.MINUTE, 0);
		schema.setTimestamp(dateFormatter.format(calCurrent.getTime()));
		schema.setHourTotal(vimeoData);
		Integer deltaLastHour = null;
		if(previousViews != null) {
			deltaLastHour = vimeoData - previousViews;
		}
		schema.setDeltaLastHour(deltaLastHour);
		schema.setSource("Vimeo");
		schema.setType("views");

		dHelper.loadDataIntoTable(schema, propertiesProvider);

		try {
			Thread.sleep(5);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();  

		}  
		this.running = false;
	}


	//load a properties file from class path, inside static method
	private static PropertiesProvider loadProperties() {
		PropertiesProvider propertiesProvider = new PropertiesProvider();

		Properties prop = new Properties();
		InputStream input = null;
		try {
			String filename = "vimeo.properties";
			input = Worker.class.getClassLoader().getResourceAsStream(filename);
			if(input==null){
				logger.error("Sorry, unable to find " + filename);
				return propertiesProvider;
			}

			prop.load(input);
			propertiesProvider.setAwsAccessKey(prop.getProperty("aws.accesskey"));
			propertiesProvider.setAwsSecretKey(prop.getProperty("aws.secretkey"));
			propertiesProvider.setDynamodbTable(prop.getProperty("dynamodb.table"));
			propertiesProvider.setS3Bucket(prop.getProperty("s3.bucket"));

		} catch (IOException ex) {
			logger.error("Exception thrown when trying to load properties", ex.getMessage());
		} finally{
			if(input!=null){
				try {
					input.close();
				} catch (IOException e) {
					logger.error("Exception thrown when trying to close input stream ", e.getMessage());
				}
			}
		}
		return propertiesProvider;

	}
}
