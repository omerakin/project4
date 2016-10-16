package cs601.hotelapp;

public class Address {
	
	private String street_address;
	private String city;
	private String state;
	private double longitude;
	private double latitude;
	
	public Address (String street_address, String city, String state, 
			double longitude, double latitude) {
		
		this.street_address = street_address;
		this.city = city;
		this.state = state;
		this.longitude = longitude;
		this.latitude = latitude;
		
	}

	public String getStreet_address() {
		return street_address;
	}

	public void setStreet_address(String street_address) {
		this.street_address = street_address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

}
