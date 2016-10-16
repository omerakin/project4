package cs601.hotelapp;

public class Hotel implements Comparable<Hotel> {
	
	private String hotel_id;
	private String hotel_name;
	private Address address;
	
	
	public Hotel(String hotel_id, String hotel_name, Address address){
		this.hotel_id = hotel_id;
		this.hotel_name = hotel_name;
		this.address = address;
	}
	
	@Override
	public int compareTo(Hotel o) {
		// TODO Auto-generated method stub
		return hotel_name.compareTo(o.getHotel_name());
	}

	public String getHotel_id() {
		return hotel_id;
	}

	public void setHotel_id(String hotel_id) {
		this.hotel_id = hotel_id;
	}

	public String getHotel_name() {
		return hotel_name;
	}

	public void setHotel_name(String hotel_name) {
		this.hotel_name = hotel_name;
	}

	public Address getAddress() {
		return address;
	}

	public void setAdress(Address address) {
		this.address = address;
	}	

}
