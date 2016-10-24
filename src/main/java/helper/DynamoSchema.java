package helper;

public class DynamoSchema {

	private String title;

	private String searchTerm;

	private String timestamp;

	private String source;

	private String type;

	private Integer hourTotal;

	private Integer deltaLastHour;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getHourTotal() {
		return hourTotal;
	}

	public void setHourTotal(Integer hourTotal) {
		this.hourTotal = hourTotal;
	}

	public Integer getDeltaLastHour() {
		return deltaLastHour;
	}

	public void setDeltaLastHour(Integer deltaLastHour) {
		this.deltaLastHour = deltaLastHour;
	}


}
