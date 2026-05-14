package webserver.util;

import java.util.HashMap;

import webserver.IWebRequestHandler;
import webserver.WebRequest;
import webserver.WebRespons;

public class WebRouteContainer {
	private HashMap<String, IWebRequestHandler> routes = new HashMap<>();
	private IWebRequestHandler defaultsHandler;

	public void put(String path, IWebRequestHandler object) {
		this.routes.put(path, object);
	}
	
	public void handleRoute(WebRequest webRequest, WebRespons webRespons) {
		if(this.routes.containsKey(webRequest.getPath())) {
			this.routes.get(webRequest.getPath()).trigger(webRequest, webRespons);
			return;
		}
		
		if(this.defaultsHandler != null)
			this.defaultsHandler.trigger(webRequest, webRespons);
	}

	public void onUnknownRequest(IWebRequestHandler defaultsHandler) {
		this.defaultsHandler = defaultsHandler;
	}
}
