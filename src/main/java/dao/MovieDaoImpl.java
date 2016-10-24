package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovieDaoImpl {

    private static final Logger logger = LoggerFactory.getLogger(MovieDaoImpl.class);

	public List<MovieSearch> getMovieList() {
		List<MovieSearch> movieList = new ArrayList<>();
		try {
	        String url = "jdbc:mysql://technicaltest.ctkeuweqhlpx.us-east-1.rds.amazonaws.com:3306/movies?autoReconnect=true&useSSL=false";
	        Connection conn = DriverManager.getConnection(url,"techtest","playp3n");
	        Statement stmt = conn.createStatement();
	        ResultSet rs;

	        rs = stmt.executeQuery("SELECT movie_title, search_term FROM movie_search");
	        while ( rs.next() ) {
	            String movieTitle = rs.getString("movie_title");
	            String searchTerm = rs.getString("search_term");

	            movieList.add(new MovieSearch(movieTitle, searchTerm));
	            
	        }
	        //TODO move to finally block
	        conn.close();
	    } catch (Exception e) {
	    	logger.error("Got an exception! ", e.getMessage());
	    }		
		logger.info("Movie list size- " + movieList.size());
		return movieList;
	}

}
