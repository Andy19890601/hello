package cn.lucas.crawler.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler {

	private List<String> cookies;
	private HttpURLConnection conn;

	private final String USER_AGENT = "Mozilla/5.0";

	public static void main(String[] args) throws Exception {

		String url = "http://www.newsmth.net/nForum/board/Intern";
		String url2 = "http://www.newsmth.net/nForum/board/Intern?p=2";
		String beiyoubbs = "http://bbs.byr.cn/board/parttimejob";
		String gmail = "https://mail.google.com/mail/";

		String beiyouren = "http://bbs.byr.cn/board/parttimejob";

		// shuimu bbs
		String shuimu = "http://www.newsmth.net/nForum/board/Intern";

		// weiming bbs
		String weiming = "http://www.bdwm.net/bbs/bbsdoc.php?board=Intern&go=U&to=3000";

		Crawler http = new Crawler();

		// make sure cookies is turn on
		CookieHandler.setDefault(new CookieManager());

		// 1. Send a "GET" request, so that you can extract the form's data.
		String page = http.getPageContent(shuimu);
	//	System.out.println("page" + page);

		String ROOT_PATH = "/Users/stanley_hwang/Code/crawler/html/";

		String SHUIMU_FILE_PATH = "shuimu-" + new Date().toString() + ".html";

		OutputStreamWriter writer = new OutputStreamWriter(  
				new FileOutputStream(new File(ROOT_PATH+SHUIMU_FILE_PATH), true), "UTF-8");  
		BufferedWriter bw = new BufferedWriter(writer);
		bw.write(page);
		//		String postParams = http.getFormParams(page, "huang3981658@gmail.com", "19890601h");
		//
		//		// 2. Construct above post's content and then send a POST request for
		//		// authentication
		//		http.sendPost(url, postParams);
		//
		//		// 3. success then go to gmail.
		//		String result = http.GetPageContent(gmail);
		//		System.out.println(result);
	}

	private String getPageContent(String url) throws Exception {

		URL obj = new URL(url);
		conn = (HttpURLConnection)obj.openConnection();

		// default is GET
		conn.setRequestMethod("GET");

		conn.setUseCaches(false);

		// act like a browser
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Charset", "GB2312");  
		conn.setRequestProperty("contentType", "GB2312");  
		conn.setConnectTimeout(5 * 1000);  
		if (cookies != null) {
			for (String cookie : this.cookies) {
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}
		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);
		if (conn.getResponseCode() == conn.HTTP_OK) {
		}
		InputStreamReader isr = new InputStreamReader(conn.getInputStream());
		System.out.println("Encode = " + isr.getEncoding());
		
		BufferedReader in = 
				new BufferedReader(new InputStreamReader(conn.getInputStream(), "GB2312"));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			System.out.println(inputLine);
			response.append(inputLine + "/n");
		}
		in.close();

		// Get the response cookies
		setCookies(conn.getHeaderFields().get("Set-Cookie"));

		return response.toString();
	}

	private void sendPost(String url, String postParams) throws Exception {

		URL obj = new URL(url);
		conn = (HttpURLConnection)obj.openConnection();

		// Acts like a browser
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Host", "accounts.google.com");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		for (String cookie : this.cookies) {
			conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
		}
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Referer", "https://accounts.google.com/ServiceLoginAuth");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));

		conn.setDoOutput(true);
		conn.setDoInput(true);

		// Send post request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();

		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + postParams);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = 
				new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		// System.out.println(response.toString());

	}



	public String getFormParams(String html, String username, String password)
			throws UnsupportedEncodingException {

		System.out.println("Extracting form's data...");

		Document doc = Jsoup.parse(html);

		// Google form id
		Element loginform = doc.getElementById("gaia_loginform");
		Elements inputElements = loginform.getElementsByTag("input");
		List<String> paramList = new ArrayList<String>();
		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");

			if (key.equals("Email"))
				value = username;
			else if (key.equals("Passwd"))
				value = password;
			paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
		}

		// build parameters list
		StringBuilder result = new StringBuilder();
		for (String param : paramList) {
			if (result.length() == 0) {
				result.append(param);
			} else {
				result.append("&" + param);
			}
		}
		return result.toString();
	}

	public List<String> getCookies() {
		return cookies;
	}

	public void setCookies(List<String> cookies) {
		this.cookies = cookies;
	}

}
