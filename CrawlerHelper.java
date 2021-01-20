import java.lang.Exception;
import java.util.*;
import java.net.MalformedURLException;
import java.net.*;
import java.io.*;

public class CrawlerHelper {
	
	// Набор форматов
	public static String[] formats = {".html", ".pdf", ".java", ".xml", "txt", ".css", ".doc", ".c"};	


	// Получение пары из аргументов
	public URLDepthPair getURLDepthPairFromArgs(String[] args) {
		if (args.length > 2) System.out.println("Warning more than 2 parameters from command line!\n");
		if (args.length < 2) {
			System.out.println("Warning less than 2 parameters from command line!\n");
			return null;
		}
		
		// Проверка второго параметра - глубины
		int depth;
		try {
			depth = Integer.parseInt(args[1]);
		} catch (Exception e) {
			System.out.println("Error depth parameter!");
			return null;
		}
		
		URLDepthPair urlDepth;
		
		// Вызов конструктора класса конструктором класса (вызовается, если не понравится какой-либо параметр)
		try {
			urlDepth = new URLDepthPair(args[0], depth);
		} 
		catch (MalformedURLException ex) {
			System.out.println(ex.getMessage() + "\n");
			return null;
		}  
		catch (IllegalArgumentException e) {
			System.out.println(e.getMessage() + "\n");
			return null;
		}
		
		return urlDepth;
	}

	// Получение пары из ввода через консоль
	public URLDepthPair getURLDepthPairFromInput() {
		
		// Временные переменные для хранения
		String url;
		int depth;
		
		// Массив со значениями для передачи на проверку
		String[] args;
		
		// Объект искомого класса
		URLDepthPair urlDepth = null;
		
		// Сканер ввода пользователя
		Scanner in = new Scanner(System.in);
		
		/*
		* Считывание, преобразование и проверка ввода пользователя
		*/ 	
		while (urlDepth == null) {
			
			// Считывание
			System.out.println("Enter URL and depth of parsing (in a line with a space between):");
			String input = in.nextLine();
		
			// Преобразование
			args = input.split(" ", 2);
			
			// Проверка
			urlDepth = this.getURLDepthPairFromArgs(args);
			if (urlDepth == null) System.out.println("Try again!\n");
		}
		return urlDepth;	
	}


	/*
	* Вывод информации по интересующему URL
	*/
	public static String[] getInfoAboutUrl(URL url, boolean needToOut) {
		
		String[] info = new String[10];

		info[0] = url.toString();
		try {
			info[1] = url.toURI().toString();
		} catch (URISyntaxException e) {
			System.out.println("Cannot get URI, this may be https protocol page");
			info[1] = "";
		}
		info[2] = url.getPath();
		info[3] = url.getHost();
		info[4] = String.valueOf(url.getPort());
		info[5] = url.getRef();
		info[6] = url.getProtocol();
		info[7] = url.getUserInfo();
		info[8] = url.getFile();
		try {
			info[9] = url.getContent().toString();
		} catch (IOException e) {
			System.out.println("Cannot get content-type, this may be https protocol page");
			info[9] = "";
		}
		
		if (needToOut) {
			System.out.println("\n------------Info about this url------------");
			System.out.println("Full url: " + info[0]);
			System.out.println("URI: " + info[1]);
			System.out.println("Path: " + info[2]);
			System.out.println("Host name : " + info[3]);
			System.out.println("Port: " + info[4]);
			System.out.println("Ref: " + info[5]);
			System.out.println("Protocol: " + info[6]);
			System.out.println("UserInfo: " + info[7]);
			System.out.println("Files: " + info[8]);
			System.out.println("Content: " + info[9]);
			System.out.println("---------------------------------------------\n");
		}		
		return info;
	}
	
	public static String[] getInfoAboutUrl(String urlStr, boolean needToOut) {
		URL url = null;
		try {
			url = new URL(urlStr);
		}
		catch (MalformedURLException e) {
			System.err.println("MalformedURLException: " + e.getMessage());
			return null;
		}
		
		String[] info = getInfoAboutUrl(url, needToOut);	
		return info;
		
	}


	// Выделение ссылки из текста тэга
	public static String getURLFromHTMLTag(String line) {
		if (line.indexOf(Crawler.HOOK_REF) == -1) return null;
		
		int indexStart = line.indexOf(Crawler.HOOK_REF) + Crawler.HOOK_REF.length();
		int indexEnd = line.indexOf("\"", indexStart);
		if (indexEnd == -1) return null;

		return line.substring(indexStart, indexEnd);
	}
	
	// Очищает от мусора после адреса
	public static String cutURLEndFormat(String url) {
		url = CrawlerHelper.cutTrashAfterFormat(url);
		
		for (String format : formats) {
			if (url.endsWith(format)) {
				int lastCatalog = url.lastIndexOf("/");
				return url.substring(0, lastCatalog + 1);
			}
		}
		return url;
	}
	
	// Склейка ссылки с возвратом с полной текущей ссылкой
	public static String urlFromBackRef(String url, String backRef) {
		int count = 2;
		int index = url.length();

		char[] urlSequnce = url.toCharArray();
		while (count > 0 && index > 0) {
			index -= 1;
			if (urlSequnce[index] == '/') count -= 1;
		}
		
		if (index == 0) return null;
		
		String cutURL = url.substring(0, index + 1);
		String cutBackRef = backRef.substring(3, backRef.length());
		
		return (cutURL + cutBackRef);
	}
	
	// Очистка лишней вохможной информации после указания формата в адресе
	public static String cutTrashAfterFormat(String url) {
		int index = url.lastIndexOf("#");
		if (index == -1) return url;
		return url.substring(0, index);
		
	}
}