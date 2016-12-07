package com.od.dev.liberty.api;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;


@Path("/watson/news")
/**
 * REST service to search AlchemyData News API.
 * @see http://docs.alchemyapi.com/
 */
public class AlchemyDataNewsAPI {
	
    public AlchemyDataNewsAPI() {
		
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(
	@QueryParam("startdate") String startdate, 
	@QueryParam("enddate") String enddate, 
	@QueryParam("searchterm") String searchterm, 
	@QueryParam("count") String count) 
    throws Exception {		
		
	// format arguments
	SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");	
	// startdate
	Calendar startdate1 = Calendar.getInstance();
	startdate1.setTime(format1.parse(startdate));
	long startdate2 = startdate1.getTimeInMillis()/1000;
	// enddate
	Calendar enddate1 = Calendar.getInstance();
	enddate1.setTime(format1.parse(enddate));
	long enddate2 = enddate1.getTimeInMillis()/1000;
	// count
	int count2 = Integer.valueOf(count).intValue();
		
	// call the AlchemyData News API
	JsonArray docs = callDataNews(searchterm, startdate2, enddate2, count2);
      
	// create response
	JsonObject jsonObject = new JsonObject();
	jsonObject.addProperty("startdate", startdate);
	jsonObject.addProperty("enddate", enddate);
	jsonObject.addProperty("searchterm", searchterm);
	jsonObject.addProperty("count", count);
	jsonObject.add("docs", docs);
		
	JsonArray jsonArrayResponse = new JsonArray();
	jsonArrayResponse.add(jsonObject);

	return Response.ok(jsonArrayResponse.toString()).build();		
    }
	
private JsonArray callDataNews(String searchterm, long startdate, long enddate, int count) {
		
    String apikey = BluemixConfig.getInstance().getAlchemyApikey();
		
    JsonArray alchemyDocs = null;
	
    String urlGetNews = "https://gateway-a.watsonplatform.net/calls/data/GetNews";
    String params = "outputMode=json"+"&"+
	            "start="+startdate+"&"+
		    "end="+enddate+"&count="+count+"&"+
		    "q.enriched.url.enrichedTitle.keywords.keyword.text="+searchterm+"&"+
		    "return=enriched.url.url,enriched.url.title,enriched.url.publicationDate.date,enriched.url.docSentiment.score"+"&"+
		    "apikey="+apikey;
	
    CloseableHttpResponse response1 = null;
    try {
	CloseableHttpClient httpclient = HttpClients.createDefault();
	HttpGet httpGet = new HttpGet(urlGetNews+"?"+params);
	response1 = httpclient.execute(httpGet);
					
	System.out.println(response1.getStatusLine());
					
	HttpEntity entity1 = response1.getEntity();
	String result = EntityUtils.toString(entity1);
	
	Gson gson = new Gson();
	JsonObject alchemyJsonObject = gson.fromJson(result, JsonObject.class);
	JsonObject resultJsonObject = alchemyJsonObject.get("result").getAsJsonObject();
	alchemyDocs = resultJsonObject.get("docs").getAsJsonArray();
			
    } catch(IOException ioe){
	ioe.printStackTrace();
    }finally {
	try {
	    response1.close();
	} catch(IOException ioe){
	    ioe.printStackTrace();
	}
    }
    return alchemyDocs;
}
}