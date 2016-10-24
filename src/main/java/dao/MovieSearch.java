package dao;

public class MovieSearch {

	String movieTitle;

	String movieSearchTerm;

	public MovieSearch(String movieTitle, String searchTerm) {
		this.movieTitle = movieTitle;
		this.movieSearchTerm = searchTerm;
	}

	public String getMovieTitle() {
		return movieTitle;
	}

	public void setMovieTitle(String movieTitle) {
		this.movieTitle = movieTitle;
	}

	public String getMovieSearchTerm() {
		return movieSearchTerm;
	}

	public void setMovieSearchTerm(String movieSearchTerm) {
		this.movieSearchTerm = movieSearchTerm;
	}


}
