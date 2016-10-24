package vimeo;

import java.nio.file.Path;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import helper.Singleton;

public class VimeoSearch {

    private static final Logger logger = LoggerFactory.getLogger(VimeoSearch.class);

	public int getResponse(String searchParam, String page, Path filePath) {
		Vimeo vimeo = new Vimeo("829e9488d8d8af48c4a57068b4fd405d"); 
		int totalViews = 0;

		VimeoResponse info;
		try {
			info = vimeo.searchVideos(searchParam, page);
			JSONObject data = info.getJson();
			logger.debug("json for search param - "+ searchParam + " is " + data);

			// add to file 
			Singleton.getInstance().writeToFile(filePath, data.toString());

			data.get("data");
			JSONArray dataArray = data.getJSONArray("data");

			for(int i = 0 ; i < dataArray.length() ; i++) {
				Object stats = dataArray.getJSONObject(i).getJSONObject("stats").get("plays");
				if(stats instanceof Integer) {
					totalViews +=(Integer) stats;
				}

			}
		} catch (Exception e) {
			logger.error("Exception when retreiving data from Vimeo " + e.getMessage());
		}
		logger.debug("Total views for searchParam {}, pageNumber {} is - {} ", searchParam, page, totalViews);

		return totalViews;
	}

}
