package webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import webserver.util.WebWriter;

public class WebRespons {
	private WebWriter writer;
	private HashMap<String, String> headers = new HashMap<>();
	
	public WebRespons(@SuppressWarnings("exports") WebWriter writer) {
		this.writer = writer;
		this.addHeader("Content-Type", "text/html");
	}

	public void addHeader(String name, String value) {
		this.headers.put(name, value);
	}

	public void sendHeader(String status) {
		this.writer.writeLine("HTTP/1.1 "+status);
		for(String key : this.headers.keySet())
			this.writer.writeLine(key+": "+this.headers.get(key));
		this.writer.writeLine("");
	}

	public void send(String string) {
		this.writer.write(string);
	}

	public void sendFile(File file) {
		try {
			FileInputStream stream = new FileInputStream(file);
			this.writer.write(stream.readAllBytes());
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
