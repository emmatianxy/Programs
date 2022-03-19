package edu.nmsu.cs.webserver;

/**
 * Web worker: an object of this class executes in its own new thread to receive and respond to a
 * single HTTP request. After the constructor the object executes on its "run" method, and leaves
 * when it is done.
 *
 * One WebWorker object is only responsible for one client connection. This code uses Java threads
 * to parallelize the handling of clients: each WebWorker runs in its own thread. This means that
 * you can essentially just think about what is happening on one client at a time, ignoring the fact
 * that the entirety of the webserver execution might be handling other clients, too.
 *
 * This WebWorker class (i.e., an object of this class) is where all the client interaction is done.
 * The "run()" method is the beginning -- think of it as the "main()" for a client interaction. It
 * does three things in a row, invoking three methods in this class: it reads the incoming HTTP
 * request; it writes out an HTTP header to begin its response, and then it writes out some HTML
 * content for the response content. HTTP requests and responses are just lines of text (in a very
 * particular format).
 * 
 * @author Jon Cook, Ph.D.
 *
 **/

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class WebWorker implements Runnable {

	private Socket socket;
	private String local_filepath;

	/**
	 * Constructor: must have a valid open socket
	 **/
	public WebWorker(Socket s) {
		socket = s;
	}

	/**
	 * Worker thread starting point. Each worker handles just one HTTP request and then returns, which
	 * destroys the thread. This method assumes that whoever created the worker created it with a
	 * valid open socket object.
	 **/
	public void run() {
		System.err.println("Handling connection...");
		try {
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			readHTTPRequest(is);
			this.response(os);
			os.flush();
			socket.close();
		} catch (Exception e) {
			System.err.println("Output error: " + e);
		}
		System.err.println("Done handling connection.");
	}

	/**
	 * Read the HTTP request header.
	 **/
	private void readHTTPRequest(InputStream is) {
		String line;
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		while (true) {
			try {
				while (!r.ready()) {
					Thread.sleep(1);
				}
				line = r.readLine();
				if (line.contains("GET") && line.endsWith("html")) {
					this.local_filepath = this.getFilePath(line.split(" ")[1]);
				}
				System.err.println("Request line: (" + line + ")");
				if (line.isEmpty()) {
					break;
				}
			} catch (Exception e) {
				System.err.println("Request error: " + e);
				break;
			}
		}
	}


	private String getFilePath(String filename) throws IOException {
		File f = new File("");
		File file = new File(f.getCanonicalPath());
		File filepath = new File(file, filename);
		return filepath.getCanonicalPath();
	}


	private void response(OutputStream os) throws IOException {
		File file = new File(this.local_filepath);
		String content = "<html><head></head><body>\n<h2>404 Not found</h2>\n</body></html>\n";
		String response_code = "404 Not Found";
		if (file.exists()) {
			response_code = "200 OK";
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
			StringBuilder buffers = new StringBuilder();
			String con = "";
			while ((con = reader.readLine()) != null) {
				buffers.append(con);
			}
			content = buffers.toString();
			Date date = new Date();
			SimpleDateFormat date_format = new SimpleDateFormat("dd/MM/yyyy");
			content = content.replace("<cs371date>", date_format.format(date))
					.replace("<cs371server>", "web_server");
			reader.close();
		}
		this.write(os,response_code,content);
	}

	private void write(OutputStream os,String code,String content) throws IOException {
		Date d = new Date();
		DateFormat date_format = DateFormat.getDateTimeInstance();
		date_format.setTimeZone(TimeZone.getTimeZone("GMT"));
		os.write(String.format("HTTP/1.1 %s\n", code).getBytes());
		os.write("Date: ".getBytes());
		os.write((date_format.format(d)).getBytes());
		os.write("\n".getBytes());
		os.write("Server: web service\n".getBytes());
		os.write("Connection: close\n".getBytes());
		os.write("Content-Type: ".getBytes());
		os.write("text/html".getBytes());
		os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
		os.write(content.getBytes());
	}


} // end class
