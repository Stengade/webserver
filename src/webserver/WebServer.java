package webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import webserver.util.WebRouteContainer;
import webserver.util.WebserverListenerThread;

public class WebServer {
	private ServerSocket socket;
	private boolean isActive = true;
	private WebRouteContainer routes = new WebRouteContainer();
	private String defaultFile = "index.html";
	private WebserverListenerThread[] threads = new WebserverListenerThread[0];
	
	/**
	 * Open server socket and prepare to listen. Only when 
	 * listen() is called the webserver start listen and handle 
	 * request
	 * @param port The port you want the port to listen on
	 * @throws WebServerConnectionException When failed to open server socket
	 */
	public WebServer(int port) throws WebServerConnectionException {
		try {
			socket = new ServerSocket(port);
			this.setThreadSize(1);
		} catch (IOException e) {
			throw new WebServerConnectionException(e.getMessage());
		}
	}

	/**
	 * When got a request there is not handled by 'on(String path, IWebRequestHandler hander)' it call
	 * this by default
	 * @param IWebRequestHandler The request handler
	 */
	public void onUnknownRequest(IWebRequestHandler defaultsHandler) {
		this.routes.onUnknownRequest(defaultsHandler);
	}

	/**
	 * Start listen after request this server
	 */
	public void listen() {
		new Thread(this::loop).start();
	}
	
	/**
	 * When the server got a directory request it add a default file
	 * This is becuse this server do not support directory only file
	 * @param file The file name you want to add to the directory
	 */
	public void setDefaultFile(String file) {
		this.defaultFile = file;
	}
	
	public String getDefaultFile() {
		return this.defaultFile;
	}
	
	private void loop() {
		while(this.isActive) {
			try {
				handleRequest(this.socket.accept());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void handleRequest(Socket socket) throws IOException {
		WebserverListenerThread thread = this.threads[0];
		for(int i=1;i<this.threads.length;i++) {
			if(thread.inQuery() > this.threads[i].inQuery())thread = this.threads[i];
		}
		
		thread.addQuery(socket);
	}

	public void on(String path, IWebRequestHandler object) {
		this.routes.put(path, object);
	}
	
	public void setThreadSize(int newSize) {
		if(newSize < this.threads.length) {
			for(int i=newSize;i<this.threads.length;i++) {
				this.threads[i].close();
			}
			this.threads = Arrays.copyOf(this.threads, newSize);
		}else {
			WebserverListenerThread[] buffer = Arrays.copyOf(this.threads, newSize);
			for(int i=this.threads.length;i<newSize;i++)buffer[i] = new WebserverListenerThread(this, this.routes);
			this.threads = buffer;
		}
	}

	public int getThreadSize() {
		return this.threads.length;
	}

	public int[] getThreadQuery() {
		int[] result = new int[this.getThreadSize()];
		for(int i=0;i<result.length;i++)
			result[i] = this.threads[i].inQuery();
		return result;
	}
}
