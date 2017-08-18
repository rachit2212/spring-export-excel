package com.tothenew.myapp.excelUtilities;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class CommonUtility {
	
	public static Date truncateToDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public static long getUTCTime(Date date) {
		return Date.UTC(date.getYear(),date.getMonth(), date.getDate(), 0, 0, 0);
	}

	public static long getDifferenceDays(Date d1, Date d2) {
		long diff = CommonUtility.truncateToDay(d2).getTime() - CommonUtility.truncateToDay(d1).getTime();
		return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}

	public static Date addDays(Date date, int days) {
		return new Date(date.getTime() + TimeUnit.DAYS.toMillis(days));
	}

	public static JSONArray getDataFromAPI(String urlString, boolean mockResponse) {
		String apiResponse = null;
		if(mockResponse) {
			apiResponse = getMockedResponse("api1.json");
		}else{ 
			apiResponse = getAPIRespose(urlString);
		}
		JSONObject jsonObject = null;
		try {
			JSONParser jsonParser = new JSONParser();
			jsonObject = (JSONObject) jsonParser.parse(apiResponse);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return (JSONArray) jsonObject.get("series");
	}

	public static JSONObject getValue(JSONArray array, long key) {
		JSONObject value = null;
		for (int i = 0; i < array.size(); i++) {
			JSONObject jsonObject = (JSONObject) array.get(i);

			Long hourMs = (Long) jsonObject.get("hourMs");

			if (hourMs.equals(key)) {
				value = jsonObject;
				break;

			}
		}

		return value;
	}

	public static void updateCurrentRowActualScore(int currentrow, JSONObject currentObj, HSSFSheet sheet) {

		// get row to update which is a key in map
		// iterate over pulse score object and update the pulse score in actual
		// field

		for (Iterator iterator = currentObj.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			String value = currentObj.get(key).toString();

			int cellNumber = ReadExcelWithFormula.searchKey(sheet, 8, key);
			System.out.println("writing data to " + currentrow);
			if (value.equals("NA")) {
				setcellValue(sheet, currentrow - 1, cellNumber + 2, 0d);
			} else {
				setcellValue(sheet, currentrow - 1, cellNumber + 2, Double.parseDouble(value));
			}

		}

	}

	public static void setcellValue(Sheet sheet, int row, int column, Double value) {
		if (sheet.getRow(row) == null) {
			sheet.createRow(row);
		}
		if (value != null) {
			sheet.getRow(row).getCell(column, Row.CREATE_NULL_AS_BLANK).setCellValue(value);
			System.out.println(sheet.getRow(row).getCell(column, Row.CREATE_NULL_AS_BLANK));

		}
	}

	public static String getAPIRespose(String urlString) {
		StringBuilder result = new StringBuilder();
		try {
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();

			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			rd.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result.toString();
	}

	public static String getMockedResponse(String mockResponseFileName) {
		StringBuilder result = new StringBuilder();
		try {
			ClassLoader classLoader = ReadExcelWithFormula.class.getClassLoader();
			BufferedReader rd = new BufferedReader(new FileReader(classLoader.getResource(mockResponseFileName).getFile()));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}

			rd.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result.toString();
	}

}
