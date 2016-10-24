package init;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import helper.DynamoHelper;
import helper.DynamoSchema;
import helper.PropertiesProvider;
import vimeo.VimeoSearch;

public class Worker implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(Worker.class);

	private String movieTitle;
	private String searchTerm;
	private Path filePath;
	private PropertiesProvider propertiesProvider;

	static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

	public Worker(String movieTitle, String searchTerm, Path filePath, PropertiesProvider propertiesProvider) {
		this.movieTitle = movieTitle;
		this.searchTerm = searchTerm;
		this.filePath = filePath;
		this.propertiesProvider = propertiesProvider;
	}

	@Override
	public void run() {
		logger.info(
				"This is currently running on a separate thread, " + "the id is: " + Thread.currentThread().getId());
		VimeoSearch vimeoSearch = new VimeoSearch();
		int vimeoData = vimeoSearch.getResponse(searchTerm, "1", filePath)
				+ vimeoSearch.getResponse(searchTerm, "2", filePath)
				+ vimeoSearch.getResponse(searchTerm, "3", filePath);
		dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -1);
		cal.set(Calendar.MINUTE, 0);
		Date oneHourBack = cal.getTime();

		DynamoHelper dHelper = new DynamoHelper();
		Integer previousViews = dHelper.getItemsFromTable(movieTitle, dateFormatter.format(oneHourBack),
				propertiesProvider);

		DynamoSchema schema = new DynamoSchema();
		schema.setTitle(movieTitle);
		schema.setSearchTerm(searchTerm);

		Calendar calCurrent = Calendar.getInstance();
		calCurrent.set(Calendar.MINUTE, 0);
		schema.setTimestamp(dateFormatter.format(calCurrent.getTime()));
		schema.setHourTotal(vimeoData);
		Integer deltaLastHour = null;
		if (previousViews != null) {
			deltaLastHour = vimeoData - previousViews;
		}
		schema.setDeltaLastHour(deltaLastHour);
		schema.setSource("Vimeo");
		schema.setType("views");

		dHelper.loadDataIntoTable(schema, propertiesProvider);
	}

}
