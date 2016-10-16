package cs601.hotelapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Review implements Comparable<Review>{
	
	private String review_id;
	private String hotel_id;
	private String review_title;
	private String review_text;
	private String username;
	private boolean isRecom;
	private String date;
	private int rating;
	
	public Review (String review_id, String hotel_id, String review_title,
			String review_text, String username, boolean isRecom, String date, int rating){
		
		this.review_id = review_id;
		this.hotel_id = hotel_id;
		this.review_title = review_title;
		this.review_text = review_text;
		this.username = username;
		this.isRecom = isRecom;
		this.date = date;
		this.rating = rating;
		
	}

	@Override
	public int compareTo(Review o) {
		// TODO Auto-generated method stub
		/*
		 * This class should implement a Comparable interface so that reviews 
		 * can be compared based on (a) the date (a review is "less" than 
		 * another review, if it was submitted earlier), and, (b) if the dates 
		 * are the same, based on user nicknames (in alphabetical order).
		 */
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date1 = sdf.parse(date);
			Date date2 = sdf.parse(o.getDate());
			
			if(date1.compareTo(date2)>0){
				//System.out.println("date1 is after date2");
				return date1.compareTo(date2); 
			} else if (date1.compareTo(date2)<0) {
				//System.out.println("date1 is before date1");
				return date1.compareTo(date2);
			} else if (date1.compareTo(date2)==0) {
				//System.out.println("date1 is equal to date2");
				if(username.compareTo(o.getUsername())==0){
					return review_id.compareTo(o.getReview_id());
				} else {
					return username.compareTo(o.getUsername());
				}
				
			} else {
				//System.out.println("Something is wrong with date!!!");
			}
			
		} catch (ParseException  e) {
			// TODO: handle exception
			//e.printStackTrace();
			System.out.println("ParseException occured.");
		}
		return 0;
	}	
	
	public String getReview_id() {
		return review_id;
	}

	public void setReview_id(String review_id) {
		this.review_id = review_id;
	}

	public String getHotel_id() {
		return hotel_id;
	}

	public void setHotel_id(String hotel_id) {
		this.hotel_id = hotel_id;
	}

	public String getReview_title() {
		return review_title;
	}

	public void setReview_title(String review_title) {
		this.review_title = review_title;
	}

	public String getReview_text() {
		return review_text;
	}

	public void setReview_text(String review_text) {
		this.review_text = review_text;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean getIsRecom() {
		return isRecom;
	}

	public void setIsRecom(boolean isRecom) {
		this.isRecom = isRecom;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

}
