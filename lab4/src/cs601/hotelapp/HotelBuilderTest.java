package cs601.hotelapp;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.junit.*;

import cs601.concurrent.WorkQueue;

/**
 * Original author srollins, modified by okarpenko.
 *
 */
public class HotelBuilderTest {

	private static final int RUNS = 5;
	private static final int THREADS = 5;
    public static final int TIMEOUT = 6000;

   
	@Test(timeout = TestUtils.TIMEOUT)
	public void testSimpleAddHotel() {
		// Add a hotel to the hotel data, check if it was added correctly
		String testName = "testSimpleAddHotel";
		ThreadSafeHotelData hdata = new ThreadSafeHotelData();
		hdata.addHotel("25622", "Hilton San Francisco Union Square", "San Francisco", "CA", "55 Cyril Magnin St", 37.78,
				-122.4);
		String expected = "Hilton San Francisco Union Square: 25622\n" + "55 Cyril Magnin St\nSan Francisco, CA";
		Assert.assertEquals(String.format("%n" + "Test Case: %s%n", testName), expected,
				hdata.toString("25622").trim());
	}

	@Test(timeout = TestUtils.TIMEOUT)
	public void testSimpleAddHotelReview() {
		// Adds a hotel and a review, checks that they've been added correctly
		String testName = "testSimpleAddHotelReview";
		ThreadSafeHotelData hdata = new ThreadSafeHotelData();
		hdata.addHotel("25622", "Hilton San Francisco Union Square", "San Francisco", "CA", "55 Cyril Magnin St", 37.78,
				-122.4);
		hdata.addReview("25622", "57b717a44751ca0b791823b2", 4, "Room too small",
				"Great location, but the room is too small", true, "2015-03-04", "Xiaofeng");
		// System.out.println(hdata.toString("25622"));
		String expected = "Hilton San Francisco Union Square: 25622\n" + "55 Cyril Magnin St\nSan Francisco, CA\n"
				+ "--------------------\n";
		expected += "Review by Xiaofeng: 4\n" + "Room too small\n" + "Great location, but the room is too small";

		// System.out.println(expected);
		Assert.assertEquals(String.format("%n" + "Test Case: %s%n", testName), expected,
				hdata.toString("25622").trim());
	}

	@Test(timeout = TestUtils.TIMEOUT)
	public void testThreeReviewsSameHotel() {
		// Adds one hotel and three reviews for it. Makes sure the reviews are
		// sorted correctly
		// (by date, and if the dates are equal, by username
		String testName = "testThreeReviewsSameHotel";
		ThreadSafeHotelData hdata = new ThreadSafeHotelData();
		hdata.addHotel("25622", "Hilton San Francisco Union Square", "San Francisco", "CA", "55 Cyril Magnin St", 37.78,
				-122.4);

		hdata.addReview("25622", "57b717a44751ca0b791823b2", 4, "Room too small",
				"Great location, but the room is too small", true, "2015-03-04", "Xiaofeng");
		hdata.addReview("25622", "23d756a64672vr2gwegyhqw4", 5, "Great deal", "Loved the neighborhood, very lively",
				true, "2014-09-05", "Chris");
		hdata.addReview("25622", "92rlnlvnabuwbf256jsf20fj", 3, "Overpriced", "Good location, but very expensive", true,
				"2014-09-05", "Alicia");

		StringBuilder sb = new StringBuilder();
		sb.append("Hilton San Francisco Union Square: 25622\n" + "55 Cyril Magnin St\nSan Francisco, CA\n");
		sb.append("--------------------\n");
		sb.append("Review by Alicia: 3\nOverpriced\nGood location, but very expensive\n");
		sb.append("--------------------\n");
		sb.append("Review by Chris: 5\nGreat deal\nLoved the neighborhood, very lively\n");
		sb.append("--------------------\n");
		sb.append("Review by Xiaofeng: 4\n" + "Room too small\nGreat location, but the room is too small\n");
		String expected = sb.toString();

		// uncomment if your code fails this test to see where the mismatch is:
		/*
		 * System.out.println(hdata.toString("25622"));
		 * System.out.println("==========="); System.out.println(expected );
		 */

		Assert.assertEquals(String.format("%n" + "Test Case: %s%n", testName), expected.trim(),
				hdata.toString("25622").trim());
	}

	@Test(timeout = TestUtils.TIMEOUT)
	public void testInvalidRating() {
		// Tries to add a review where the rating is out of range
		// addReview should return false
		// This review should not be added to the map
		String testName = "testInvalidRating";
		ThreadSafeHotelData hdata = new ThreadSafeHotelData();
		hdata.addHotel("68432", "Best Hotel", "San Francisco", "CA", "762 Market st", 38.0, -120.0);
		boolean isAdded = hdata.addReview("68432", "57b717a44751ca0b791823b2", 7, "Awesome stay",
				"Central location. Free sodas!", true, "2016-09-01", "Phil");
		String expected = "Best Hotel: 68432\n" + "762 Market st\nSan Francisco, CA\n";
		Assert.assertEquals(String.format("%n" + "Test Case: %s%n", testName), false, isAdded);
		Assert.assertEquals(String.format("%n" + "Test Case: %s%n", testName), expected, hdata.toString("68432"));
	}

	@Test(timeout = TestUtils.TIMEOUT)
	public void testInvalidDate() {
		// tries to add a review with invalid date "2014-24"
		// addReview should return false
		String testName = "testInvalidDate";
		ThreadSafeHotelData hdata = new ThreadSafeHotelData();

		boolean isAdded = hdata.addReview("68432", "57b717a44751ca0b791823b2", 5, "Awesome stay",
				"Central location. Free sodas!", true, "2014-24", "Phil");
		Assert.assertEquals(String.format("%n" + "Test Case: %s%n", testName), false, isAdded);
	}

	@Test(timeout = TestUtils.TIMEOUT)
	public void testGetHotels() {
		String testName = "testGetHotels";
		ThreadSafeHotelData hdata = new ThreadSafeHotelData();
		HotelDataBuilder builder = new HotelDataBuilder(hdata);
		builder.loadHotelInfo("input/hotels200.json");
		List<String> hList = hdata.getHotels();
		Collections.sort(hList);

		String[] ids = { "1003", "1006", "10323", "1047", "10939713", "1110785", "1139289", "1145267", "1146383",
				"11509", "11523", "1155453", "11771", "11793", "118232", "1208461", "12239224", "12239317", "12345",
				"12493", "12539", "12671477", "1321986", "13950", "1442078", "14451", "14553", "14742075", "14772741",
				"14781", "150946", "15135", "1515923", "15178126", "15254", "1543026", "15674041", "15769", "15824334",
				"1588312", "1606984", "16552", "1694", "16955", "17280", "17616", "1766605", "1793764", "1813658",
				"18200", "18204", "18320", "18437", "18962", "19035", "19842", "200649", "20191", "20488", "20547",
				"2063", "20701", "21073", "22148", "22500", "22510", "225819", "2336", "23383", "23395", "23406",
				"23581", "23774", "23838", "2391910", "2406", "24468", "24540", "2458097", "24620", "24625", "2508778",
				"25271", "2530359", "2536795", "25622", "25860", "26500", "26760", "26945", "27274", "28200", "283107",
				"28502", "287112", "287665", "2959920", "296720", "2981", "3308", "3552", "360", "3918508", "3941365",
				"3960243", "4044", "40443", "40682", "41833", "422927", "42336", "4302", "437098", "438727", "4432027",
				"444672", "455591", "4564720", "4705", "476728", "480813", "4812964", "487", "491", "4993138", "5043",
				"5045", "50993", "51116", "519729", "522505", "524164", "533579", "5338", "5342", "5361249", "5423384",
				"5425", "547988", "564858", "57255", "578899", "5830", "5875", "5883", "5901", "5984", "599536",
				"6271901", "6505", "662368", "6666", "6706717", "6908747", "693658", "7262522", "7635", "7655", "7713",
				"7825812", "7827145", "789395", "790530", "790579", "791769", "7942", "808403", "8090040", "8121405",
				"8151107", "828", "854854", "855749", "856888", "8638", "8647196", "8727731", "8737", "876", "876315",
				"881699", "883306", "890830", "891239", "894295", "897225", "897333", "9022184", "9043627", "904482",
				"910225", "912982", "915510", "918527", "919198", "9329", "9491356", "9520298", "9613947", "9744315" };

		Assert.assertEquals(String.format("%n" + "Test Case: %s%n" + " Incorrect number of hotels %n", testName),
				ids.length, hList.size());
		//System.out.println(ids.length);

		for (int i = 0; i < ids.length; i++) {
			String hotelId = ids[i];

			if (!hotelId.equals(hList.get(i))) {
				System.out.println("getHotels() did not return the correct list of hotel ids.");
				System.out.println("The " + i + "th element is supposed to be " + ids[i]);
			}
			Assert.assertEquals(String.format("%n" + "Test Case: %s%n", testName), hotelId, hList.get(i));
		}
	}

	@Test(timeout = TestUtils.TIMEOUT)
	public void testConcurrentBuildSmallSet() {
		String testName = "testConcurrentBuildSmallSet";
		ThreadSafeHotelData hdata = new ThreadSafeHotelData();
		WorkQueue queue = new WorkQueue(THREADS);
		HotelDataBuilder builder = new HotelDataBuilder(hdata, queue);
		String inputHotelFile = TestUtils.INPUT_DIR + File.separator + "hotels200.json";
		builder.loadHotelInfo(inputHotelFile);
		
		builder.loadReviews(Paths.get(TestUtils.INPUT_DIR + File.separator + "reviews")); 
		Path actual = Paths.get(TestUtils.OUTPUT_DIR + File.separator + "studentOutput");  // your output
		builder.printToFile(actual);

		builder.shutdown();
		Path expected = Paths.get(TestUtils.OUTPUT_DIR + File.separator + "expectedOutput"); // instructor's
																								// output
																							
		int count = 0;
		try {
			count = TestUtils.checkFiles(expected, actual);
		} catch (IOException e) {
			Assert.fail(String.format("%n" + "Test Case: %s%n" + " File check failed: %s%n", testName, e.getMessage()));
		}

		if (count <= 0) {
			Assert.fail(String.format("%n" + "Test Case: %s%n" + " Mismatched Line: %d%n", testName, -count));
		}

	}
	
	@Test(timeout = TestUtils.TIMEOUT)
	public void testConcurrentBuildLargerSetSeveralThreads() {
		String testName = "testConcurrentBuildLargerSetSeveralThreads";
		ThreadSafeHotelData hdata = new ThreadSafeHotelData();
		WorkQueue queue = new WorkQueue(THREADS);
		HotelDataBuilder builder = new HotelDataBuilder(hdata, queue);
		String inputHotelFile = TestUtils.INPUT_DIR + File.separator + "hotels200.json";
		builder.loadHotelInfo(inputHotelFile);
		
		builder.loadReviews(Paths.get(TestUtils.INPUT_DIR + File.separator + "reviews8000")); 
		Path actual = Paths.get(TestUtils.OUTPUT_DIR + File.separator + "studentOutput8000");  // your output
		builder.printToFile(actual);
		
		builder.shutdown();
		Path expected = Paths.get(TestUtils.OUTPUT_DIR + File.separator + "expectedOutput8000"); // instructor's
																								// output
																							
		int count = 0;
		try {
			count = TestUtils.checkFiles(expected, actual);
		} catch (IOException e) {
			Assert.fail(String.format("%n" + "Test Case: %s%n" + " File check failed: %s%n", testName, e.getMessage()));
		}

		if (count <= 0) {
			Assert.fail(String.format("%n" + "Test Case: %s%n" + " Mismatched Line: %d%n", testName, -count));
		}

	}
	
	@Test(timeout = TestUtils.TIMEOUT)
	public void testConcurrentBuildLargerSetOneThread() {
		String testName = "testConcurrentBuildLargerSetOneThread";
		ThreadSafeHotelData hdata = new ThreadSafeHotelData();
		WorkQueue queue = new WorkQueue(1);
		HotelDataBuilder builder = new HotelDataBuilder(hdata, queue);
		String inputHotelFile = TestUtils.INPUT_DIR + File.separator + "hotels200.json";
		builder.loadHotelInfo(inputHotelFile);
		
		builder.loadReviews(Paths.get(TestUtils.INPUT_DIR + File.separator + "reviews8000")); 
		Path actual = Paths.get(TestUtils.OUTPUT_DIR + File.separator + "studentOutput8000");  // your output
		builder.printToFile(actual);

		builder.shutdown();
		Path expected = Paths.get(TestUtils.OUTPUT_DIR + File.separator + "expectedOutput8000"); // instructor's
																								// output
																							
		int count = 0;
		try {
			count = TestUtils.checkFiles(expected, actual);
		} catch (IOException e) {
			Assert.fail(String.format("%n" + "Test Case: %s%n" + " File check failed: %s%n", testName, e.getMessage()));
		}

		if (count <= 0) {
			Assert.fail(String.format("%n" + "Test Case: %s%n" + " Mismatched Line: %d%n", testName, -count));
		}
	}
	
	/**
	 * Tests the hotel builder output multiple times, to make sure the
	 * results are always consistent.
	 */
	@Test(timeout = TIMEOUT * RUNS)
	public void testHotelDataConsistency() {
		
		for (int i = 0; i < RUNS; i++) {
			testConcurrentBuildLargerSetSeveralThreads();
		}
	}

	
	
	

}