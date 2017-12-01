package com.sesnu.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.Contract;

public class Util {

	private static final Logger mainL = LoggerFactory.getLogger("MainLog");
	
	private final static Properties prop = loadProp();;
	
	public Util(){
		
	}
	
	
	public static void writeToFile(String data, String fileName,boolean overWrite){

			BufferedWriter bw = null;
			FileWriter fw = null;

			try {

				fw = new FileWriter("/Users/mgebreki/Documents/fireball_doc/" + fileName,!overWrite);
				bw = new BufferedWriter(fw);
				bw.write(data + "\n");

			} catch (IOException e) {

				e.printStackTrace();

			} finally {

				try {

					if (bw != null)
						bw.close();

					if (fw != null)
						fw.close();

				} catch (IOException ex) {

					ex.printStackTrace();

				}
			}		
	}
	
	public static List<String> readFile(String fileName,boolean includesPath){
			List<String> readList = new ArrayList<String>();
			BufferedReader br = null;
			FileReader fr = null;
			String filePath = "/home/msghe/Documents/" + fileName;
			if(includesPath)filePath = fileName;
			try {
				File f = new File(filePath);
				if(!f.exists()) return readList;
				
				fr = new FileReader(filePath);
				br = new BufferedReader(fr);

				String sCurrentLine;

				br = new BufferedReader(new FileReader(filePath));

				while ((sCurrentLine = br.readLine()) != null) {
					readList.add(sCurrentLine);
				}

			} catch (IOException e) {

				e.printStackTrace();

			} finally {

				try {

					if (br != null)
						br.close();

					if (fr != null)
						fr.close();

				} catch (IOException ex) {

					ex.printStackTrace();

				}

			}

			return readList;
		}

	public String getFile(String fileName){
		InputStream inputStream=null;
		
		try {

			inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
		
			if (inputStream != null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				StringBuffer sb = new StringBuffer();
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);					
				}
				return sb.toString();
			} else {
				throw new FileNotFoundException("file '" + fileName + "' not found in resources");
			}

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return "";
		}
	
	

	private static Properties loadProp(){
		
		Properties prop = new Properties();
		try {
			File f= new File(Util.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			String path = f.getParentFile().getAbsolutePath() + "/config.properties";
			File file = new File(path);
//			System.out.println(file.getAbsolutePath());
			if(file.exists()){
				mainL.info("Loading custom properties file ....");
				prop.load(new FileInputStream(file));
			}else{
//				mainL.info("Loading properties file from resources folder....");
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				prop = new Properties();
				InputStream resourceStream = loader.getResourceAsStream("config.properties");
				prop.load(resourceStream);
			}

		}catch(Exception e){
			mainL.error("error loading prop file",e);
		}
		return prop;
	}
	
	public static boolean isPaper(){
		return Boolean.parseBoolean(prop.get("paperMode").toString());
	}
	
	public static String getAcctNo(){
		if(!isPaper()){
			return prop.get("liveAccountNo").toString();
		}else{
			return prop.get("paperAccountNo").toString();
		}
	}
	
	public static boolean isDevMode(){
		return Boolean.parseBoolean(prop.getProperty("devMode"));
	}
	
	public static int getInt(String propName){
		String x= prop.getProperty(propName);
		return Integer.parseInt(x);
	}
	
	public static String getString(String propName){
		return prop.getProperty(propName);
	}
	
	public static double getDouble(String propName){
		String x= prop.getProperty(propName);
		return Double.parseDouble(x);
	}
	
	public static boolean getBoolean(String propName){
		String x= prop.getProperty(propName);
		return Boolean.parseBoolean(x);
	}
	
	  
	  public static int delay(Integer interval) {
		  		  
	      final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	      calendar.setTimeInMillis(System.currentTimeMillis());
	      int currentSec = calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND);
	      return interval - (currentSec % interval);
	  }
	  
	 	public synchronized static double roundTo2D(double val){
	 		val = Math.round(val*100);
	 		return val/100;
	 	}
	 	
	 	public synchronized static double roundTo3D(double val){
	 		val = Math.round(val*1000);
	 		return val/1000;
	 	}
	 	
	 	public static String formatTimeStamp(long timeStamp){
	 		Date date = new Date(timeStamp);
	 		DateFormat formatter = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	 		String dateFormatted = formatter.format(date);
	 		return dateFormatted;
	 	}
	 	
	 	public static String getSimpleDate(String dateStr){
	 		Calendar cal = Calendar.getInstance();
	 		cal.setTime(new Date());
	 		if(dateStr.equals("yDay")) cal.add(Calendar.DATE, -1);
	 		DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
	 		String dateFormatted = formatter.format(cal.getTime());
	 		return dateFormatted;
	 	}
	 	
	 	public static int rnd(int min,int max){
	 		Random rand = new Random();
	 		return rand.nextInt((max - min) + 1) + min;
	 	}
	 	
		public static Contract getContract(String ticker){
			   Contract contract = new Contract();
		       contract.symbol(ticker); 

		       contract.secType("STK");

		       contract.exchange("SMART"); 
		       
		       contract.currency("USD");

		       return contract;
		}
	 	

	    public static <K, V extends Comparable<? super V>> Map<K, V> 
	        sortByValue(Map<K, V> map) {
	        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
	        Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
	            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
	                return (o2.getValue()).compareTo( o1.getValue() );
	            }
	        });

	        Map<K, V> result = new LinkedHashMap<K, V>();
	        for (Map.Entry<K, V> entry : list) {
	            result.put(entry.getKey(), entry.getValue());
	        }
	        return result;
	    }
	    

	    public static List<String>  getAllTickers(){
	    	String nysePath = Util.class.getClassLoader().getResource("NYSE.txt").getPath();
	    	List<String> tickers = readFile(nysePath,true);
	    	String nasdaqPath = Util.class.getClassLoader().getResource("NASDAQ.txt").getPath();
	    	tickers.addAll(readFile(nasdaqPath,true));
	    	return tickers;
	    }
	    
	    public static List<String> getWSJ() {
	    	List<String> tickersWSJ = new ArrayList<String>();
	    	try{
				WebDriver driver = new HtmlUnitDriver();
				
				driver.get("https://blogs.wsj.com/moneybeat/tag/stocks-to-watch/");
						
		 	    WebElement ulList = (new WebDriverWait(driver, 10))
			    		   .until(ExpectedConditions.elementToBeClickable(By.id("excerpts-list")));
				
	
			    List<WebElement> li= ulList.findElements(By.tagName("li")); 
	
				String lines[] = li.get(0).getText().split("\\r?\\n");
				
				String time = lines[2].replace("ET", "");
		        SimpleDateFormat parser = new SimpleDateFormat("MMM d, yyyy HH:mm a");
		        parser.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		        Date date = parser.parse(time);
			    
		        if(getDate(date.getTime())==getDate(System.currentTimeMillis())){
			    	String[] tickers = lines[1].replace("Stocks to Watch:", "").trim().split(",");
			    	List<String> allTickers = getAllTickers();
			    	for (String ticker : tickers) {
						if(ticker.trim().length()<=4 && ticker.trim().split("\\s+").length==1){
							tickersWSJ.add(ticker.trim().toUpperCase());
						
						}else{
				    		for (String fullTickerName : allTickers) {
								fullTickerName = fullTickerName.trim().replaceAll(" +", " ");
								boolean contains = fullTickerName.toUpperCase().matches(".*\\b" + ticker.toUpperCase() + "\\b.*");
								if(contains){
									tickersWSJ.add(fullTickerName.split("\\s+")[0].toUpperCase().trim());
									break;
								}
							}
						}
					}
			    	return tickersWSJ;
			    }

				return tickersWSJ;
	    	}catch(Exception e){
	    		e.printStackTrace();
	    		return tickersWSJ;
	    	}
		}
	    
	    public static String parseTime(long time){
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
			calendar.setTimeInMillis(time);
			Integer date = calendar.get(Calendar.DATE);
			Integer hr = calendar.get(Calendar.HOUR_OF_DAY);
	        Integer min = calendar.get(Calendar.MINUTE);
	        Integer sec = calendar.get(Calendar.SECOND);
	        String result = date + "/" +  hr + ":" + min;
	        if(!sec.equals(0)){
	        	result = result + "." + sec;
	        }
	        return result;
		}
	


	    public static double getDoubleTime(){
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
			calendar.setTimeInMillis(System.currentTimeMillis());
			Integer hr = calendar.get(Calendar.HOUR_OF_DAY);
	        Integer min = calendar.get(Calendar.MINUTE);
	        String minStr = min<10? "0" + min:min.toString();
	        return Double.parseDouble(hr + "." + minStr);
		}
	    
	    public static double getDoubleTime(long time){
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
			calendar.setTimeInMillis(time);
			Integer hr = calendar.get(Calendar.HOUR_OF_DAY);
	        Integer min = calendar.get(Calendar.MINUTE);
	        String minStr = min<10? "0" + min:min.toString();
	        return Double.parseDouble(hr + "." + minStr);
		}
	    
	    public static String getDateTime(long time){
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
			calendar.setTimeInMillis(time);
			Integer date = calendar.get(Calendar.DATE);
			Integer hr = calendar.get(Calendar.HOUR_OF_DAY);
	        Integer min = calendar.get(Calendar.MINUTE);
	        String minStr = min<10? "0" + min:min.toString();
	        return date + "/" + hr + "." + minStr;
		}
	    
	    public static double getDate(long time){
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
			calendar.setTimeInMillis(time);
			return calendar.get(Calendar.DATE);
		}
	    
	    public static String getDateTimeStr(long time){
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
			calendar.setTimeInMillis(time);
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			return dateFormat.format(calendar.getTime());
		}
	    
	    public static String getDateStr(long time){
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
			calendar.setTimeInMillis(time);
			DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			return dateFormat.format(calendar.getTime());
		}
	    
	    public static long getDayStartTime(){
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
			calendar.setTimeInMillis(System.currentTimeMillis());
			Integer hr = calendar.get(Calendar.HOUR_OF_DAY);
	        Integer min = calendar.get(Calendar.MINUTE); 
	        return calendar.getTimeInMillis()-hr*3600000-min*60000;
		}
	    
		public static void sendMail(String subject,String to, String msg) {
	        Properties props = new Properties();
	        props.put("mail.smtp.host", "smtp.gmail.com");
	        props.put("mail.smtp.socketFactory.port", "465");
	        props.put("mail.smtp.socketFactory.class",
	                "javax.net.ssl.SSLSocketFactory");
	        props.put("mail.smtp.auth", "true");
	        props.put("mail.smtp.port", "465"); 
	        Session session = Session.getDefaultInstance(props,
	            new javax.mail.Authenticator() {
	                                @Override
	                protected PasswordAuthentication getPasswordAuthentication() {
	                    return new PasswordAuthentication("fireball78660@gmail.com","delta@1977");
	                }
	            });

	        try {

	            Message message = new MimeMessage(session);
	            message.setFrom(new InternetAddress("fireball78660@gmail.com"));
	            message.setRecipients(Message.RecipientType.TO,
	                    InternetAddress.parse(to));
	            message.setSubject(subject);
	            message.setText(msg);

	            Transport.send(message);

	            mainL.info("Notification sent to: {}, content: {}",to,msg);

	        } catch (MessagingException e) {
	            throw new RuntimeException(e);
	        }
	    }


		public static String getMode() {
			boolean devMode = isDevMode();
			boolean isPaper = isPaper();
			if(devMode && isPaper) return "DEV-MODE";
			else if(!devMode && isPaper) return "PAPER-MODE";
			else if(!devMode && !isPaper) return "LIVE-MODE";
			return "NA";
		}
		
	  

}
