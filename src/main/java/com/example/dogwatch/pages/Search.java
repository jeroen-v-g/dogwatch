package com.example.dogwatch.pages;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ApplicationGlobals;
import org.apache.tapestry5.services.HttpError;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.util.TextStreamResponse;
import org.slf4j.Logger;

import com.example.dogwatch.annotations.AnonymousAccess;
import com.example.dogwatch.dao.ImageTransfer;
import com.example.dogwatch.dao.SearchBot;
import com.example.dogwatch.services.Authenticator;

/**
 * Start page of application DogWatch
 */

@Import(module="search")
@AnonymousAccess
public class Search {
	@Inject
	private Logger logger;
	
	@Inject
	private Authenticator authenticator;

	@Property
	@Inject
	@Symbol(SymbolConstants.TAPESTRY_VERSION)
	private String tapestryVersion;

	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM uuuu HH:mm:ss");
	
	@Property
	@Persist
	private boolean isSearching;

	private static boolean stopSearch;

	private static SearchBot searchBot;
	private static ExecutorService executor;
	static {
		searchBot = new SearchBot();
		executor = Executors.newSingleThreadExecutor();
	}
	
	@Inject
	private ApplicationGlobals applicationGlobals;
	
	@InjectPage
	private NetworkDisk networkDisk;

	@Inject
	private Request request;

	@Environmental
	private JavaScriptSupport javaScriptSupport;
	
	// Handle call with an unwanted context
	Object onActivate(EventContext eventContext) {
		
		if (stopSearch) {
			isSearching = false;
			stopSearch = false;
		}
		return eventContext.getCount() > 0 ? new HttpError(404, "Resource not found") : null;
	}
	
	void afterRender()
	{
		if (isSearching)
			javaScriptSupport.require("search").invoke("startRefreshCall").with(getContextPath());
		else 
			javaScriptSupport.require("search").invoke("doAjaxCall").with(getContextPath());
	}
	
	public String getContextPath()
	{
		String context = applicationGlobals.getServletContext().getContextPath();
		return context!=null?context:"";
	}

	@Log
	Object onSearch() {
		if (!authenticator.isLoggedIn())
			return Login.class;
		if (!isSearching) {
			isSearching = true;
			searchBot.setSearchResults(networkDisk.getSelectedPaths());
			executor.execute(searchBot);
			executor.execute(() -> {
				stopSearch = true;
				System.out.println("Searching done");
			});

		}
		return null;
	}

	void onStopsearch() {
		isSearching = false;
		searchBot.stopSearching();
	}

	@Log
	StreamResponse onRetrieve() {
		if (request.getParameter("action") != null && request.getParameter("action").equals("isSearching")) {
			JSONObject json = new JSONObject();
			json.put("isSearching", isSearching);
			return new TextStreamResponse("application/json", json.toCompactString());
		}

		if (request.getParameter("action") != null && request.getParameter("action").equals("refresh")) {

			StringBuffer stringBuffer = new StringBuffer();
			for (String fileName : searchBot.found) {
				stringBuffer.append("<p><i>" + fileName + "</i></p>\n");
				
				String imageUrl = "";
				try {
					imageUrl = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
				} catch (UnsupportedEncodingException e) {
					logger.error("Could not encode image URL",e);
				}
				
				stringBuffer.append("<p><img src="+getContextPath()+"/search:image?file=" + imageUrl + " ></img></p>\n");
			}

			return new TextStreamResponse("text/html;charset=utf-8", stringBuffer.toString());
		}
		// If no action matches return empty string
		return new TextStreamResponse("text/html;charset=utf-8", "");

	}

	@Log
	StreamResponse onImage() {

		String fileName = request.getParameter("file");
		if (fileName == null)
			return new TextStreamResponse("", "");

		StreamResponse streamResponse = new StreamResponse() {

			public String getContentType() {
				return "image/jpg ";
			}

			@Override
			public InputStream getStream() throws IOException {
				return ImageTransfer.getThumbImage(fileName);
			}

			@Override
			public void prepareResponse(Response response) {

			}
		};
		return streamResponse;
	}

	public String getCurrentTime() {
		return dateTimeFormatter.format(LocalDateTime.now());
	}
}
