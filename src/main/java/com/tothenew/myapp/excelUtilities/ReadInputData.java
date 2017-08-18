package com.tothenew.myapp.excelUtilities;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class ReadInputData {
	
	public static JSONParser parser = new JSONParser();
	
	@SuppressWarnings("finally")
	public static JSONArray getInputData() {
		JSONObject jsonObject = null;
		JSONArray jsonarray = null;
		try {
			/*
			 * jsonarray = (JSONArray) parser.parse(new
			 * FileReader("input.json")); System.out.println(">>>>>>>>>>>>" +
			 * jsonarray);
			 */
			// To connect to mongodb server
			MongoClient mongoClient = new MongoClient("localhost", 27017);

			// Now connect to your databases
			DB db = mongoClient.getDB("TVLArticlesDB");
			System.out.println("Connect to database successfully" + db);

			DBCollection coll = db.getCollection("articles_3_2016");

			DBObject query = new BasicDBObject();
			query.put("tags.0.ISIN", "US30303M1027");
			BasicDBObject fields = new BasicDBObject("tvl2Cats", 1).append(
					"articlePubDateMs", 1);
			DBObject orderBy = new BasicDBObject("articlePubDateMs", 1);
			List<DBObject> arr = (ArrayList<DBObject>) coll.find(query, fields)
					.sort(orderBy).toArray();

			Object object = null;
			JSONParser jsonParser = new JSONParser();
			object = jsonParser.parse(arr.toString());
			jsonarray = (JSONArray) object;
			System.out.println(">>>>>>>>>>>>>. abc :::" + arr.size() + ":::"
					+ jsonarray);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return jsonarray;
		}
	}
	
	
	
	public static JSONArray readInputDataFromAPI(String apiUrl, boolean mockResponse) {
		String apiResponse = null;
		if(mockResponse) {
			apiResponse = CommonUtility.getMockedResponse("api2.json");
		} else {
			apiResponse = CommonUtility.getAPIRespose(apiUrl);
		} 

		JSONArray jsonArr = null;
		JSONArray newJsonArray = new JSONArray();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		
		try {
			Date startDate=new SimpleDateFormat("dd/MM/yyyy").parse("01/01/2015");  
			JSONParser jsonParser = new JSONParser();
			jsonArr = (JSONArray) jsonParser.parse(apiResponse);
			Date previousDate=null;
			JSONObject previousData=null;
			for (int i = 0; i < jsonArr.size(); i++) {
				JSONObject newObj = new JSONObject();
				 JSONObject jsonObj = (JSONObject) jsonArr.get(i);
				 JSONObject tvl2cats=(JSONObject) jsonObj.get("tvl2Cats");
				if(! tvl2cats.toJSONString().equals("{}")){
				 newObj.put("tvl2Cats", tvl2cats);
				 Date date = format.parse((String)jsonObj.get("pubDate"));
				 newObj.put("pubDate",CommonUtility.truncateToDay(date));
				
				 if(previousDate!=null){
					long daysDiff= CommonUtility.getDifferenceDays(previousDate,date);
					    int j=1;
						while(daysDiff-- >1){
							JSONObject newDataObj = new JSONObject();
							//newDataObj.put("tvl2Cats", previousData.get("tvl2Cats"));
							newDataObj.put("pubDate", CommonUtility.truncateToDay(CommonUtility.addDays(previousDate, j++)));
							newJsonArray.add(newDataObj);
						}
				 }
				 newJsonArray.add(newObj);
				 previousDate=date;
				 previousData=newObj;
			}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
		return newJsonArray;
	}

}