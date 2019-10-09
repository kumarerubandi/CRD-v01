package org.hl7.davinci.endpoint.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.hl7.davinci.RequestIncompleteException;
import org.hl7.davinci.endpoint.YamlConfig;
import org.hl7.davinci.endpoint.database.CoverageRequirementRule;
import org.hl7.davinci.endpoint.database.DataService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * Defines pages by searching for returned string in the resources/templates directory.
 * Making changes here will alter the home page.
 * The "Model" parameter can be given attributes which can be referenced in the html
 * Thymeleaf provides the ability to reference and use the attributes.
 */
@Controller
public class HomeController {
  static final Logger logger = LoggerFactory.getLogger(HomeController.class);


  @Autowired
  private DataService dataService;
  private YamlConfig config;

  @RequestMapping("/")
  public String index(Model model, final HttpServletRequest request) {
    model.addAttribute("contextPath", request.getContextPath());
    return "index";
  }

  @GetMapping("/data")
  public String data(Model model, final HttpServletRequest request) {
    model.addAttribute("contextPath", request.getContextPath());
    request.getContextPath();
    return "index";
  }

  /**
   * Accepts form submissions to create new entries in the database, then redirects to the
   * data page to keep the user on the same page and show the change to the database.
   * @param datum the data object to be added to the database
   * @param errors any errors incurred from the form submission
   * @return an object that contains the model and view of the data page
   */
  @PostMapping("/data")
  public ModelAndView saveDatum(@ModelAttribute CoverageRequirementRule datum,
      BindingResult errors) {


    if (errors.hasErrors()) {
      logger.warn("There was a error " + errors);
    }
    dataService.create(datum);

    return new ModelAndView("redirect:index");
  }
  
  @RequestMapping(value = "/smart_app", method = RequestMethod.POST, 
		  consumes = "application/json", produces = "application/json")
  @ResponseBody
  public String getFromFhir(@RequestBody Map<String, Object> inputjson,@RequestHeader Map<String,String> headers){
	   
	
	   System.out.println("Input Json");
	   System.out.println(inputjson);
	   String response = "";
	   ObjectMapper oMapper = new ObjectMapper();
	   JSONObject errorObj = new JSONObject();
	   JSONArray appContext ;
	   String basePathOfClass = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
       String[] splitPath = basePathOfClass.split("server/build/classes/java/main/");
       CloseableHttpClient httpClient = HttpClients.createDefault();
	   List<Object> entries =  new ArrayList();
       if(splitPath.length == 1) {
    	  
     	   File filesDirectory = new File(splitPath[0]+"data_files");
     	   try {
			   File appContextFile = ResourceUtils.getFile(filesDirectory+"/"+ (String) inputjson.get("appContext")+".json");
			   InputStream inStream = new FileInputStream(appContextFile);
//			   String jsonContent = IOUtils.toString(inStream, StandardCharsets.UTF_8);
			   response = IOUtils.toString(inStream, StandardCharsets.UTF_8);

			    
			   // 
			   /*
			   appContext = new JSONArray(jsonContent) ;
	       
			   System.out.println("-----APP Contextt --");
			   System.out.println(appContext);
			   if(appContext.length() == 0) {
				   errorObj.put("type", "Exception");
				   errorObj.put("Exception", "Data is missing in appContext");
				   return errorObj.toString();
			   }
			   JSONObject valueInFirstIndex = oMapper.convertValue(appContext.get(0) , JSONObject.class);
			   if(!valueInFirstIndex.has("requirements")) {
				   errorObj.put("type", "Exception");
				   errorObj.put("Exception", "Couldn't find requirements in the given request's appContext");
				   return errorObj.toString();
			   }
			   if(!valueInFirstIndex.has("appData")) {
				   errorObj.put("type", "Exception");
				   errorObj.put("Exception", "Couldn't find appData in the given request's appContext");
				   return errorObj.toString();
			   }
//			   System.out.println(valueInFirstIndex.get("requirements").getClass());
//			   System.out.println(valueInFirstIndex.get("appData").getClass());
//			   System.out.println(valueInFirstIndex.get("requirements"));
			   JSONArray requirementsArray = oMapper.convertValue(valueInFirstIndex.get("requirements") , JSONArray.class);
			   JSONObject resources = new JSONObject();
			   if(requirementsArray.length()>0) {
				   resources = requirementsArray.getJSONObject(0);
			   }
			   JSONObject  appData = oMapper.convertValue(valueInFirstIndex.get("appData").toString() , JSONObject.class);
//			   System.out.println("-----Resssus --");
//			   System.out.println(resources);
//			   System.out.println(appData);
			   
		//	   Map<String, LinkedHashMap> resources = oMapper.convertValue(appContext.get(0) , Map.class);
			   final String authorization = headers.get("authorization");
			   
			   
			   String ResourceField = null;
			   boolean recievedPatientData = false;
			   
			   if(!appData.has("patientId")) {
				   errorObj.put("type", "Exception");
				   errorObj.put("Exception", "Patient ID is missing");
				   return errorObj.toString();
			   }
		       JSONObject patientObj= this.getResourceById("Patient",appData.get("patientId").toString() , authorization);
		       if(patientObj.has("resourceType")) {
		    	   entries.add(patientObj);
		       }
		       Iterator<String> appDataKeys = appData.keys();
		       Iterator<String> resourceKeys = resources.keys();
		       while(appDataKeys.hasNext()) {
		    	   String key = (String)appDataKeys.next();
		           String value = appData.getString(key);
//		           System.out.println("1, Key-Val : "+key+" -- "+ value);
		           if(key != "patientId") {
					   JSONObject entryObject = this.getResourceById(key,value.toString(), authorization);
					   if(entryObject.has("resourceType")) {
						   entries.add(entryObject);
					   }
					   
				   }
		       }

		       while(resourceKeys.hasNext()) {
		    	   String key = (String)resourceKeys.next();
		           JSONObject value =  oMapper.convertValue(resources.get(key) , JSONObject.class);
//		           System.out.println("\n\n2, Key-Val : "+key+" -- "+ value.toString());

			       JSONArray codes = oMapper.convertValue(value.get("codes") , JSONArray.class);
			       JSONObject codeObject =  oMapper.convertValue(codes.get(0), JSONObject.class);
			       String code = oMapper.convertValue(codeObject.get("code") , String.class);
			       String display = oMapper.convertValue(value.get("display") , String.class);
			       File file;
					try {
						file = ResourceUtils.getFile("classpath:config/data.json");
						InputStream in = new FileInputStream(file);
						String jsonTxt = IOUtils.toString(in, StandardCharsets.UTF_8);
						JSONObject configData = new JSONObject(jsonTxt);
						JSONObject fieldMapper = oMapper.convertValue(configData.get("fhir_field_mapper") , JSONObject.class);
						String finalUrl = "";
						if(fieldMapper.has(key)) {
							JSONObject keyObj = oMapper.convertValue(fieldMapper.get(key) , JSONObject.class);
							if((boolean) keyObj.get("has_patient_field")) {
								System.out.println("Has patient field : "+key);
								finalUrl = "http://18.222.7.99:8181/fhir/baseDstu3/"+key+"?"+(String)keyObj.get("code_field")+"="+code+"&patient="+appData.get("patientId");
							    	
							}
							else {

					   			if(patientObj.has((String) keyObj.get("field_common_with_patient"))) {
						   			JSONObject mappingFieldObj = oMapper.convertValue(patientObj.get((String) keyObj.get("field_common_with_patient")) , JSONObject.class);
						   			if(mappingFieldObj.has("reference")) {
							   			String mappingField = (String) mappingFieldObj.get("reference");
							   			System.out.println("mappingField Id");
							   			System.out.println(mappingField);
							   			if(mappingField.contains("/")) {
								   			String fieldId = mappingField.substring(((String)  keyObj.get("string")+"/").length()).trim();
								   			System.out.println("Field Id");
								   			System.out.println(fieldId);
								   			finalUrl = "http://18.222.7.99:8181/fhir/baseDstu3/"+key+"?"+(String)keyObj.get("code_field")+"="+code+"&"+(String) keyObj.get("key")+"="+fieldId;
							   			}
						   			}
					   			}
							}
						}
						else {
						       finalUrl = "http://18.222.7.99:8181/fhir/baseDstu3/"+key+"?patient="+appData.get("patientId")+"&code="+code;
						} 

				        if(finalUrl != "") {
					        HttpGet httpGet = new HttpGet(finalUrl);
					   	    httpGet.addHeader("Authorization",authorization);
					   	   
					   	   
				   		   	StringBuffer fhirresponse = new StringBuffer();
					        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
					   		System.out.println("GET Response Status:: "
					   				+ httpResponse.getStatusLine().getStatusCode());
					
					   		BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
					
					   		String inputLine;
					   		
					   		if(httpResponse.getStatusLine().getStatusCode() == 200) {
					   			while ((inputLine = reader.readLine()) != null) {
						   			fhirresponse.append(inputLine);
						   		}
					   			JSONObject response  = new JSONObject(fhirresponse.toString());
					   			System.out.println("Resssss");
					   			System.out.println(response);
					   			
					   			int total = (int) response.get("total");
					   			System.out.println(total);
					   			if(total != 0) {
					   				JSONArray entry = new JSONArray(response.get("entry").toString());
						   			System.out.println("\n\n\n"+response.get("entry").getClass()+":::::::::::::"+response.get("entry"));
				
						   			entry.forEach((element) -> {
						   				System.out.println("\n\n\n ==========="+element);
						   				JSONObject searchResultJson = oMapper.convertValue(element , JSONObject.class);
							   			if(searchResultJson.has("resource")) {
							   				entries.add(searchResultJson.get("resource"));
							   			}
						   				
						   				
						   			});
					   			}
					   			
					   		}
					   		
			
					   		System.out.println("fhirresponse");
					   		System.out.println(fhirresponse.getClass());
					   		reader.close();
				        }
				   	   
			       
		
				       
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				
			       
			       
			   }
			   try {
					httpClient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			   
			   */
     	   }
     	  catch (FileNotFoundException e) {
				e.printStackTrace();
				JSONObject response_err = new JSONObject();
				response_err.put("Exception", "Invalid appContex : record with given appContext id was not found ");
				return response_err.toString() ;
			} catch (IOException e) {
				e.printStackTrace();
			}
     	  
  	   }
//	   System.out.println("entriii");
//
//			   System.out.println(entries);
	  
	  
	
		// print result
		
		
		return response;
		

	   	 
	   //return entries.toString();
	   
  }
  
  
  
//  @PostMapping("/coverage_determination")
  @RequestMapping(value = "/cds-services/coverage_decision", method = RequestMethod.POST, 
  consumes = "application/json", produces = "application/json")
  @ResponseBody
  public String coverageDecision(@RequestBody Map<String, Object> inputjson,@RequestHeader Map<String,String> headers) {
	    
	  
	  File file;
//	  System.out.println("------");
//      System.out.println(inputjson);
      ObjectMapper oMapper = new ObjectMapper();
      Map<String, Object> context = oMapper.convertValue(inputjson.get("context") , Map.class);
      Map<String, Object> orders = oMapper.convertValue(context.get("orders") , Map.class);

//      System.out.println(context);
      
      JSONObject reqJson = new JSONObject();
      JSONObject patientFhir = new JSONObject();
      try {
    	  
		  JSONObject configData = this.getConfigData();
		  
//		  System.out.println("configData:\n"+ configData.get("hook_cql_map"));
		  String hook  = (String) inputjson.get("hook");
		  JSONObject hookMap = oMapper.convertValue(configData.get("hook_cql_map") , JSONObject.class);
//		  System.out.println(hookMap);
		  List<String> hookList = oMapper.convertValue(hookMap.get(hook) , List.class);
		  
		  
	   List<Object> entryArray = oMapper.convertValue(orders.get("entry") , List.class);
		  if(context.containsKey("patientId")) {
			  String patString = " {\n" + 
				  		"    		  resource:{\n" + 
				  		"    		  resourceType:\"Patient\",\n" + 
				  		"    		  id:"+context.get("patientId").toString()+"\n" + 
				  		"    		  }\n" + 
				  		"    		  }";
			  JSONObject patientResObj = new JSONObject(patString);
			  entryArray.add(patientResObj);
			  
		  }

    	  patientFhir.put("resourceType","Bundle");
    	  patientFhir.put("id",context.get("patientId"));
    	  patientFhir.put("type","collection");
    	  patientFhir.put("entry",entryArray);
    	  reqJson.put("cql",hookList.get(0));
    	  reqJson.put("patientFhir",patientFhir);
//    	  System.out.println("reqqjson -----\n");
//    	  System.out.println(reqJson);
      
      }
      catch(JSONException json_ex) {
    	  System.out.println(json_ex.getStackTrace());
      }
      CloseableHttpClient client = HttpClients.createDefault();
      // Get the token and drop the "Bearer"
  /*//GEtting Token -------------    
      final String authorization = headers.get("authorization");
//      System.out.println("httpheaddd");
//      System.out.println(headers);
//      System.out.println(headers.get("authorization"));
      String username = null;
      String password = null;
      if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
          // Authorization: Basic base64credentials
          String base64Credentials = authorization.substring("Basic".length()).trim();
          byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
          String credentials = new String(credDecoded, StandardCharsets.UTF_8);
          // credentials = username:password
          final String[] auth_values = credentials.split(":", 2);
          if(auth_values.length == 2) {
        	  username = auth_values[0];
        	  password = auth_values[1];
          }
      }

//      System.out.println(password);
//      Object[] req_context = inputjson.get;
      
      String clientId = "app-login";
      HttpPost httpPost = new HttpPost("https://18.222.7.99:8443/auth/realms/ClientFhirServer/protocol/openid-connect/token");
      List<NameValuePair> params = new ArrayList<NameValuePair>();
      params.add(new BasicNameValuePair("client_id", clientId));
      params.add(new BasicNameValuePair("username",username));
      params.add(new BasicNameValuePair("password", password));
      params.add(new BasicNameValuePair("grant_type", "password"));
      try {
        httpPost.setEntity(new UrlEncodedFormEntity(params));
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
//      System.out.println("introspectUrl::");
//      System.out.println(introspectUrl);
      JsonObject tokenResponse;
      
      
      // Map<String,Object> params = new LinkedHashMap<>();
      try {
        CloseableHttpResponse response = client.execute(httpPost);
        String jsonString = EntityUtils.toString(response.getEntity());
        
        tokenResponse = new JsonParser().parse(jsonString).getAsJsonObject();
        client.close();
      }
      catch (IOException e) {
//        System.out.println("\n\n\\n\n\n\\n\n\n\n\nEXceptionnnnnn");
        e.printStackTrace();
        tokenResponse = null;
      }
     System.out.println(tokenResponse);
     
     
     */
      final String authorization = headers.get("authorization");
      String token = null;
      try {
	       if(authorization != null && authorization.startsWith("Bearer"))
	       {
	        token = authorization.substring("Bearer".length()).trim();
	        System.out.println("token....");
	        System.out.println(token);
	          
	       }
//	       else {
//		       
//	    	   throw new RequestIncompleteException("No valid authorization header was found");	       
//	       }
      }
//      catch(RequestIncompleteException req_exception) {
//    	  	JSONObject errorObj = new JSONObject();
//    	  	errorObj.put("exception", req_exception.getMessage());
//	 		return errorObj.toString();
//	 	}
      catch (Exception exception) {
	        System.out.println("388 EXceptionnnnnn");
	        exception.printStackTrace();
	    }
      
      String client_Id = "app-token";
String client_secret = "48bf2c3e-2bd6-4f8d-a5ce-2f94adcb7492";
HttpPost httpPost = new HttpPost("https://auth.mettles.com:8443/auth/realms/ProviderCredentials/protocol/openid-connect/token/introspect");
	    
	    //String client_secret = "c6b0c305-6968-4a3a-875d-4fd0dcc0cd4f";
	   // HttpPost httpPost = new HttpPost("https://valtest.mettles.com:8443/auth/realms/ClientFhirServer/protocol/openid-connect/token/introspect");
	    List<NameValuePair> params = new ArrayList<NameValuePair>();
      params.add(new BasicNameValuePair("client_id", client_Id));
      params.add(new BasicNameValuePair("client_secret", client_secret));
      params.add(new BasicNameValuePair("token", token));
      try {
        httpPost.setEntity(new UrlEncodedFormEntity(params));
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      
      JsonObject tokenResponse = null;
      if(authorization != null) {
	      try {
	        CloseableHttpResponse response = client.execute(httpPost);
	        String jsonStr = EntityUtils.toString(response.getEntity());
	        tokenResponse = new JsonParser().parse(jsonStr).getAsJsonObject();      
	        client.close();
	      }
	      catch (IOException e) {
	        System.out.println("412\n\n\\n\n\n\\n\n\n\n\nEXceptionnnnnn");
	        e.printStackTrace();
	        tokenResponse = null;
	      }
  	  }
      StringBuilder sb = new StringBuilder();
      JSONObject responseObj = new JSONObject();
      try {
      	if (authorization == null || ((tokenResponse != null) && (tokenResponse.get("active").getAsBoolean()))) {
      	
      		
	        // execute method and handle any error responses.
	    	URL url = new URL("http://localhost:4200/execute_cql");
	        Gson gsonObj = new Gson();
	        reqJson.put("request_for", "decision");
	        String jsonStr = reqJson.toString();
	        System.out.println(jsonStr);
	        byte[] postDataBytes = jsonStr.getBytes("UTF-8");
	
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/json");
	        conn.setRequestProperty("Accept","application/json");
	        if(authorization!= null) {
	        	conn.setRequestProperty("Authorization",authorization);
	          
	        }
//	        else {
//	        	throw new RequestIncompleteException("Unable to call CDS . token doesn't exist");
//	        }
	        conn.setDoOutput(true);
	        conn.getOutputStream().write(postDataBytes);
	        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	        String line =null;
	        while((line=in.readLine())!= null){
	          sb.append(line);
	        }
	        JSONObject jsonObj = new JSONObject(sb.toString());
	        
	        if(jsonObj.get("Coverage") != null) {
	        	responseObj.put("Coverage", jsonObj.get("Coverage"));
	        }
	        
		 }
//      	else {
//      		
//        	throw new RequestIncompleteException("Invalid Oauth Token");
//        }
       }
//	 catch(RequestIncompleteException req_exception) {
//		 	JSONObject errorObj = new JSONObject();
// 	  		errorObj.put("exception", req_exception.getMessage());
//	 		return errorObj.toString();
//	 	}
	 catch (Exception exception) {
	        System.out.println("\n\n\\n\n\n\\n\n\n\n\nEXceptionnnnnn");
	        exception.printStackTrace();
	    }
	 String result = responseObj.toString();
	 return result;
  }
  
  
  protected String getSaltString() {
      String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
      StringBuilder salt = new StringBuilder();
      Random rnd = new Random();
      while (salt.length() < 12) { // length of the random string.
          int index = (int) (rnd.nextFloat() * SALTCHARS.length());
          salt.append(SALTCHARS.charAt(index));
      }
      String saltStr = salt.toString();
      return saltStr;

  }
  
  @RequestMapping(value = "/cds-services/coverage_requirement", method = RequestMethod.POST, 
		  consumes = "application/json", produces = "application/json")
  @ResponseBody
  public String coverageRequirement(@RequestBody Map<String, Object> inputjson,@RequestHeader Map<String,String> headers) {
	  
	  System.out.println("------");
      System.out.println(inputjson);
      System.out.println(headers);
      String referer = "";
      if(headers.containsKey("referer")) {
    	  referer = headers.get("referer");
      }
      ObjectMapper oMapper = new ObjectMapper();
      File file;
      JSONObject appData = new JSONObject();
      Map<String, Object> context = oMapper.convertValue(inputjson.get("context") , Map.class);
      Map<String, Object> orders = oMapper.convertValue(context.get("orders") , Map.class);
//		      System.out.println(context);
      List<Object> entryArray = oMapper.convertValue(orders.get("entry") , List.class);
      
      if(context.containsKey("patientId")) {
    	  appData.put("patientId",  context.get("patientId").toString());
      }
      if(context.containsKey("userId")) {
    	  appData.put("Practitioner", context.get("userId").toString());
      } 
      if(context.containsKey("encounterId")) {
    	  appData.put("Encounter", context.get("encounterId").toString());
      }
      if(context.containsKey("converageId")) {
    	  appData.put("Coverage", context.get("converageId").toString());
      }
      JSONObject newAppContext = new JSONObject();
//      entryArray.forEach()
//      try {
//		  
//      }
//      catch(Exception Ex) {
//    	  Ex.printStackTrace();
//      }
      String hook = "";
      
      if(inputjson.containsKey("hook")) {
    	  hook  = (String) inputjson.get("hook");
      }
      else {
    	  JSONObject errorObj = new JSONObject();
	  	  errorObj.put("exception", "hook is missing in the request body");
	 	  return errorObj.toString();
      }
     
      
      
      
      JSONObject reqJson = new JSONObject();
      JSONObject patientFhir = new JSONObject();
//      System.out.println("673");
      String cql_name = "";
      final String authorization = headers.get("authorization");
      try {
    	  file = ResourceUtils.getFile("classpath:config/data.json");
		  InputStream in = new FileInputStream(file);
		  String jsonTxt = IOUtils.toString(in, StandardCharsets.UTF_8);
		  JSONObject configData = new JSONObject(jsonTxt);
		  
		  
		  JSONObject hookMap = oMapper.convertValue(configData.get("hook_cql_map") , JSONObject.class);
		  JSONObject hcpcTemplateMap  = oMapper.convertValue(configData.get("hcpc_template_mapper") , JSONObject.class);
		  JSONObject cqlReqJson = new JSONObject();
		  cqlReqJson.put("hook",hook);
		  
	      for (int entryIndex=0; entryIndex < entryArray.size(); entryIndex++) {
//	    	  System.out.println("Entry Item:");
	
	    	  Map<String, Object> entryResourceObj = oMapper.convertValue(entryArray.get(entryIndex) , Map.class);
	    	  if(entryResourceObj.containsKey("resource")) {
		    	  JSONObject entryResource =new JSONObject(oMapper.writeValueAsString(entryResourceObj.get("resource")));
//		    	  System.out.println(entryResource.get("resourceType"));
		    	  
		    	  if(((String) entryResource.get("resourceType")).equals("DeviceRequest")) {
		    		  newAppContext.put("request",entryResource);
		    		  cqlReqJson.put("deviceRequest",entryResource);
		    		/*
		    		  if(entryResource.has("codeCodeableConcept")) {
//		    			  System.out.println("entryResource.has");
//		    			  System.out.println(entryResource.get("codeCodeableConcept"));
		    			  JSONObject codeConcept = new JSONObject(entryResource.get("codeCodeableConcept").toString());
		    			  
		    			  if(codeConcept.has("coding")) {
		    				  JSONArray codeArray = new JSONArray(codeConcept.get("coding").toString());
		    				  for(int codeIndex=0; codeIndex < codeArray.length(); codeIndex++) {
//		    					  System.out.println(codeArray.get(codeIndex));
		    					  JSONObject codeItem = new JSONObject(codeArray.get(codeIndex).toString());
		    					  if(codeItem.has("code")) {
		    						  String hcpcCode = (String) codeItem.get("code");
//		    						  System.out.println("hcpcCode: "+ hcpcCode);
//		    						  System.out.println( hcpcTemplateMap.has(hcpcCode));
								 // newAppContext.put("template","urn:hl7:davinci:crd:"+(String) hcpcTemplateMap.get(hcpcCode));
                                                                 // newAppContext.put("prior_auth",true);
	
		    						  if(hcpcTemplateMap.has(hcpcCode)) {
		    							  newAppContext.put("template","urn:hl7:davinci:crd:"+(String) hcpcTemplateMap.get(hcpcCode));
		    						//	  newAppContext.put("prior_auth",true);
		    							  if(hcpcCode.equals("A0425")) {
		    				    			  newAppContext.put("prior_auth",false);
		    				    		  }
		    				    		  else {
		    				    			  newAppContext.put("prior_auth",true);
		    				    		  }
		    							  
		    						  }
		    						  else {
	    				    			  newAppContext.put("prior_auth",true);
	    				    		  }
		    					//	  else {
		    					//		  newAppContext.put("prior_auth",false);
		    					//	  }
    						  
		    					  }
		    					  else {
    				    			  newAppContext.put("prior_auth",true);
    				    		  }
		    				  }
		    				  if(codeArray.length() == 0) {
				    			  newAppContext.put("prior_auth",true);
				    		  }
		    			  }
		    			  else {
			    			  newAppContext.put("prior_auth",true);
			    		  }
		    		  }
		    		  else {
		    			  newAppContext.put("prior_auth",true);
		    		  } */
		    	  }
		    	  if(((String) entryResource.get("resourceType")).equals("Patient")) {
				//    		  oMapper.convertValue(entryResourceObj.get("resource") , Map.class);
				      newAppContext.put("patient",(String) entryResource.get("id"));
				   }
	    	  }
		    	 
	    	  
	//    	  }
	    	  
	      }
	      
	       URL cqlDataUrl = new URL("http://localhost:4200/getCqlData");
	        String cqlJsonStr = cqlReqJson.toString();
	        System.out.println(cqlJsonStr);
	        byte[] cqlReqDataBytes = cqlJsonStr.getBytes("UTF-8");
	        HttpURLConnection cqlDataconn = (HttpURLConnection)cqlDataUrl.openConnection();
	        cqlDataconn.setRequestMethod("POST");
	        cqlDataconn.setRequestProperty("Content-Type", "application/json");
	        cqlDataconn.setRequestProperty("Accept","application/json");
	        if(authorization!= null) {
	    	   cqlDataconn.setRequestProperty("Authorization",authorization);
		      }  
	        cqlDataconn.setDoOutput(true);
	        cqlDataconn.getOutputStream().write(cqlReqDataBytes);
	        BufferedReader cqlResReader = new BufferedReader(new InputStreamReader(cqlDataconn.getInputStream(), "UTF-8"));
	        String line =null;
	        StringBuilder cqlResStrBuilder = new StringBuilder();
	        while((line=cqlResReader.readLine())!= null){
	        	cqlResStrBuilder.append(line);
	        }
		   System.out.println("cqlll Reststs Str");
		   System.out.println(cqlResStrBuilder);
	       JSONObject cqlResObj = new JSONObject(cqlResStrBuilder.toString());
	       newAppContext.put("prior_auth",cqlResObj.get("prior_auth"));
	       newAppContext.put("template","urn:hl7:davinci:crd:"+cqlResObj.get("template"));
//		  List<String> hookList = oMapper.convertValue(hookMap.get(hook) , List.class);
		 
		  if(context.containsKey("patientId")) {
			  String patString = " {\n" + 
				  		"    		  resource:{\n" + 
				  		"    		  resourceType:\"Patient\",\n" + 
				  		"    		  id:"+context.get("patientId").toString()+"\n" + 
				  		"    		  }\n" + 
				  		"    		  }";
			  JSONObject patientResObj = new JSONObject(patString);
			  entryArray.add(patientResObj);
			  
		  }
    	  patientFhir.put("resourceType","Bundle");
    	  patientFhir.put("id",context.get("patientId"));
    	  patientFhir.put("type","collection");
    	  patientFhir.put("entry",entryArray);
//    	  cql_name = hookList.get(0);
//    	  reqJson.put("cql",hookList.get(0));
//    	  System.out.println("CQL name");
//    	  System.out.println(hookList.get(0));
    	  reqJson.put("patientFhir",patientFhir);
    	  System.out.println("reqquirejson -----\n");
    	  System.out.println(reqJson);
      
      }
      catch(JSONException json_ex) {
    	 json_ex.printStackTrace();
    	 System.out.println("710");
      } 
      catch (FileNotFoundException e) {
		e.printStackTrace();
		System.out.println("714");
	  } 
      catch (IOException e) {
		e.printStackTrace();
		System.out.println("718");
	  }
      CloseableHttpClient client = HttpClients.createDefault();
      // Get the token and drop the "Bearer"
      
      String token = null;
      try {
	       if(authorization != null && authorization.startsWith("Bearer"))
	       {
	        token = authorization.substring("Bearer".length()).trim();
	        System.out.println("token....");
	        System.out.println(token);
	          
	       }
//	       else {
//		       
//	    	   throw new RequestIncompleteException("No valid authorization header was found");	       
//	       }
      }
//      catch(RequestIncompleteException req_exception) {
//    	  	JSONObject errorObj = new JSONObject();
//    	  	errorObj.put("exception", req_exception.getMessage());
//	 		return errorObj.toString();
//	 	}
      catch (Exception exception) {
	        System.out.println("743 EXceptionnnnnn");
	        exception.printStackTrace();
	    }
      String client_Id = "app-token";
String client_secret = "48bf2c3e-2bd6-4f8d-a5ce-2f94adcb7492";
HttpPost httpPost = new HttpPost("https://auth.mettles.com:8443/auth/realms/ProviderCredentials/protocol/openid-connect/token/introspect");
	    
	    //String client_secret = "c6b0c305-6968-4a3a-875d-4fd0dcc0cd4f";
	    //HttpPost httpPost = new HttpPost("https://valtest.mettles.com:8443/auth/realms/ClientFhirServer/protocol/openid-connect/token/introspect");
	    List<NameValuePair> params = new ArrayList<NameValuePair>();
      params.add(new BasicNameValuePair("client_id", client_Id));
      params.add(new BasicNameValuePair("client_secret", client_secret));
      params.add(new BasicNameValuePair("token", token));
      System.out.println("754");
      try {
        httpPost.setEntity(new UrlEncodedFormEntity(params));
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      
      JsonObject tokenResponse = null;
      System.out.println("------Authhor------");
      System.out.println(authorization);
      if(authorization != null) {
	      try {
	    	  System.out.println("------Before call------");
	        CloseableHttpResponse response = client.execute(httpPost);
	    	  System.out.println("------After call------");
	        String jsonStr = EntityUtils.toString(response.getEntity());
	        System.out.println("------Get jsosoosn responsee------");
	        System.out.println(jsonStr);
	        tokenResponse = new JsonParser().parse(jsonStr).getAsJsonObject();      
	        client.close();
	      }
	      catch (IOException e) {
	        System.out.println("412\n\n\\n\n\n\\n\n\n\n\nEXceptionnnnnn");
	        e.printStackTrace();
	        tokenResponse = null;
	      }
      }
      
    
      StringBuilder sb = new StringBuilder();
      JSONObject response = new JSONObject();
      try{
	 
    	if (authorization == null || ((tokenResponse != null) && (tokenResponse.get("active").getAsBoolean()))) {    	      	
	        // execute method and handle any error responses.
    		
//	    	URL url = new URL("http://localhost:4200/execute_cql");
//	        Gson gsonObj = new Gson();
//	        reqJson.put("request_for", "requirements");
//	        String jsonStr = reqJson.toString();
//	        System.out.println(jsonStr);
//	        byte[] postDataBytes = jsonStr.getBytes("UTF-8");
//	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
//	        conn.setRequestMethod("POST");
//	        conn.setRequestProperty("Content-Type", "application/json");
//	        conn.setRequestProperty("Accept","application/json");
//	       if(authorization!= null) {
//		 conn.setRequestProperty("Authorization",authorization);
//	      }  conn.setDoOutput(true);
//	        conn.getOutputStream().write(postDataBytes);
//	        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
//	        String line =null;
//	        while((line=in.readLine())!= null){
//	          sb.append(line);
//	        }
    		if(hook.equals("order-select")) {
    	    	  String summaryString = "{ \"summary\": \"High risk for opioid overdose - taper now\", \"indicator\": \"warning\", \"links\": [ { \"label\": \"CDC guideline for prescribing opioids for chronic pain\", \"type\": \"absolute\", \"url\": \"https://guidelines.gov/summaries/summary/50153/cdc-guideline-for-prescribing-opioids-for-chronic-pain---united-states-2016#420\" }, { \"label\": \"MME Conversion Tables\", \"type\": \"absolute\", \"url\": \"https://www.cdc.gov/drugoverdose/pdf/calculating_total_daily_dose-a.pdf\" } ], \"detail\": \"Total morphine milligram equivalent (MME) is 125mg. Taper to less than 50.\" }";
    	          JSONObject summaryJson =new JSONObject(summaryString);
//    	          System.out.println("Summary JSON");
//    	          System.out.println(summaryJson);
    	          JSONArray resCards = new JSONArray();
    	          resCards.put(summaryJson);
    	          JSONObject orderSelectResponse = new JSONObject();
    		      orderSelectResponse.put("cards",resCards);
    		      return orderSelectResponse.toString();
    	          
    	          
    	      }
    	     
    		
	        String basePathOfClass = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
	        System.out.println("---crd path---");
	       
	        System.out.println(sb.toString());	        
//	        JSONObject jsonObj = new JSONObject(sb.toString());
			JSONObject jsonObj = new JSONObject();
	        jsonObj.put("appData", appData);
	        jsonObj.put("hook", hook);
	        if(inputjson.containsKey("payerName")) {
	        	jsonObj.put("payerName", (String) inputjson.get("payerName"));
	        }
	        JSONArray appContext = new JSONArray();
	        appContext.put(jsonObj);
	        String[] splitPath = basePathOfClass.split("server/build/classes/java/main/");
	        System.out.println(splitPath.length > 1);
	        System.out.println(splitPath.length + "  " + basePathOfClass.split("server/build/classes/").length );
	        System.out.println(jsonObj);
	        boolean fileCreated = false;
	        String filename = "";
	        if(splitPath.length == 1) {
	      	  File filesDirectory = new File(splitPath[0]+"data_files");
	      	  System.out.println(filesDirectory.exists());
	      	  System.out.println(filesDirectory.isDirectory());
	      	  if (filesDirectory.exists() && filesDirectory.isDirectory()) {
	      		  File newFile = new File(filesDirectory,this.getSaltString()+".json");
	      		  while(newFile.exists()) {
	      			  newFile = new File(filesDirectory,this.getSaltString()+".json");
	      		  }
	      		  
	      		  if (newFile.createNewFile())
  				  {
	      			  filename = newFile.getName();
  				      System.out.println("File is created!");
  				      PrintWriter writer = new PrintWriter(newFile, "UTF-8");
  				      
	  				  writer.print(appContext.toString());
	  				  writer.close();
	  				  fileCreated = true;
  				      
  				  } else {
  				      System.out.println("File already exists.");
  				  }
	      	  }
	        }
	//        System.out.println("");
	//        System.out.println(sb);
	        
	        JSONObject singleCard = new JSONObject();
	        
	        ArrayList<JSONObject> suggestions = new ArrayList<JSONObject>();
	        ArrayList<JSONObject> links = new ArrayList<JSONObject>();
	        ArrayList<JSONObject> cards = new ArrayList<JSONObject>();
	        JSONObject applink = new JSONObject();
	        applink.put("label","SMART App");
	        String appLinkURL = referer.replace("provider_request", "");
	        System.out.println("|| appLinkURL : "+appLinkURL);
	        if(!inputjson.containsKey("fhirServer")) {
	        	 throw new RequestIncompleteException("Parameter Missing : key 'fhirServer' is missing in given request");	    
	        }
//	        System.out.println(cql_name);
//	        if(!cql_name.equals("")) {
//	        	
//	        }
//	        applink.put("url",appLinkURL+"launch?launch="+filename.replace(".json", "")+"&iss="+inputjson.get("fhirServer").toString());
	        applink.put("url","http://cdex.mettles.com:3005/launch");

	        applink.put("type","smart");
	        
	        JSONObject file_links = new JSONObject();
	        
	        if(inputjson.containsKey("service_code")) {
	        	jsonObj.put("service_code", (String) inputjson.get("service_code"));
	        }
//	        file_links.put("cql_json","/fetchFhirUri/urn%3Ahl7%3Adavinci%3Acrd%3A"+cql_name+"_requirements.json");
//	        file_links.put("cql_file","/fetchFhirUri/urn%3Ahl7%3Adavinci%3Acrd%3A"+cql_name+"_requirements.cql");
//	        file_links.put("questionnaire_file","fetchFhirUri/urn%3Ahl7%3Adavinci%3Acrd%3Ahome-oxygen-questionnaire");
//	        jsonObj.put("file_links",file_links);
//	        "template=urn:hl7:davinci:crd:home-oxygen-questionnaire_2"
//	        + "&request={\"resourceType\":\"DeviceRequest\",\"id\":\"devreq013\",\"meta\":{\"profile\":[\"http:\/\/hl7.org\/fhir\/us\/davinci-crd\/STU3\/StructureDefinition\/profile-devicerequest-stu3\"]},\"extension\":[{\"url\":\"http:\/\/build.fhir.org\/ig\/HL7\/davinci-crd\/STU3\/ext-insurance.html\",\"valueReference\":{\"reference\":\"Coverage\/0f58e588-eecd-4ab3-9316-f3d02a3ba39d\"}}],\"status\":\"draft\",\"codeCodeableConcept\":{\"coding\":[{\"system\":\"https:\/\/bluebutton.cms.gov\/resources\/codesystem\/hcpcs\",\"code\":\"E0424\",\"display\":\"Stationary Compressed Gaseous Oxygen System, Rental\"}]},\"subject\":{\"reference\":\"Patient\/f31500e8-15cb-4e8e-8c6e-a001edc6604e\"},\"performer\":{\"reference\":\"PractitionerRole\/f0b0cf14-4066-403f-b217-e92e73c350eb\"}}"
//	        + "&filepath=../../getfile/cms/hcpcs/E0424";
//	        newAppContext.put("template","urn:hl7:davinci:crd:home-oxygen-questionnaire_2");
	        
	        applink.put("appContext",newAppContext);
	        //	        applink.put("appContext",jsonObj.get("requirements"));
////	        applink.put("appContext", filename.replace(".json", ""));
	        links.add(applink);
	        JSONObject sourceJson = new JSONObject();
	        sourceJson.put("label","CMS Medicare coverage database");
	        sourceJson.put("url","https://www.cms.gov/medicare-coverage-database/details/ncd-details.aspx?NCDId=70&ncdver=3&bc=AAAAgAAAAAAA&\n");
	        singleCard.put("links", links);
	        singleCard.put("source", sourceJson);
	        singleCard.put("suggestions", suggestions);
//	        singleCard.put("summary","Requirements for "+HomeController.toTitleCase(hook.replace("-", " ")));
		singleCard.put("summary","Requirements");
	        singleCard.put("indicator","info");
	        singleCard.put("detail","The requested procedure needs more documentation to process further");
	        cards.add(singleCard);
	        response.put("cards",cards);
	//        System.out.println("cards------\n\n\n");
	//        System.out.println(cards);
    	   }
    	else {
           throw new RequestIncompleteException("Invalid Oauth Token");
    	}
	  }
	 catch(RequestIncompleteException req_exception) {
		  JSONObject errorObj = new JSONObject();
	  	  errorObj.put("exception", req_exception.getMessage());
	 	  return errorObj.toString();
	 	}
     catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 catch (Exception exception) {
	        System.out.println("\n\n\\n\n\n\\n\n\n\n\nEXceptionnnnnn");
	        exception.printStackTrace();
	    }
    
	 String result = response.toString();
	 return result;

  }
  

  public static String toTitleCase(String input) {
	    StringBuilder titleCase = new StringBuilder();
	    boolean nextTitleCase = true;

	    for (char c : input.toCharArray()) {
	        if (Character.isSpaceChar(c)) {
	            nextTitleCase = true;
	        } else if (nextTitleCase) {
	            c = Character.toTitleCase(c);
	            nextTitleCase = false;
	        }

	        titleCase.append(c);
	    }

	    return titleCase.toString();
	}

	//@PostMapping("/coverage_determination")
  @RequestMapping(value = "/cds-services/patient_view", method = RequestMethod.POST, 
	consumes = "application/json", produces = "application/json")
  @ResponseBody
  public String patient_view(@RequestBody Map<String, Object> inputjson,@RequestHeader Map<String,String> headers) {
	  ObjectMapper oMapper = new ObjectMapper();
	  String authorization  = "";
	  Map<String, Object>  fhirAuth ;
	  try {
		  if(!inputjson.containsKey("fhirAuthorization")) {
			  throw new RequestIncompleteException("FHIR Authorization details are missing !!");
		  }
		  fhirAuth = oMapper.convertValue(inputjson.get("fhirAuthorization") , Map.class);

		  if(!fhirAuth.containsKey("access_token")) {
//			 throw new RequestIncompleteException("FHIR access token is missing !!");
			  
		  }
		  else {
			  authorization = "Bearer "+fhirAuth.get("access_token");
		  }
		  
	   }
//	  catch(RequestIncompleteException req_exception) {
//		  JSONObject errorObj = new JSONObject();
//	  	  errorObj.put("exception", req_exception.getMessage());
//	 	  return errorObj.toString();
//	 	}

	  catch (Exception exception) {
	        System.out.println("\n\n\\n\n\n\\n\n\n\n\nEXceptionnnnnn");
	        exception.printStackTrace();
	    }
	  JSONObject tokenResponse = null;
	  if(!authorization.equals("")) {
		  tokenResponse = this.verifiyToken(authorization);
	  }
	  
	  System.out.println(tokenResponse);
	  try {
		  if(!inputjson.containsKey("fhirServer")) {
	     	 throw new RequestIncompleteException("Parameter Missing : key 'fhirServer' is missing in given request");	    
		  }
	  }
	  catch(RequestIncompleteException req_exception) {
  	  	JSONObject errorObj = new JSONObject();
  	  	errorObj.put("exception", req_exception.getMessage());
	 		return errorObj.toString();
	 	}
	  if(tokenResponse.get("type") == "exception") {
		  return tokenResponse.toString();
	  }
	  JSONObject response = new JSONObject();
	  if(!authorization.equals("") || (tokenResponse != null && (boolean)tokenResponse.get("active"))) {
		  ArrayList<JSONObject> cards = new ArrayList<JSONObject>();
		  JSONObject singleCard = new JSONObject();
		  JSONObject resource=new JSONObject();
		  boolean getPatient = false;
		  
		  System.out.println("Prefetch0:"+inputjson.get("prefetch")!=null);
		  if(inputjson.containsKey("prefetch")) {
			  String prefetchJson = "";
			  try {
				 prefetchJson = oMapper.writeValueAsString(inputjson.get("prefetch"));
			  } catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			  }
			  JSONObject prefetch = new JSONObject(prefetchJson);
			  System.out.println("Prefetch1:"+inputjson.get("prefetch").getClass());
			  if(prefetch.has("patient")) {
				  resource = oMapper.convertValue(prefetch.get("patient") , JSONObject.class);
			  }else {
				  getPatient = true;	
				  
			  }
		  }
		  else {
			  getPatient = true;	  
		  }
		  if(getPatient) {
			  System.out.println("inputjson.containsKey(");
			  System.out.println(inputjson.containsKey("patientId"));
			  if(inputjson.containsKey("patientId")) {
				  resource= this.getResourceById("Patient",(String) inputjson.get("patientId"),authorization,inputjson.get("fhirServer").toString());
			  }
			  
		  }
		  if(resource != null) {
			if(resource.has("name")) {
				JSONArray name = oMapper.convertValue(resource.get("name") , JSONArray.class);
				if(name.length() > 0) {
					JSONObject nameObj = oMapper.convertValue(name.get(0) , JSONObject.class);
					if(nameObj.has("given")) {
						System.out.println("788 "+nameObj.get("given").getClass());
						List<String> nameList = oMapper.convertValue(nameObj.get("given") , List.class);
						if(nameList.size() > 0) {
							singleCard.put("summary", "Now seeing: "+nameList.get(0));
							JSONObject sourceKey = new JSONObject();
							
							sourceKey.put("label", (String) this.getConfigData().get("patient_view_service_label"));
							singleCard.put("indicator", "info");
						    singleCard.put("source", sourceKey);
						}
						
					  	cards.add(singleCard);
					}
					
				}
			  	
			}
		  }
		  response.put("cards", cards);
		  return response.toString();
	  }
	  else {
		  response.put("Exception", "Invalid OAuth Token");
		  return response.toString();
	  }
	  
  }
  
  public JSONObject getConfigData() {
	    File file;
	    JSONObject configData = new JSONObject();
		try {
			file = ResourceUtils.getFile("classpath:config/data.json");
			InputStream in = new FileInputStream(file);
			String jsonTxt = IOUtils.toString(in, StandardCharsets.UTF_8);
			configData = new JSONObject(jsonTxt);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return configData;
		
  }
  
  public JSONObject verifiyToken(String authorization) {
	  JSONObject tokenResponse = new JSONObject();
	  CloseableHttpClient client = HttpClients.createDefault();
      // Get the token and drop the "Bearer"
	    String token = null;
	    try {
	       if(authorization != null && authorization.startsWith("Bearer"))
	       {
	        token = authorization.substring("Bearer".length()).trim();
	        System.out.println("token....");
	        System.out.println(token);
	          
	       }
	       else {
		       
	    	   throw new RequestIncompleteException("No valid authorization header was found");	       
	       }
	    }
	    catch(RequestIncompleteException req_exception) {
		  	JSONObject errorObj = new JSONObject();
		  	errorObj.put("exception", req_exception.getMessage());
		  	errorObj.put("type", "exception");
	 		return errorObj;
	 	}
	    catch (Exception exception) {
	        System.out.println("388 EXceptionnnnnn");
	        exception.printStackTrace();
	    }
	      
	    String client_Id = "app-token";
String client_secret = "48bf2c3e-2bd6-4f8d-a5ce-2f94adcb7492";
HttpPost httpPost = new HttpPost("https://auth.mettles.com:8443/auth/realms/ProviderCredentials/protocol/openid-connect/token/introspect");
	    
	    //String client_secret = "c6b0c305-6968-4a3a-875d-4fd0dcc0cd4f";
	    //HttpPost httpPost = new HttpPost("https://valtest.mettles.com:8443/auth/realms/ClientFhirServer/protocol/openid-connect/token/introspect");
	    List<NameValuePair> params = new ArrayList<NameValuePair>();
	    params.add(new BasicNameValuePair("client_id", client_Id));
	    params.add(new BasicNameValuePair("client_secret", client_secret));
	    params.add(new BasicNameValuePair("token", token));
	    try {
	    	httpPost.setEntity(new UrlEncodedFormEntity(params));
	    } catch (UnsupportedEncodingException e) {
	    	e.printStackTrace();
	    }
	      
	    try {
	    	CloseableHttpResponse tokenResponceObj = client.execute(httpPost);
	        String jsonStr = EntityUtils.toString(tokenResponceObj.getEntity());
	        tokenResponse = new JSONObject(jsonStr);      
	        client.close();
	      }
	    catch (IOException e) {
	        System.out.println("412\n\n\\n\n\n\\n\n\n\n\nEXceptionnnnnn");
	        e.printStackTrace();
	        tokenResponse = null;
	     }
//      System.out.println("Tokrkekek");
//      System.out.println(tokenResponse);
      tokenResponse.put("type", "token");
	  return tokenResponse;
	  
  }
		    
  public JSONObject getResourceById(String resourceName,String resourceId,String authorization,String server_url) {
	  JSONObject response = new JSONObject();
	  StringBuilder sb = new StringBuilder();
	  String urlString = server_url+resourceName+"/"+resourceId;
      
//      String urlString = "http://hapi.fhir.org/baseDstu3/"+key+"?patient="+inputjson.get("patientId")+"&code="+code;
      
	  System.out.println(urlString);
	  HttpGet httpGet = new HttpGet(urlString);
	//			   http://hapi.fhir.org/baseDstu3/Condition?patient=415133
	  System.out.println(authorization);
	  httpGet.addHeader("Authorization",authorization);
  	   
	  StringBuffer fhirresponse = new StringBuffer();
	  CloseableHttpClient httpClient = HttpClients.createDefault();
	  CloseableHttpResponse httpResponse;
	  try {
			httpResponse = httpClient.execute(httpGet);
			System.out.println("GET Response Status:: "
					+ httpResponse.getStatusLine().getStatusCode());
	
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
	
			String inputLine;
			
			if(httpResponse.getStatusLine().getStatusCode() == 200) {
				while ((inputLine = reader.readLine()) != null) {
					fhirresponse.append(inputLine);
				}
				response  = new JSONObject(fhirresponse.toString());
			}
			else if(httpResponse.getStatusLine().getStatusCode() == 403) {
				response.put("type", "Exception");
				response.put("exception", "Access Denied");
			}
			reader.close();
	  } catch (IOException e) {
			// TODO Auto-generated catch block
		e.printStackTrace();
	  }
		
	  return response;
  }
  
  
  @RequestMapping(value = "/prefetch", method = RequestMethod.POST, 
		  consumes = "application/json", produces = "application/json")
  @ResponseBody
  public String prefetch(@RequestBody Map<String, String> inputjson,@RequestHeader Map<String,String> headers) {

   // JSONObject obj = new JSONObject();
	  	StringBuilder sb = new StringBuilder();
	    File file;
	    JSONObject response = new JSONObject();
	    
	    
	    CloseableHttpClient client = HttpClients.createDefault();
	      // Get the token and drop the "Bearer"
	    final String authorization = headers.get("authorization");
	    String token = null;
	    try {
	       if(authorization != null && authorization.startsWith("Bearer"))
	       {
	        token = authorization.substring("Bearer".length()).trim();
	        System.out.println("token....");
	        System.out.println(token);
	        
	       }
	       else {
		       
	    	   throw new RequestIncompleteException("No valid authorization header was found");	       
	       }
	    }
	    catch(RequestIncompleteException req_exception) {
    	  	JSONObject errorObj = new JSONObject();
    	  	errorObj.put("exception", req_exception.getMessage());
	 		return errorObj.toString();
	 	}
	    catch (Exception exception) {
	        System.out.println("388 EXceptionnnnnn");
	        exception.printStackTrace();
	    }
	      
	    String client_Id = "app-token";
	    String client_secret = "48bf2c3e-2bd6-4f8d-a5ce-2f94adcb7492";
	    HttpPost httpPost = new HttpPost("https://auth.mettles.com:8443/auth/realms/ProviderCredentials/protocol/openid-connect/token/introspect");
	    List<NameValuePair> params = new ArrayList<NameValuePair>();
	    params.add(new BasicNameValuePair("client_id", client_Id));
	    params.add(new BasicNameValuePair("client_secret", client_secret));
	    params.add(new BasicNameValuePair("token", token));
	    try {
	    	httpPost.setEntity(new UrlEncodedFormEntity(params));
	    } catch (UnsupportedEncodingException e) {
	    	e.printStackTrace();
	    }
	      
	    JsonObject tokenResponse;
	    try {
	    	CloseableHttpResponse tokenResponceObj = client.execute(httpPost);
	        String jsonStr = EntityUtils.toString(tokenResponceObj.getEntity());
	        tokenResponse = new JsonParser().parse(jsonStr).getAsJsonObject();      
	        client.close();
	      }
	    catch (IOException e) {
	        System.out.println("412\n\n\\n\n\n\\n\n\n\n\nEXceptionnnnnn");
	        e.printStackTrace();
	        tokenResponse = null;
	     }
	    int resourcesLength  = inputjson.size();
	    JSONArray listResponse =  new JSONArray() ;
		try {
			if ((tokenResponse != null) && (tokenResponse.get("active").getAsBoolean())) {
				System.out.println(inputjson);
				
				inputjson.forEach((resource,id)->{
					JSONObject resourceObj = new JSONObject();
					String urlString = "http://54.227.218.17:8181/fhir/baseDstu3/"+resource+"/"+id;
				       
//				       String urlString = "http://hapi.fhir.org/baseDstu3/"+key+"?patient="+inputjson.get("patientId")+"&code="+code;
					CloseableHttpClient httpClient = HttpClients.createDefault();

				    
				    HttpGet httpGet = new HttpGet(urlString);
				//			   http://hapi.fhir.org/baseDstu3/Condition?patient=415133
				    System.out.println(authorization);
				   	httpGet.addHeader("Authorization",authorization);
				   	   
				   	   
		   		   	StringBuffer fhirresponse = new StringBuffer();
			        CloseableHttpResponse httpResponse;
					try {
						httpResponse = httpClient.execute(httpGet);
						BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
						
				   		String inputLine;
				   		
				   		if(httpResponse.getStatusLine().getStatusCode() == 200) {
				   			while ((inputLine = reader.readLine()) != null) {
					   			fhirresponse.append(inputLine);
					   		}
				   			resourceObj  = new JSONObject(fhirresponse.toString());
				   			listResponse.put(resourceObj);
				   			response.put(resource,resourceObj);
				   			
		//		   			entries.addAll(entry);
				   		}else if(httpResponse.getStatusLine().getStatusCode() == 403) {
				   			System.out.println("Access Denied");
				   			throw new RequestIncompleteException("403 : Access Denied");
				   			
				   		}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (RequestIncompleteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			   		
			
				});
				
				
			}
			else {
		       throw new RequestIncompleteException("Invalid Oauth Token");
		    }
			
		   
		    
		}
		catch(RequestIncompleteException req_exception) {
		  JSONObject errorObj = new JSONObject();
	  	  errorObj.put("exception", req_exception.getMessage());
	 	  return errorObj.toString();
	 	}
		System.out.println(resourcesLength);
		System.out.println(listResponse);
        if(resourcesLength > 1) {
        	return listResponse.toString();
        }
        else {
        	return response.toString();
        }
		
        


  }
  
  
  @RequestMapping(value = "/cds-services/prior_authorization", method = RequestMethod.POST, 
		  consumes = "application/json", produces = "application/json")
  @ResponseBody
  public String priorAuthorization(@RequestBody Map<String, Object> inputjson,@RequestHeader Map<String,String> headers) {

   // JSONObject obj = new JSONObject();
	  	StringBuilder sb = new StringBuilder();
	    File file;
	    JSONObject response = new JSONObject();
	    response.put("PriorAuthorization", false);
	    
	    /*
	    CloseableHttpClient client = HttpClients.createDefault();
	      // Get the token and drop the "Bearer"
	      final String authorization = headers.get("authorization");
	      String token = null;
	      try {
		       if(authorization != null && authorization.startsWith("Bearer"))
		       {
		        token = authorization.substring("Bearer".length()).trim();
		        System.out.println("token....");
		        System.out.println(token);
		          
		       }
		       else {
			       
		    	   throw new RequestIncompleteException("No valid authorization header was found");	       
		       }
	      }
	      catch(RequestIncompleteException req_exception) {
	    	  	JSONObject errorObj = new JSONObject();
	    	  	errorObj.put("exception", req_exception.getMessage());
		 		return errorObj.toString();
		 	}
	      catch (Exception exception) {
		        System.out.println("388 EXceptionnnnnn");
		        exception.printStackTrace();
		    }
	      
	      String client_Id = "app-token";
	      String client_secret = "48bf2c3e-2bd6-4f8d-a5ce-2f94adcb7492";
	      HttpPost httpPost = new HttpPost("https://3.92.187.150:8443/auth/realms/ClientFhirServer/protocol/openid-connect/token/introspect");
	      List<NameValuePair> params = new ArrayList<NameValuePair>();
	      params.add(new BasicNameValuePair("client_id", client_Id));
	      params.add(new BasicNameValuePair("client_secret", client_secret));
	      params.add(new BasicNameValuePair("token", token));
	      try {
	        httpPost.setEntity(new UrlEncodedFormEntity(params));
	      } catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	      }
	      
	      JsonObject tokenResponse;
	      try {
	        CloseableHttpResponse tokenResponceObj = client.execute(httpPost);
	        String jsonStr = EntityUtils.toString(tokenResponceObj.getEntity());
	        tokenResponse = new JsonParser().parse(jsonStr).getAsJsonObject();      
	        client.close();
	      }
	      catch (IOException e) {
	        System.out.println("412\n\n\\n\n\n\\n\n\n\n\nEXceptionnnnnn");
	        e.printStackTrace();
	        tokenResponse = null;
	      }
	      */
		try {
			//if ((tokenResponse != null) && (tokenResponse.get("active").getAsBoolean())) {
				file = ResourceUtils.getFile("classpath:config/data.json");
				InputStream in = new FileInputStream(file);
				String jsonTxt = IOUtils.toString(in, StandardCharsets.UTF_8);
				JSONObject configData = new JSONObject(jsonTxt);
				ObjectMapper oMapper = new ObjectMapper();
				
				//OLD Logic ---- start
				/*
				List<String> allowedResources = oMapper.convertValue(configData.get("PriorAuthorizationResources") , List.class);
				Map<String, Object> context = oMapper.convertValue(inputjson.get("context") , Map.class);
			    Map<String, Object> orders = oMapper.convertValue(context.get("orders") , Map.class);
			    List<LinkedHashMap> entries = oMapper.convertValue(orders.get("entry") , List.class);
			    entries.forEach((obj) -> {
					  System.out.println("\nresource:"+obj.get("resource").getClass());

					  LinkedHashMap jsonEntry = oMapper.convertValue(obj.get("resource") , LinkedHashMap.class);
				      String resType =(String)  jsonEntry.get("resourceType");
				      if(allowedResources.contains(resType)) {
				    	  response.put("PriorAuthorization", true);
				    	  
				      }
			    });
			    */
			    //OLD LOGIC ---- END
				
				//Updated LOGIC
				if(inputjson.containsKey("hook") && configData.has("PriorAuthorizationHooks")) {
					String hook  = (String) inputjson.get("hook");
					List<String> allowedHooks = oMapper.convertValue(configData.get("PriorAuthorizationHooks") , List.class);
					if(allowedHooks.contains(hook)) {
				    	  response.put("PriorAuthorization", true);
				    	  
				      }
					
				}
				else {
					System.out.println("Missing Key");
				}
				
//			}
//			else {
//		       throw new RequestIncompleteException("Invalid Oauth Token");
//		    }
			
		   
		    
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		catch(RequestIncompleteException req_exception) {
//		  JSONObject errorObj = new JSONObject();
//	  	  errorObj.put("exception", req_exception.getMessage());
//	 	  return errorObj.toString();
//	 	}
		  
       
		return response.toString();
        


  }
  
  
  
  
  @PostMapping("/review")
  @ResponseBody
  public String review(@RequestBody Object inputjson) {
    
    
   // JSONObject obj = new JSONObject();
    StringBuilder sb = new StringBuilder();
 try{
       
        // execute method and handle any error responses.
    	URL url = new URL("http://localhost:3000/test");
        Gson gsonObj = new Gson();
        String jsonStr = gsonObj.toJson(inputjson);
        System.out.println(jsonStr);
        byte[] postDataBytes = jsonStr.getBytes("UTF-8");

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept","application/json");
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        String line =null;
        while((line=in.readLine())!= null){
          sb.append(line);
        }
       
        
    }
    catch (Exception e) {
        System.out.println("\n\n\\n\n\n\\n\n\n\n\nEXceptionnnnnn");
        e.printStackTrace();
    }
    
  String result = sb.toString();
  return result;

  }


  @GetMapping("/public")
  public String public_key(Model model, final HttpServletRequest request) {
    model.addAttribute("contextPath", request.getContextPath());
    return "index";
  }

  @GetMapping("/requests")
  public String request_log(Model model, final HttpServletRequest request) {
    model.addAttribute("contextPath", request.getContextPath());
    return "index";
  }




}
