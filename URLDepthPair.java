import java.net.MalformedURLException;
import java.net.URL;

public class URLDepthPair {
	
	private String url;
	private int depth;
	public static final String url_prefix = "http://";
	public static final int max_depth = 100;
	
	// Конструктор
	public URLDepthPair(String url, int depth) throws MalformedURLException {
		// Проверка введенных параметров
		if (depth < 0 || depth > max_depth) {
			throw new IllegalArgumentException("Error limits of depth");
		}
		if (!URLDepthPair.isHttpPrefixInURL(url)) {
			MalformedURLException ex = new MalformedURLException("Error of url prefix");
			throw ex;
		}
		
		this.url = url;
		this.depth = depth;
	}
	
	// Поверяет префикс URL на соответствие протоколу HTTP
	public static boolean isHttpPrefixInURL(String url) {
		if (!url.startsWith(url_prefix)) return false;
		return true;
	}

	// Вывод в виде строки
	public String toString() {
		return "[ " + this.url + ", " + this.depth + " ]";
	}


	public String getHostName() {
		try {
            URL url = new URL(this.url);
            return url.getHost();
        }
        catch (MalformedURLException e) {
            System.err.println("MalformedURLException: " + e.getMessage());
            return null;
        }
	}
	
	public String getPagePath() {
		try {
			URL url = new URL(this.url);
            return url.getPath();
        }
        catch (MalformedURLException e) {
            System.err.println("MalformedURLException: " + e.getMessage());
            return null;
        }
	}
	
	public String getURL() {
		return this.url;
	}
	
	public int getDepth() {
		return this.depth;
	}
	
	public void setDepth(int depth) {
		if (depth < 0 || depth > max_depth) {
			throw new IllegalArgumentException("Error limits of depth");
		} 
		this.depth = depth;
	}
}