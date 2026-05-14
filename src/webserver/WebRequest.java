package webserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import webserver.util.WebReader;

public class WebRequest {

	private String path;
	private HashMap<String, String> posts = new HashMap<>();
	private HashMap<String, String> gets = new HashMap<>();

	public WebRequest(String path, String query, Map<String, String> header, @SuppressWarnings("exports") WebReader reader) {
		this.path = path;
		if(!query.isEmpty()) {
			for(String parts : query.split("&")) {
				int pos = parts.indexOf("=");
				if(pos == -1)continue;
				
				try {
					this.gets.put(parts.substring(0, pos), URLDecoder.decode(parts.substring(pos+1), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		if(header.containsKey("Content-Type")){
			final String contenttype = header.get("Content-Type");
			if(contenttype.equals("application/x-www-form-urlencoded")) {
				byte[] buf = new byte[Integer.parseInt(header.get("Content-Length"))];
				reader.read(buf);
				String body = new String(buf);
				for(String part : body.split("&")) {
					int pos = part.indexOf("=");
					try {
						this.posts.put(URLDecoder.decode(part.substring(0, pos), "UTF-8"), URLDecoder.decode(part.substring(pos+1), "UTF-8"));
					} catch (UnsupportedEncodingException e) {
					}
				}
				return;
			}
			
			if(contenttype.startsWith("multipart/form-data; boundary=")) {
				String boundary = "--"+contenttype.substring(30);
				String line = reader.readLine();
				if(!line.startsWith(boundary))return;
				while(!line.endsWith("--")) {
					HashMap<String, String> c = new HashMap<>();
					while(true) {
						line = reader.readLine();
						if(line.isEmpty())break;
						int pos = line.indexOf(": ");
						if(pos == -1)return;
						c.put(line.substring(0, pos), line.substring(pos+2));
					}
					
					if(!c.containsKey("Content-Disposition") || !c.get("Content-Disposition").startsWith("form-data; name=\""))return;
					HashMap<String, String> parts = this.convertMultipart(c.get("Content-Disposition"));
					if(!parts.containsKey("name"))return;
					StringBuilder builder = new StringBuilder();
					if(parts.containsKey("filename")) {
						try {
							File temp = File.createTempFile("upload_", null);
							temp.deleteOnExit();
							FileWriter writer = new FileWriter(temp);
							while(!(line = reader.readLine()).startsWith(boundary)) {
								writer.write(line+"\r\n");
							}
							writer.close();
							this.posts.put(parts.get("name"), temp.getAbsolutePath());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}else {
						while(!(line = reader.readLine()).startsWith(boundary)) {
							if(!builder.isEmpty())builder.append("\r\n");
							builder.append(line);
						}
						this.posts.put(parts.get("name"), builder.toString().trim());
					}
				}
			}
		}
	}
	
	private HashMap<String, String> convertMultipart(String string) {
		HashMap<String, String> buffer = new HashMap<>();
		
		for(String context : string.split("; ")) {
			int pos = context.indexOf("=");
			if(pos == -1)continue;
			String value = context.substring(pos+1);
			if(value.charAt(0) == '"')
				value = value.substring(1, value.length()-1);
			
			buffer.put(context.substring(0, pos), value);
		}
		
		return buffer;
	}

	/**
	 * Get the request path
	 * @return the request path there was given by the client
	 */
	public String getPath() {
		return this.path;
	}
	
	public boolean hasPost(String key) {
		return this.posts.containsKey(key) && !this.posts.get(key).isEmpty();
	}

	public String post(String string) {
		if(!this.hasPost(string))return "";
		return this.posts.get(string);
	}
	
	public boolean hasGet(String key) {
		return this.gets.containsKey(key) && !this.gets.get(key).isEmpty();
	}
	
	public String get(String key) {
		if(!this.hasGet(key))
			return "";
		return this.gets.get(key);
	}
}
