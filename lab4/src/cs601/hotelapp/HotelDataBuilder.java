package cs601.hotelapp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import cs601.concurrent.WorkQueue;

public class HotelDataBuilder {	
	private ThreadSafeHotelData tshdata;
	private final WorkQueue workQueue;
	private volatile int numTasks; // how many runnable tasks are pending
	
	public HotelDataBuilder(ThreadSafeHotelData tshdata) {
		this.tshdata = tshdata;
		workQueue = new WorkQueue();
		numTasks = 0;
	}
	
	public HotelDataBuilder(ThreadSafeHotelData tshdata, WorkQueue q) {
		this.tshdata = tshdata;
		workQueue = q;
		numTasks = 0;
	}
	
	/**
	 * Read the json file with information about the hotels (id, name, address,
	 * etc) and load it into the appropriate data structure(s). Note: This
	 * method does not load reviews
	 * 
	 * @param filename
	 *            the name of the json file that contains information about the
	 *            hotels
	 */
	public void loadHotelInfo(String jsonFilename) {

		// Hint: Use JSONParser from JSONSimple library
		// FILL IN CODE
		
		//Get the file directory and find the path
		Path jsonFileNameDirectory = Paths.get(jsonFilename);
		String jsonFilenameString = jsonFileNameDirectory.toAbsolutePath().toString();
		
		
		JSONParser parser = new JSONParser(); 
		try {
			Object object = parser.parse(new FileReader(jsonFilenameString));
			JSONObject jsonObject = (JSONObject) object;
			
			JSONArray listOfHotel = (JSONArray) jsonObject.get("sr");
			JSONObject jsonObjectHotel;
			
			for (int i=0; i<listOfHotel.size();i++) {
				jsonObjectHotel = (JSONObject) listOfHotel.get(i);
				
				// Get hotelId.
				String hotelId = (String) jsonObjectHotel.get("id");
				// Get hotelName.
				String hotelName = (String) jsonObjectHotel.get("f");
				// Get hotelCity.
				String hotelCity = (String) jsonObjectHotel.get("ci");
				// Get hotelState
				String hotelState = (String) jsonObjectHotel.get("pr");
				// Get hotelStreetAddress
				String hotelStreetAddress = (String) jsonObjectHotel.get("ad");
				//Create jsonObjectHotelLL to get Lat and Lng
				JSONObject jsonObjectHotelLL = (JSONObject) jsonObjectHotel.get("ll");
				// Get hotelLat
				double hotelLat = Double.parseDouble((String) jsonObjectHotelLL.get("lat"));
				// Get hotelLon
				double hotelLon = Double.parseDouble((String) jsonObjectHotelLL.get("lng"));
				
				// Add to the hotelsGivenByHotelId
				tshdata.addHotel(hotelId, hotelName, hotelCity, hotelState, hotelStreetAddress, hotelLat, hotelLon);
				
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}
	
	}

	private class LoadReviewsWorker implements Runnable {
		private Path p;
		private ThreadSafeHotelData localtshData;
		LoadReviewsWorker(Path p) {
			this.p = p;
			localtshData = new ThreadSafeHotelData();
			incrementNumTasks();
		}

		@Override
		public void run() {
			
			JSONParser jsonParser = new JSONParser();
			try {
				JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(p.toAbsolutePath().toString()));
				
				//reviewDetails Object
				JSONObject reviewDetails = (JSONObject) jsonObject.get("reviewDetails");
				//reviewCollection Object
				JSONObject reviewCollection = (JSONObject) reviewDetails.get("reviewCollection");
				//review Array
				JSONArray review = (JSONArray) reviewCollection.get("review");
				JSONObject reviewObject;
				for(int i=0; i<review.size();i++){
					reviewObject = (JSONObject) review.get(i);
					
					String hotelId = (String) reviewObject.get("hotelId");
					String reviewId = (String) reviewObject.get("reviewId");
					long ratingLong = (long) reviewObject.get("ratingOverall");
					int rating = (int) ratingLong;
					String reviewTitle = (String) reviewObject.get("title");
					String reviewText = (String) reviewObject.get("reviewText");
					boolean isRecom = ("YES" == (String) reviewObject.get("isRecommended"));
					String date = (String) reviewObject.get("reviewSubmissionTime");								
					String username = (String) reviewObject.get("userNickname");
					if(username.equals("")){ username = "anonymous"; }
					//Add local review
					localtshData.addReview(hotelId, reviewId, rating, reviewTitle, reviewText, isRecom, date, username);
					//tshdata.addReview(hotelId, reviewId, rating, reviewTitle, reviewText, isRecom, date, username);
				}
				//merge local reviews to global review
				mergeReviews(localtshData);
			
			} catch (org.json.simple.parser.ParseException e) {
				System.out.println(p.toString());
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				decrementNumTask();
			}
		}
	}

	/**
	 * Load reviews for all the hotels into the appropriate data structure(s).
	 * Traverse a given directory recursively to find all the json files with
	 * reviews and load reviews from each json. Note: this method must be
	 * recursive and use DirectoryStream as discussed in class.
	 * 
	 * @param path
	 *            the path to the directory that contains json files with
	 *            reviews Note that the directory can contain json files, as
	 *            well as subfolders (of subfolders etc..) with more json files
	 */
	public void loadReviews(Path path) {
		// FILL IN CODE
		try {
			DirectoryStream<Path> pathsList = Files.newDirectoryStream(path);
			for(Path p : pathsList){
				// check that file is directory or not.
				if(!Files.isDirectory(p)){
					//send to Threads
					workQueue.execute(new LoadReviewsWorker(p));
				} else if (Files.isDirectory(p)) {
					// If it is, check the subfolders.
					// this method get the paremeter path, and in it, check sub directories.
					loadReviews(p);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param localtshData
	 * 			- merge local reviews to global review
	 */
	public synchronized void mergeReviews(ThreadSafeHotelData localtshData) {
		tshdata.mergeReviews(localtshData);
	}

	/**
	 *  Wait for all pending work to finish
	 */
	public synchronized void waitUntilFinished() {
		while(numTasks > 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * increment number of task in synchronised way
	 */
	public synchronized void incrementNumTasks() {
		numTasks++;
	}

	/**
	 * decrement number of task in synchronised way
	 */
	public synchronized void decrementNumTask() {
		numTasks--;
		if(numTasks <= 0){
			notifyAll();
		}
	}

	/**
	 * Wait until there is no pending work, then shutdown the queue
	 */
	public synchronized void shutdown(){
		waitUntilFinished();
		workQueue.shutdown();
	}
	
	/**
	 * 
	 * @return numTasks - For testing, it is created.
	 */
	public synchronized int getNumTasks() {
		return numTasks;
	}

	/**
	 * 
	 * @param filename
	 * 			- Path specifying where to save the output.
	 */
	public void printToFile(Path filename) {
		waitUntilFinished();
		tshdata.printToFile(filename);
	}
	
	/**For testing, it is created (in main method)
	 * 
	 * @param filename
	 * 			- Path of file (For testing)
	 */
	public void print(Path filename){
		waitUntilFinished();
		tshdata.printAttractionsNearEachHotel(filename);
		
	}
	
	/** This class is multithreaded.
	 * (the process of fetching attractions)
	 * 
	 * @author akin_
	 *
	 */
	private class FetchAttractionsWorker implements Runnable{
		private int radiusInMiles;
		private ThreadSafeHotelData localtshData;
		private SSLSocketFactory sslSocketFactory = null;
		private SSLSocket sslSocket = null;
		private PrintWriter printWriter = null;
		private BufferedReader bufferedReader = null;
		private String host = "maps.googleapis.com";
		private String request = "";
		private String jsonObjectString = "";
		private String hotelId;
		private String hotelLocationInfo;
		
		FetchAttractionsWorker(String hotelId, String hotelLocationInfo, int radiusInMiles){
			this.radiusInMiles = radiusInMiles;
			this.hotelId = hotelId;
			this.hotelLocationInfo = hotelLocationInfo;
			localtshData = new ThreadSafeHotelData();
			incrementNumTasks();
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			try {
				// HTTPS uses port 443
				sslSocket = (SSLSocket) sslSocketFactory.createSocket(host, 443);
				// output stream for the secure socket
				printWriter = new PrintWriter(new OutputStreamWriter(sslSocket.getOutputStream()));
				request = getRequest(host,hotelLocationInfo,radiusInMiles);
				System.out.println(request );
				//send a request to the server
				printWriter.println(request);
				printWriter.flush();
				
				// input stream for the secure socket.
				bufferedReader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
				String str;
				StringBuffer stringBuffer = new StringBuffer();
				while((str = bufferedReader.readLine()) != null ) {
					stringBuffer.append(str);
				}
				jsonObjectString = stringBuffer.toString();
				
				//remove headers and get jsonObject
				Pattern p = Pattern.compile("Connection: close(.*)");
				Matcher matcher = p.matcher(jsonObjectString);
				if(matcher.find()){
					jsonObjectString = matcher.group(1);
					//System.out.println(jsonObjectString);	
				}
				
				JSONParser jsonParser = new JSONParser();
				try {
					JSONObject jsonObject = (JSONObject) jsonParser.parse(jsonObjectString);
					JSONArray jsonArray = (JSONArray) jsonObject.get("results");
					JSONObject jsonObjectAttraction;
					
					for(int i=0; i<jsonArray.size();i++) {
						jsonObjectAttraction = (JSONObject) jsonArray.get(i);
						
						String attractionId = (String) jsonObjectAttraction.get("id");
						String name = (String) jsonObjectAttraction.get("name");
						String address = (String) jsonObjectAttraction.get("formatted_address");
						double rating = 0;
						if(jsonObjectAttraction.get("rating") != null){
							rating = ((Number)jsonObjectAttraction.get("rating")).doubleValue();
						}
						//Add local Attraction
						localtshData.addAttraction(attractionId, name, rating, address, hotelId);
						//tshdata.addAttraction(attractionId, name, rating, address, hotelId);
					}
					//merge to global Alttractions
					tshdata.mergeAttractions(localtshData);
				} catch (ParseException e) {
					e.printStackTrace();
				}			
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					printWriter.close();
					bufferedReader.close();
					sslSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					decrementNumTask();
				}
			}
		}
	}

	/** For each hotel in the ThreadSafeHotelData, 
	 * New Worker is created to handle the Attractions
	 * 
	 * @param radiusInMiles
	 * 				- near distance amount
	 */
	public void fetchAttractions(int radiusInMiles){
		HashMap<String, String> hotelLocationInfo;
		
		hotelLocationInfo = tshdata.generateQueries();
		for(String hotelId : hotelLocationInfo.keySet()){
			workQueue.execute(new FetchAttractionsWorker(hotelId, hotelLocationInfo.get(hotelId) ,radiusInMiles));
		}
	}
	
	/**
	 * 
	 * @param host
	 * 			- "maps.googleapis.com"
	 * @param hotelLocationInfo
	 * 			- represent location infos in string example = "tourist%20attractions+in+Emeryville&location=37.837773,-122.298142"
	 * @param radiusInMiles
	 * 			- near distance amount 
	 * @return
	 * 			- returns string represents Get request 
	 */
	private String getRequest(String host, String hotelLocationInfo, int radiusInMiles) {
		String result;
		String key = "AIzaSyCvBVHwB8nRJDMKHI1WxkNR0kZMhnI9_oU";
		//String key = "AIzaSyDhsmtS2ZuTEg3scxv2ZsipglHNBgw3vB4";
		int radiusInMeters;
		
		radiusInMeters = radiusInMiles * 1609;		
		result = "GET /maps/api/place/textsearch/json?query=" + hotelLocationInfo 
					+ "&radius=" + radiusInMeters + "&key=" + key + " HTTP/1.1" + System.lineSeparator() // GET request
					+ "Host: " + host + System.lineSeparator() // Host header required for HTTP/1.1
					+ "Connection: close" + System.lineSeparator() // make sure the server closes the connection after we fetch one page
					+ System.lineSeparator();
		return result;
	}

	public static void main(String[] args) {
		ThreadSafeHotelData tsData = new ThreadSafeHotelData();
		HotelDataBuilder hotelDataBuilder = new HotelDataBuilder(tsData);
		hotelDataBuilder.loadHotelInfo("input/hotels4.json");
		hotelDataBuilder.fetchAttractions(20);
		hotelDataBuilder.print(Paths.get("outputFile"));
		hotelDataBuilder.shutdown();
	}
	
}
