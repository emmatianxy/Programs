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
import java.text.DateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

public class WebWorker implements Runnable
{

	private Socket socket;
	private String filename;
	private File file;
	private File root;

	/**
	 * Constructor: must have a valid open socket
	 **/
	public WebWorker(Socket s) {
		socket = s;
		this.root=new File("www");
	}

	/**
	 * Worker thread starting point. Each worker handles just one HTTP request and then returns, which
	 * destroys the thread. This method assumes that whoever created the worker created it with a
	 * valid open socket object.
	 **/
	public void run()
	{
		System.err.println("Handling connection...");
		try
		{
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			readHTTPRequest(is);
			writeHTTPHeader(os, "text/html");
			writeContent(os);
			os.flush();
			socket.close();
		}
		catch (Exception e)
		{
			System.err.println("Output error: " + e);
		}
		System.err.println("Done handling connection.");
		return;
	}

	/**
	 * Read the HTTP request header.
	 **/
	private void readHTTPRequest(InputStream is) {
		String line;
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
		int count = 0;
		while (true) {
			try {
				while (!r.ready())
					Thread.sleep(1);
				line = r.readLine();
				if(count == 0) {
					String[] tokens = line.split(" ");
					this.filename = tokens[1].substring(1);
				}
				count++;
				System.err.println("Request line: (" + line + ")");
				if (line.length() == 0)
					break;
			} catch (Exception e) {
				System.err.println("Request error: " + e);
				break;
			}
		}
	}

	/**
	 * Write the HTTP header lines to the client network connection.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 * @param contentType
	 *          is the string MIME content type (e.g. "text/html")
	 **/
	private void writeHTTPHeader(OutputStream os, String contentType) throws Exception {
		Date d = new Date();
		DateFormat df = DateFormat.getDateTimeInstance();
		df.setTimeZone(TimeZone.getTimeZone("GMT"));

		file = new File(this.root,filename);
		if(filename.isEmpty() || file.exists()) {
			os.write("HTTP/1.1 200 OK\n".getBytes());
		}
		else {
			os.write("HTTP/1.1 404 Not Found\n".getBytes());
		}
		os.write("Date: ".getBytes());
		os.write((df.format(d)).getBytes());
		os.write("\n".getBytes());
		os.write("Server: Tian's very own server\n".getBytes());
		os.write("Connection: close\n".getBytes());
		os.write("Content-Type: ".getBytes());
		os.write(contentType.getBytes());
		os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines

	}


	/**
	 * Write the data content to the client network connection. This MUST be done after the HTTP
	 * header has been written out.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 **/
	private void writeContent(OutputStream os) throws Exception {
		file = new File(this.root,filename);
		if(!file.exists()) {
			file = new File("www/404.html");
			Scanner sc1 = new Scanner(file);
			while(sc1.hasNext()) {
				String l = sc1.nextLine();
				os.write(l.getBytes());
			}
			sc1.close();
		}
		else {
			if(get_content_type().equals("text/html")) {
				Scanner sc = new Scanner(file);
				while(sc.hasNext()) {
					String l = sc.nextLine();
					if(l.contains("<cs371date>")) {
						Date d1 = new Date();
						DateFormat df = DateFormat.getDateTimeInstance();
						df.setTimeZone(TimeZone.getTimeZone("GMT-6:00"));
						os.write("Date: ".getBytes());
						os.write((df.format(d1)).getBytes());
						os.write("\n".getBytes());
					}
					if(l.contains("<cs371server>")) {
						os.write("Server: Tian's very own server\n".getBytes());
					}
					os.write(l.getBytes());
				}
				sc.close();
			}

			else {
				//read file
				byte[] bFile = new byte[(int) file.length()];
				FileInputStream fis = new FileInputStream(filename);
				fis.read(bFile);
				for(int i = 0; i < bFile.length; i++) {
					os.write(bFile[i]);
				}
				fis.close();
			}
		}
	}

	public String get_content_type() {
		//accord to current filename get different content type
		String content_type = null;
		if(filename.contains(".html")) {
			content_type = "text/html";
		} else if(filename.contains(".jpg") || filename.contains(".jpeg")) {
			content_type = "image/jpg";
		} else if(filename.contains(".png")) {
			content_type = "image/png";
		} else if(filename.contains(".gif"))  {
			content_type = "image/gif";
		}
		return content_type;
	}


} // end class
