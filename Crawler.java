import java.lang.Exception;
import java.util.*;
import java.net.MalformedURLException;
import java.net.*;
import java.io.*;

public class Crawler {

	// Для тестирования (скопируйте строку без комментарного оператора):
	// http://users.cms.caltech.edu/~donnie/cs11/java/ 1
	// http://users.cms.caltech.edu/~donnie/cs11/java/lectures/cs11-java-lec1.pdf 1

	public static void main (String[] args) {
		Crawler crawler = new Crawler();
		crawler.getFirstURLDepthPair(args);
		crawler.startParse();
		crawler.showResults();
	}

	public static final int HTTP_PORT = 80;
	public static final String HOOK_REF = "<a href=\"";
	public static final String HOOK_HTTP = "<a href=\"http://";
	public static final String HOOK_HTTPS = "<a href=\"https://";
	public static final String HOOK_BACK = "<a href=\"../";
	public static final String BAD_REQUEST_LINE = "HTTP/1.1 400 Bad Request";


	// Список посещённых сайтов и  непосещённых
	LinkedList<URLDepthPair> notVisitedList;
	LinkedList<URLDepthPair> visitedList;

	// Глубина поиска
	int depth;

	// Конструктор
	public Crawler() {
		notVisitedList = new LinkedList<URLDepthPair>();
		visitedList = new LinkedList<URLDepthPair>();
	}

	//Проход по всем сайтам на определённую глубину
	public void startParse() {
		System.out.println("Stating parsing:\n");
		URLDepthPair nowPage = notVisitedList.getFirst();
		while (nowPage.getDepth() <= depth && !notVisitedList.isEmpty()) {

			nowPage = notVisitedList.getFirst();
			Socket socket = null;

			try {
				socket = new Socket(nowPage.getHostName(), HTTP_PORT);
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Connection to [" + nowPage.getURL() + "] created!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

				try {
					socket.setSoTimeout(5000);
				}
				catch (SocketException exc) {
					System.err.println("SocketException: " + exc.getMessage());
					moveURLPair(nowPage, socket);
					continue;
				}

				// Вывод информации о текущей странице
				CrawlerHelper.getInfoAboutUrl(nowPage.getURL(), true);

				// Для отправки запросов
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

				// Запрос на получение html-страницы
				out.println("GET " + nowPage.getPagePath() + " HTTP/1.1");
				out.println("Host: " + nowPage.getHostName());
				out.println("Connection: close");
				out.println("");
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				// Проверка на bad request
				String line = in.readLine();

				if (line.startsWith(BAD_REQUEST_LINE)) {
					System.out.println("ERROR: BAD REQUEST!");
					System.out.println(line + "\n");

					this.moveURLPair(nowPage, socket);
					continue;
				} else {
					System.out.println("REQUEST IS GOOD!\n");
				}

				// Чтение основного файла
				System.out.println("*********Start of file*********");
				// поиск и сбор всех ссылок со страницы
				int strCount = 0;
				int strCount2 = 0;
				while(line != null) {
					try {
						//Извлечнение строки из html-кода
						line = in.readLine();
						strCount += 1;

						// Извлечение ссылки из тэга, если она там есть, если нет, идём к следующей строке
						String url = CrawlerHelper.getURLFromHTMLTag(line);
						if (url == null) continue;
						if (url.startsWith("https://")) {
							System.out.println(strCount2 + " --> " + strCount + " |  " + url + " --> https-refference\n");
							continue;
						}
						if (url.startsWith("../")) {		
							String newUrl = CrawlerHelper.urlFromBackRef(nowPage.getURL(), url);
							System.out.println(strCount2 + " --> " + strCount + " |  " + url + " --> " +  newUrl + "\n");
							this.createURlDepthPairObject(newUrl, nowPage.getDepth() + 1);
						}
						else if (url.startsWith("http://")) {
							String newUrl = CrawlerHelper.cutTrashAfterFormat(url);
							System.out.println(strCount2 + " --> " + strCount + " |  " + url + " --> " + newUrl + "\n");
							this.createURlDepthPairObject(newUrl, nowPage.getDepth() + 1);
						}

						else {		
							String newUrl;
							newUrl = CrawlerHelper.cutURLEndFormat(nowPage.getURL()) + url;
							
							System.out.println(strCount2 + " --> " + strCount + " |  " + url + " --> " + newUrl + "\n");
							this.createURlDepthPairObject(newUrl, nowPage.getDepth() + 1);
						}
						strCount2 += 1;
					}
					catch (Exception e) {
						break;
					}
				}
				
				if (strCount == 1) System.out.println("No http refs in this page!");
				System.out.println("*********End of file*********\n");

				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!Page had been closed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
				
			}
			catch (UnknownHostException e) {
				System.out.println("Opps, UnknownHostException catched, so [" + nowPage.getURL() + "] is not workable now!");
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			// Перемещение сайта после просмотра в список просмотренных
			moveURLPair(nowPage, socket);
			nowPage = notVisitedList.getFirst();
		}
	}

	// Изменение статуса "просмотренности"
	private void moveURLPair(URLDepthPair pair, Socket socket) {
		this.visitedList.addLast(pair);
		this.notVisitedList.removeFirst();
		
		if (socket == null) return;
		
		try {
			socket.close();
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Создание объекта-пары
	private void createURlDepthPairObject(String url, int depth) {
		
		URLDepthPair newURL = null;
		try{
			// Формирование нового объекта и добавление его в список
			newURL = new URLDepthPair(url, depth);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		notVisitedList.addLast(newURL);
	}

	// Вывод в консоль результатов
	public void showResults() {
		System.out.println("-----------------------------------------Results of working-----------------------------------------");

		System.out.println("Scanner scanned next sites:");
		int count = 1;
		for (URLDepthPair pair : visitedList) {
			System.out.println(count + " |  " + pair.toString());
			count += 1;
		}
		System.out.println("");

		System.out.println("Not visited next sites:");
		count = 1;
		for (URLDepthPair pair : notVisitedList) {
			System.out.println(count + " |  " + pair.toString());
			count += 1;
		}

		System.out.println("-----------------------------------------End of results-----------------------------------------");
	}

	// Проверка ввода пользователя
	public void getFirstURLDepthPair(String[] args) {
		CrawlerHelper help = new CrawlerHelper();

		URLDepthPair urlDepth = help.getURLDepthPairFromArgs(args);
		if (urlDepth == null) {
			System.out.println("Enter your arguments manually !\n");
			urlDepth = help.getURLDepthPairFromInput();
		}

		this.depth = urlDepth.getDepth();
		urlDepth.setDepth(0);
		notVisitedList.add(urlDepth);
		System.out.println("First site: " + urlDepth.toString() + "\n");
	}
}