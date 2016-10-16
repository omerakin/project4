package cs601.hotelapp;

public class TouristAttraction {
	private String attractionId;
	private String name;
	private String address;
	private double rating;
	
	public TouristAttraction(String attractionId, String name, String address, double rating) {
		this.attractionId = attractionId;
		this.name = name;
		this.address = address;
		this.rating = rating;
	}
	
	@Override
	public String toString() {
		return "TouristAttraction [attractionId=" + attractionId + ", name=" + name + ", address=" + address
				+ ", rating=" + rating + "]";
	}

	public String getAttractionId() {
		return attractionId;
	}

	public void setAttractionId(String attractionId) {
		this.attractionId = attractionId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}
}
