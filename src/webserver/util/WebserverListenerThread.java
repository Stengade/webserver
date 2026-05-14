package webserver.util;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import webserver.WebRequest;
import webserver.WebRespons;
import webserver.WebServer;

public class WebserverListenerThread implements Runnable {
	private List<Socket> query = new ArrayList<>();
	private Thread thread = new Thread(this);
	private boolean isRun = true;
	private WebServer server;
	private WebRouteContainer routes;
	
	public WebserverListenerThread(WebServer server, WebRouteContainer routes) {
		this.server = server;
		this.routes = routes;
		this.thread.start();
	}
	
	public int inQuery() {
		return this.query.size();
	}

	public void addQuery(Socket socket) {
		this.query.add(socket);
	}

	@Override
	public void run() {
		while(this.isRun) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(this.query.isEmpty())continue;
			while(!this.query.isEmpty()) {
				try {
					Socket socket = this.query.getFirst();
					this.handleRequest(socket);
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				this.query.remove(0);
			}
		}
	}
	
	public void close() {
		this.isRun = false;
	}
	
	private void handleRequest(Socket socket) throws IOException {
		WebReader reader = new WebReader(socket);
		String line = reader.readLine();
		if(line == null)return;
		
		String[] parts = line.split(" ");
		if(parts.length != 3 || !parts[2].startsWith("HTTP/"))return;
		
		HashMap<String, String> header = new HashMap<>();
		while(true) {
			line = reader.readLine();
			if(line == null)return;
			if(line.isEmpty())break;
			
			int pos = line.indexOf(": ");
			if(pos == -1)return;
			header.put(line.substring(0, pos), line.substring(pos + 2));
		}
		
		String path = parts[1];
		String query = "";
		
		if(path.indexOf("?") != -1) {
			int pos = path.indexOf("?");
			query = path.substring(pos+1);
			path = path.substring(0, pos);
		}
		
		if(path.endsWith("/"))path += this.server.getDefaultFile();
		
		this.routes.handleRoute(new WebRequest(path, query, header, reader), new WebRespons(new WebWriter(socket)));
	}
}
