package otradotra.helper;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpClientConnection;
import org.apache.http.HttpHost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class NetworkOptimizatorSingleton {
	   private static NetworkOptimizatorSingleton instance = null;
	   private static PoolingHttpClientConnectionManager cm = null;
	   
	   protected NetworkOptimizatorSingleton() {
		      // Exists only to defeat instantiation.
		   }
		   public static NetworkOptimizatorSingleton getInstance() {
		      if(instance == null) {
		         instance = new NetworkOptimizatorSingleton();
		         
		         SSLContext sslContext=null;
				try {
					sslContext = SSLContexts.custom().loadTrustMaterial(KeyStore.getInstance(KeyStore.getDefaultType())).build();
				} catch (KeyManagementException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (KeyStoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
		 		    SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
		 		
		 		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create().register("https", sslsf).build();
		 		
		 	    cm = new PoolingHttpClientConnectionManager();
		 		// Increase max total connection to 200
		 		cm.setMaxTotal(200);
		 		// Increase default max connection per route to 20
		 		cm.setDefaultMaxPerRoute(50);
		 		// Increase max connections for localhost:80 to 50
		 		
		 		
		 	
		         
		         
		      }
		      return instance;
		   }
		   
		   public static CloseableHttpClient createConnetor(){
				CloseableHttpClient httpClient = HttpClients.custom()
		 		        .setConnectionManager(cm)
		 		        .build();
				return httpClient;
		   }
		   
		   public static PoolingHttpClientConnectionManager getPool(){
				
				
				return cm;
		   }
}
