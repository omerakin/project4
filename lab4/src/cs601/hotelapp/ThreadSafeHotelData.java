package cs601.hotelapp;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import cs601.concurrent.ReentrantReadWriteLock;

/**
 * Class HotelData - a data structure that stores information about hotels and
 * hotel reviews. Allows to quickly lookup a hotel given the hotel id. 
 * Allows to easily find hotel reviews for a given hotel, given the hotelID. 
 * Reviews for a given hotel id are sorted by the date and user nickname.
 *
 */
public class ThreadSafeHotelData {

	// FILL IN CODE - declare data structures to store hotel data
	private final Map<String, Hotel> hotelsGivenByHotelId;
	private final Map<String, TreeSet<Review>> reviewsGivenByHotelId;
	private final Map<String, TreeMap<String, TouristAttraction>> attractionsGivenByHotelId;
	private Hotel hotel;
	private Address address;
	private Review reviews;
	private TouristAttraction touristAttraction;
	private Boolean isSuccessful;
	
	//Created ReentrantReadWriteLock lock object
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	

	/**
	 * Default constructor.
	 */
	public ThreadSafeHotelData() {
		// FILL IN CODE
		// Initialise all data structures
		hotelsGivenByHotelId = new HashMap<String,Hotel>();
		reviewsGivenByHotelId = new HashMap<String,TreeSet<Review>>();
		attractionsGivenByHotelId = new HashMap<String, TreeMap<String, TouristAttraction>>();

	}

	/**
	 * Create a Hotel given the parameters, and add it to the appropriate data
	 * structure(s).
	 * 
	 * @param hotelId
	 *            - the id of the hotel
	 * @param hotelName
	 *            - the name of the hotel
	 * @param city
	 *            - the city where the hotel is located
	 * @param state
	 *            - the state where the hotel is located.
	 * @param streetAddress
	 *            - the building number and the street
	 * @param latitude
	 * @param longitude
	 */
	public void addHotel(String hotelId, String hotelName, String city, String state, String streetAddress, double lat,
			double lon) {
		// FILL IN CODE
		//With the lockWrite it is added to our HashMap safely
		lock.lockWrite();
		try {
			//Set the values to the address and hotel object.
			address = new Address(streetAddress, city, state, lat, lon);
			hotel = new Hotel(hotelId, hotelName, address);		
			//Add to the hotelsGivenByHotelId TreeMap.
			hotelsGivenByHotelId.put(hotelId, hotel);
		} finally {
			lock.unlockWrite();
		}
		
	}

	/**
	 * Add a new review.
	 * 
	 * @param hotelId
	 *            - the id of the hotel reviewed
	 * @param reviewId
	 *            - the id of the review
	 * @param rating
	 *            - integer rating 1-5.
	 * @param reviewTitle
	 *            - the title of the review
	 * @param review
	 *            - text of the review
	 * @param isRecommended
	 *            - whether the user recommends it or not
	 * @param date
	 *            - date of the review in the format yyyy-MM-dd, e.g.
	 *            2016-08-29.
	 * @param username
	 *            - the nickname of the user writing the review.
	 * @return true if successful, false if unsuccessful because of invalid date
	 *         or rating. Needs to catch and handle ParseException if the date is invalid.
	 *         Needs to check whether the rating is in the correct range
	 */
	public boolean addReview(String hotelId, String reviewId, int rating, String reviewTitle, String review,
			boolean isRecom, String date, String username) {		
		//With the lockWrite it is added to our HashMap safely
		lock.lockWrite();
		try {
			// FILL IN CODE
			//Initialise it to default value.
			isSuccessful = false;
			//Check the rating is in the correct range or not.
			
			if(1> rating || 5 < rating) {
				// set the false.
				isSuccessful = false;
			} else {
				//Check the date is correct format or not.
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				try {
					Date date1 = sdf.parse(date);
					// set the true.
					isSuccessful = true;
				} catch (ParseException e) {
					//e.printStackTrace();
					System.out.println("Date is invalid!");
				}
			}
			// If successful is true add it.
			//if(isSuccessful && hotelsGivenByHotelId.containsKey(hotelId)) {
			if(isSuccessful) {
				//Set the values to the reviews object.
				reviews = new Review(reviewId, hotelId, reviewTitle, review, username, isRecom, date, rating);			
				//Check that if hotel id already exist or not
				if(!(reviewsGivenByHotelId.containsKey(hotelId))) {
					TreeSet<Review> newReviewSet = new TreeSet<Review>();
					newReviewSet.add(reviews);
					//Add to the reviewsGivenByHotelId TreeMap.
					reviewsGivenByHotelId.put(hotelId, newReviewSet);
				} else {
					TreeSet<Review> existingReviewSet;
					existingReviewSet = reviewsGivenByHotelId.get(hotelId);			
					existingReviewSet.add(reviews);	
					//Add to the reviewsGivenByHotelId TreeMap.
					reviewsGivenByHotelId.put(hotelId, existingReviewSet);
				}
			}
			return isSuccessful; // don't forget to change it
			
		} finally {
			lock.unlockWrite();
		}
		
	}

	
	/**
	 * Creates a TouristAttraction object given the arguments of the method and 
	 * adds information about this attraction to the maps
	 * 
	 * @param attractionId
	 * 				- the id of the attraction
	 * @param name
	 * 				- the name of the attraction
	 * @param rating
	 * 				- the rating of the attraction
	 * @param address
	 * 				- the address of the attraction
	 * @param hotelId
	 * 				- the id of the hotel
	 */
	public void addAttraction(String attractionId, String name, double rating, String address, String hotelId){
		//With the lockWrite it is added to our HashMap safely
		lock.lockWrite();
		try {
			//Set the values to the touristAttraction object.
			touristAttraction = new TouristAttraction(attractionId, name, address, rating);			
			//Check that if hotel id already exist or not
			if(!(attractionsGivenByHotelId.containsKey(hotelId))) {
				TreeMap<String, TouristAttraction> newAttractionSet = new TreeMap<String, TouristAttraction>();
				newAttractionSet.put(attractionId, touristAttraction);
				//Add to the attractionsGivenByHotelId TreeMap.
				attractionsGivenByHotelId.put(hotelId, newAttractionSet);
			} else {
				TreeMap<String, TouristAttraction> existingAttractionSet;
				existingAttractionSet = attractionsGivenByHotelId.get(hotelId);
				existingAttractionSet.put(attractionId, touristAttraction);
				//Add to the reviewsGivenByHotelId TreeMap.
				attractionsGivenByHotelId.put(hotelId, existingAttractionSet);
			}			
		} finally {
			lock.unlockWrite();
		}		
	}
	
	/**
	 * Return an alphabetized list of the ids of all hotels
	 * 
	 * @return
	 */
	public List<String> getHotels() {
		// FILL IN CODE
		
		//With the lockRead it is read from HashMap and written the local variable hotelIdList ArrayList
		lock.lockRead();
		try {
			//Initialise an ArrayList to store hotelIds
			List<String> hotelIdList = new ArrayList<>();
			//Add hotelId to ArrayList
			for (String hotelId: hotelsGivenByHotelId.keySet()){
				hotelIdList.add(hotelId);
			}
			//Sort hotelIds
			Collections.sort(hotelIdList);
			//return it.
			return hotelIdList; // don't forget to change it
		} finally {
			lock.unlockRead();
		}
		
	}

	/**
	 * 
	 * @param hotelId
	 * 			- the id of the hotel
	 * @return
	 * 			- returns a String representing attractions for a particular hotel
	 */
	public String getAttractions(String hotelId){
		//With the lockRead it is read from TreeMap and written the local variable stringBuffer
		lock.lockRead();
		try {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("Attractions near " + hotelsGivenByHotelId.get(hotelId).getHotel_name() 
					+ ", " + hotelId + "" + System.lineSeparator());
			TreeMap<String, TouristAttraction> treeMap = attractionsGivenByHotelId.get(hotelId);
			for (String attractionId : treeMap.keySet()) {
				TouristAttraction touristAttraction = treeMap.get(attractionId);
				stringBuffer.append(touristAttraction.getName() + "; " 
								+ touristAttraction.getAddress() 
			//					+ System.lineSeparator()
								+ System.lineSeparator());
			}
			return stringBuffer.toString();
		} finally {
			lock.unlockRead();
		}
	}
	
	
	/**
	 * Returns a string representing information about the hotel with the given
	 * id, including all the reviews for this hotel separated by
	 * -------------------- Format of the string: HoteName: hotelId
	 * streetAddress city, state -------------------- Review by username: rating
	 * ReviewTitle ReviewText -------------------- Review by username: rating
	 * ReviewTitle ReviewText ...
	 * 
	 * @param hotel
	 *            id
	 * @return - output string.
	 */
	public String toString(String hotelId) {
		// FILL IN CODE
		
		//With the lockRead it is written to the local variable result
		lock.lockRead();
		try {
			StringBuffer stringBuffer = new StringBuffer();
			for (String hotel_id_hotels: hotelsGivenByHotelId.keySet()){
				if(hotel_id_hotels.equals(hotelId)){
					stringBuffer.append(hotelsGivenByHotelId.get(hotel_id_hotels).getHotel_name() + ": "
							+ hotelsGivenByHotelId.get(hotel_id_hotels).getHotel_id() + "\n"
							+ hotelsGivenByHotelId.get(hotel_id_hotels).getAddress().getStreet_address() + "\n"
							+ hotelsGivenByHotelId.get(hotel_id_hotels).getAddress().getCity() + ", "
							+ hotelsGivenByHotelId.get(hotel_id_hotels).getAddress().getState() + "\n");		
				}
			}
			for (String hotel_id_review: reviewsGivenByHotelId.keySet()){
				if(hotel_id_review.equals(hotelId)){
					for(Review hotelIdReview : reviewsGivenByHotelId.get(hotel_id_review)){
						stringBuffer.append("--------------------\n"
								+ "Review by " + hotelIdReview.getUsername() + ": "
								+ hotelIdReview.getRating() + "\n"
								+ hotelIdReview.getReview_title() + "\n"
								+ hotelIdReview.getReview_text() + "\n");
					}
				}
			}
			return stringBuffer.toString(); // don't forget to change to the correct string
		} finally {
			lock.unlockRead();
		}
		
	}

	/**
	 * Save the string representation of the hotel data to the file specified by
	 * filename in the following format: 
	 * an empty line 
	 * A line of 20 asterisks ******************** on the next line 
	 * information for each hotel, printed in the format described in the toString method of this class.
	 * 
	 * @param filename
	 *            - Path specifying where to save the output.
	 */
	public void printToFile(Path filename) {
		// FILL IN CODE
		lock.lockRead();
		try {
			try {
				PrintWriter printWriter = new PrintWriter(new FileWriter(filename.toString()));
				for(String hotelid_info: getHotels()){
					printWriter.println("\n********************");
					printWriter.print(toString(hotelid_info));
				}
				printWriter.flush();
				printWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			lock.unlockRead();
		}
	}
	
	/**
	 * 
	 * @param filename
	 * 				- Path specifying where to save the output.
	 */
	public void printAttractionsNearEachHotel(Path filename){
		lock.lockRead();
		try {
			try {
				PrintWriter printWriter = new PrintWriter(new FileWriter(filename.toString()));
				for(String hotelId : getHotels()){
					printWriter.print(getAttractions(hotelId));
					printWriter.println("++++++++++++++++++++");
				}
				printWriter.flush();
				printWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} finally {
			lock.unlockRead();
		}
	}

	/**
	 * 
	 * @param localtshData
	 * 			- Second ThreadSafeHotelData will be merged with main ThreadSafeHotelData
	 */
	public void merge(ThreadSafeHotelData localtshData) {
		// I locked with lockWrite. Otherwise, deadlock occurs.
		lock.lockWrite();
		try {
			for (String hotel_id_review: localtshData.getReviewsGivenByHotelId().keySet()){
				if(hotelsGivenByHotelId.containsKey(hotel_id_review)){
					reviewsGivenByHotelId.put(hotel_id_review, localtshData.getReviewsGivenByHotelId().get(hotel_id_review));
				}
			}
		} finally {
			lock.unlockWrite();
		}
	}

	/**
	 * 
	 * @return  - reviewsGivenByHotelId 
	 */
	private Map<String, TreeSet<Review>> getReviewsGivenByHotelId() {
		lock.lockRead();
		try{
			return reviewsGivenByHotelId;
		} finally {
			lock.unlockRead();
		}
	}
	
	/**
	 * 
	 * @return
	 * 		- returns HashMap, key is HotelId and value is a partial query containing location information for this hotel
	 */
	public HashMap<String,String> generateQueries() {
		lock.lockRead();
		try {
			String info = "";
			HashMap<String, String> hotelLocationInfo = new HashMap<String, String>();
			for (String hotelId : getHotels()) {
				info = "tourist%20attractions+in+" 
						+ hotelsGivenByHotelId.get(hotelId).getAddress().getCity().replace(" ", "%20")
						+ "&location="
						+ hotelsGivenByHotelId.get(hotelId).getAddress().getLatitude() + ","
						+ hotelsGivenByHotelId.get(hotelId).getAddress().getLongitude();
				hotelLocationInfo.put(hotelId, info);
			}
			return hotelLocationInfo;
		} finally {
			lock.unlockRead();
		}
	}
	
}
