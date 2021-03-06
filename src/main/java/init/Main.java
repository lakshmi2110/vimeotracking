package init;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dao.MovieDaoImpl;
import dao.MovieSearch;
import helper.PropertiesProvider;
import helper.S3Helper;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Worker.class);
	private static PropertiesProvider propertiesProvider = loadProperties();

	public static void main(String[] args) throws InterruptedException {

		MovieDaoImpl dao = new MovieDaoImpl();
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		// The filename is mm-hh-dd-MM-yyyy and not the other way for better
		// performance during retrieval.
		// Prefixes are not repeated this way
		Path filePath = Paths.get(new SimpleDateFormat("mm-hh-dd-MM-yyyy'.txt'").format(new Date()));
		if (!Files.exists(filePath)) {
			try {
				Files.createFile(filePath);
			} catch (IOException e) {
				logger.error("Error when creating file ", e.getMessage());
			}
		}

		// Query the MySQL database for the list of movies
		List<MovieSearch> movieList = dao.getMovieList();
		Date start = new Date();

		for (int i = 0; i < movieList.size(); i++) {
			String movieTitle = movieList.get(i).getMovieTitle();
			String searchTerm = movieList.get(i).getMovieSearchTerm();
			searchTerm = searchTerm.replaceAll(" ", "%20");
			Worker work = new Worker(movieTitle, searchTerm, filePath, propertiesProvider);
			executorService.execute(work);
		}
		executorService.shutdown();
		try {
			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException ie) {
			logger.debug("Exception received " + ie.getMessage());
		}

		// Upload file to S3
		logger.info("Starting upload of file {} to S3", filePath.getFileName());
		S3Helper s3Helper = new S3Helper();
		s3Helper.uploadObject(filePath, propertiesProvider);
		logger.info("Finished upload of file {} to S3", filePath.getFileName());

		Date end = new Date();
		long difference = end.getTime() - start.getTime();
		logger.info("This whole process took: " + difference / 1000 + " seconds.");
	}

	// load a properties file from class path, inside static method
	private static PropertiesProvider loadProperties() {
		PropertiesProvider propertiesProvider = new PropertiesProvider();

		Properties prop = new Properties();
		InputStream input = null;
		try {
			String filename = "vimeo.properties";
			input = Worker.class.getClassLoader().getResourceAsStream(filename);
			if (input == null) {
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
		} finally {
			if (input != null) {
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
