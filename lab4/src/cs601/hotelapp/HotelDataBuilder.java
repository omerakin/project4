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
				merge(localtshData);
			
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
	public synchronized void merge(ThreadSafeHotelData localtshData) {
		tshdata.merge(localtshData);
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
	
	
	public void print(Path filename){
		waitUntilFinished();
		tshdata.printAttractionsNearEachHotel(filename);
		
	}
	/**
	 * 
	 * @param radiusInMiles
	 */
	public void fetchAttractions(int radiusInMiles){
		SSLSocketFactory sslSocketFactory = null;
		SSLSocket sslSocket = null;
		PrintWriter printWriter = null;
		BufferedReader bufferedReader = null;
		String host = "maps.googleapis.com";
		String request = "";
		String jsonObjectString = "";
		
		sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		try {
			// HTTPS uses port 443
			sslSocket = (SSLSocket) sslSocketFactory.createSocket(host, 443);
			// output stream for the secure socket
			printWriter = new PrintWriter(new OutputStreamWriter(sslSocket.getOutputStream()));
			request = getRequest(host,radiusInMiles);
			//check the string
			//System.out.println(request);
			
			//send a request to the server
			printWriter.println(request);
			printWriter.flush();
			
			// input stream for the secure socket.
			bufferedReader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
			String str;
			StringBuffer stringBuffer = new StringBuffer();
			while((str = bufferedReader.readLine()) != null ) {
				stringBuffer.append(str);
				//System.out.println(str);
			}
			jsonObjectString = stringBuffer.toString();
			//System.out.println(jsonObject);
			
			//remove headers and get jsonObject
			Pattern p = Pattern.compile("Connection: close(.*)");
			Matcher matcher = p.matcher(jsonObjectString);
			if(matcher.find()){
				jsonObjectString = matcher.group(1);
				System.out.println(jsonObjectString);	
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
					//double rating = (double) jsonObjectAttraction.get("rating");
					
					
					tshdata.addAttraction(attractionId, name, rating, address, "5361249");
				}
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
			}
		}
	}
	
	private String getRequest(String host, int radiusInMiles) {
		String result;
		String key = "AIzaSyCvBVHwB8nRJDMKHI1WxkNR0kZMhnI9_oU";
		radiusInMiles = 20;
		
		result = "GET /maps/api/place/textsearch/json?query=tourist%20attractions+in+Santa%20Marta&location=-74.225873,11.203319" 
					+ "&radius=" + radiusInMiles + "&key=" + key + " HTTP/1.1" + System.lineSeparator() // GET request
					+ "Host: " + host + System.lineSeparator() // Host header required for HTTP/1.1
					+ "Connection: close" + System.lineSeparator() // make sure the server closes the connection after we fetch one page
					+ System.lineSeparator();
		return result;
	}

	public static void main(String[] args) {
		/*
		ThreadSafeHotelData tsData = new ThreadSafeHotelData();
		HotelDataBuilder hotelDataBuilder = new HotelDataBuilder(tsData);
		//hotelDataBuilder.loadHotelInfo("inputLab1/hotels200.json");
		//hotelDataBuilder.loadReviews(Paths.get("input/reviews"));
		hotelDataBuilder.loadHotelInfo("input/hotels200.json");
		hotelDataBuilder.loadReviews(Paths.get("input/reviews8000"));
		hotelDataBuilder.waitUntilFinished();
		hotelDataBuilder.printToFile(Paths.get("outputFile"));
		hotelDataBuilder.shutdown();
		*/
		
		ThreadSafeHotelData tsData = new ThreadSafeHotelData();
		HotelDataBuilder hotelDataBuilder = new HotelDataBuilder(tsData);
		hotelDataBuilder.fetchAttractions(20);
		hotelDataBuilder.print(Paths.get("outputFile"));
		hotelDataBuilder.shutdown();
		
	}
	
}
