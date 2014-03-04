package otradotra.network;

import org.codehaus.jackson.map.ObjectMapper;

public class JSONParsingOptimizationSingleton {

	private static ObjectMapper mapper = null;
	private static JSONParsingOptimizationSingleton instance = null;

	 protected JSONParsingOptimizationSingleton() {
	      // Exists only to defeat instantiation.
	   }
	 
	   public static JSONParsingOptimizationSingleton getInstance() {
	      if(instance == null) {
	         instance = new JSONParsingOptimizationSingleton();
	         setMapper(new ObjectMapper());
	      }
	      return instance;
	   }
	    
	   
	   
	   

	public static ObjectMapper getMapper() {
		return mapper;
	}

	public static void setMapper(ObjectMapper mapper) {
		JSONParsingOptimizationSingleton.mapper = mapper;
	}
	   
	      
}
