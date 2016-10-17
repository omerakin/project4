package cs601.hotelapp;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.junit.*;

/**
 * Author: okarpenko.
 *
 */
public class Lab4Test {

	public static final String INPUT_DIR = "input";
	public static final String OUTPUT_DIR = "test";
	public static final int TIMEOUT = 60000;

	/**
	 * A helper method that reads the student output file and checks if it
	 * contains expected attractions
	 */
	public void testIfContainsExpectedAttractions(String testName, String filename,
			Map<String, String[]> allExpectedAttractions) {

		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				// line: the hotel name + id
				String[] parts = line.split(", ");
				if (parts.length != 2) {
					System.out.println("line = " + line);
					System.out.println("Wrong format of the output file in test " + testName);
					Assert.fail();
				}
				String hotelId = parts[1];
				System.out.println("hotelId = " + hotelId);
				String[] expectedAttractionsForHotelId = allExpectedAttractions.get(hotelId);
				if (expectedAttractionsForHotelId == null) {
					System.out.println("No attractions for the hotel id = " + hotelId);
					Assert.fail();
				}
				StringBuffer sb = new StringBuffer();
				boolean doneWithThisHotel = false;
				while ( !doneWithThisHotel && ((line = br.readLine()) != null)) {
					if (line.equals("++++++++++++++++++++"))
						doneWithThisHotel = true;
					else
						sb.append(line + System.lineSeparator());

				}
				String result = sb.toString();
				//System.out.println("result = " + result);
				for (String attractionInfo : expectedAttractionsForHotelId) {
					//System.out.println("Testing attraction " + attractionInfo);
					Assert.assertEquals(String.format("%n" + "Test Case: %s%n + Attraction not found: %s%n HotelId = %s%n", testName, attractionInfo, hotelId),
							result.contains(attractionInfo), true);
				}

			}

		} catch (IOException e) {
			System.out.println(
					"IOException in the test can not read from the file  " + filename + System.lineSeparator() + e);
			Assert.fail();
		}
	}

	@Test(timeout = TIMEOUT)
	public void testFetchAttractionsHotels1Radius2() {
		// fetch attractions in the radius of 2 miles from the hotel in
		// hotels1.json
		String testName = "testFetchAttractionsHotels1Radius2";
		ThreadSafeHotelData hdata = new ThreadSafeHotelData();
		HotelDataBuilder builder = new HotelDataBuilder(hdata);
		String inputHotelFile = INPUT_DIR + File.separator + "hotels1.json";
		builder.loadHotelInfo(inputHotelFile);

		builder.fetchAttractions(2);
		builder.shutdown();

		Path actual = Paths.get(OUTPUT_DIR + File.separator + "studentOutput1Rad2"); // your
																						// output
		hdata.printAttractionsNearEachHotel(actual);

		String[] expectedAttractions = { "Emeryville Marina; 3310 Powell St, Emeryville, CA 94608, United States",
				"Pottery and Beyond; 4055 Hubbard St, Emeryville, CA 94608, United States",
				"Shorebird Park Emeryville; W Frontage Rd & Access Rd, Emeryville, CA 94608, United States",
				"Emeryville Recreation Department; 4300 San Pablo Ave, Emeryville, CA 94608, United States" };
		Map<String, String[]> allAttractions = new TreeMap<>();
		allAttractions.put("10323", expectedAttractions);
		testIfContainsExpectedAttractions(testName, actual.toString(), allAttractions);
	}

	@Test(timeout = 4 * TIMEOUT)
	public void testFetchAttractionsHotels4Radius2() {
		// fetch attractions close to hotels in hotels4.json
		String testName = "testFetchAttractionsHotels4Radius2";
		ThreadSafeHotelData hdata = new ThreadSafeHotelData();
		HotelDataBuilder builder = new HotelDataBuilder(hdata);
		String inputHotelFile = INPUT_DIR + File.separator + "hotels4.json";
		builder.loadHotelInfo(inputHotelFile);

		builder.fetchAttractions(2);
		builder.shutdown(); // since HotelDataBuilder creates a queue.

		Path actual = Paths.get(OUTPUT_DIR + File.separator + "studentOutput4Rad2"); // your output
		hdata.printAttractionsNearEachHotel(actual);
		Map<String, String[]> allAttractions = new TreeMap<>();
		String[] expectedAttractions10323 = { "Emeryville Marina; 3310 Powell St, Emeryville, CA 94608, United States",
				"Pottery and Beyond; 4055 Hubbard St, Emeryville, CA 94608, United States",
				"Shorebird Park Emeryville; W Frontage Rd & Access Rd, Emeryville, CA 94608, United States",
				"Emeryville Recreation Department; 4300 San Pablo Ave, Emeryville, CA 94608, United States" };
		allAttractions.put("10323", expectedAttractions10323);

		String[] expectedAttractions12539 = {
				"San Francisco CityPASS; 900 Market St, San Francisco, CA 94102, United States",
				"San Francisco Museum of Modern Art; 151 3rd St, San Francisco, CA 94103, United States",
				"PIER 39; Beach Street & The Embarcadero, San Francisco, CA 94133, United States",
				"Coit Tower; 1 Telegraph Hill Blvd, San Francisco, CA 94133, United States",
				"Alcatraz Island; San Francisco, CA 94133, United States",
				"California Academy of Sciences; 55 Music Concourse Dr, San Francisco, CA 94118, United States",
				"Golden Gate Park; San Francisco, CA, United States",
				"USS Pampanito; Fishermans Wharf, San Francisco, CA 94133, United States",
				"Aquarium of the Bay; 2 Beach St, San Francisco, CA 94133, United States",
				//"Legion of Honor; 100 34th Ave, San Francisco, CA 94121, United States",
				"Japanese Tea Garden; 75 Hagiwara Tea Garden Dr, San Francisco, CA 94102, United States",
				"Musée Mécanique; Pier 45, A, San Francisco, CA 94133, United States",
				"Chinatown San Francisco; Stockton St Tunnel, San Francisco, CA 94108, United States",
				"Twin Peaks; 501 Twin Peaks Blvd, San Francisco, CA 94114, United States"};
				//"Madame Tussauds San Francisco; 145 Jefferson St, San Francisco, CA 94133, United States" };

		allAttractions.put("12539", expectedAttractions12539);

		String[] expectedAttractions16955 = {
				"Eaglerider San Francisco BMW - Ducati - Honda Motorcycle Rental; 136 South Linden Avenue H, South San Francisco, CA 94080, United States",
				"Treasure Island RV Park; 1700 El Camino Real, South San Francisco, CA 94080, United States",
				"South San Francisco Parks and Recreation Department; 33 Arroyo Dr, South San Francisco, CA 94080, United States",
				"Sign Hill Park; 400 Grand Ave, South San Francisco, CA 94080, United States",
				"Alcatraz Island; San Francisco, CA 94133, United States",
				"California Academy of Sciences; 55 Music Concourse Dr, San Francisco, CA 94118, United States",
				"Orange Memorial Park; Orange Ave, South San Francisco, CA 94080, United States",
				"Buri Buri Park; South San Francisco, CA 94080, United States",
				"Brentwood Park; Rosewood & Briarwood, South San Francisco, CA 94080, United States"};
				//"Westborough Recreation Building; 2380 Galway Dr, South San Francisco, CA 94080, United States",
				//"Centennial Dog Park; SSF Centennial Trail, South San Francisco, CA 94080, United States" };
		allAttractions.put("16955", expectedAttractions16955);

		String[] expectedAttractions1047 = {
				"San Francisco CityPASS; 900 Market St, San Francisco, CA 94102, United States",
				"Alcatraz Island; San Francisco, CA 94133, United States",
				"San Francisco Museum of Modern Art; 151 3rd St, San Francisco, CA 94103, United States",
				"PIER 39; Beach Street & The Embarcadero, San Francisco, CA 94133, United States",
				"Coit Tower; 1 Telegraph Hill Blvd, San Francisco, CA 94133, United States",
				"California Academy of Sciences; 55 Music Concourse Dr, San Francisco, CA 94118, United States",
				"Golden Gate Park; San Francisco, CA, United States",
				"San Francisco Deluxe Sightseeing Tours; 2737 Taylor St, San Francisco, CA 94133, United States",
				"USS Pampanito; Fishermans Wharf, San Francisco, CA 94133, United States",
				//"Legion of Honor; 100 34th Ave, San Francisco, CA 94121, United States",
				"Aquarium of the Bay; 2 Beach St, San Francisco, CA 94133, United States",
				"Japanese Tea Garden; 75 Hagiwara Tea Garden Dr, San Francisco, CA 94102, United States",
				"Musée Mécanique; Pier 45, A, San Francisco, CA 94133, United States",
				"Chinatown San Francisco; Stockton St Tunnel, San Francisco, CA 94108, United States",
		};
		
		allAttractions.put("1047", expectedAttractions1047);
		testIfContainsExpectedAttractions(testName, actual.toString(), allAttractions);

	}

}