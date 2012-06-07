// Ex9 -- Loading JSON
iimport java.net.URI;
import java.util.List;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;

import java.io.FileReader;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class Presidents {

  public static void main(String[] args) throws Exception {
    
    // Read the Data
    
    Gson gson = new Gson();

    President[] Presidents = gson.fromJson(new 
            FileReader("Presidents.json"),
            President[].class);

    try {

      URI local = new URI("http://localhost:8091/pools");
      List<URI> baseURIs = new ArrayList<URI>();
      baseURIs.add(local);

      CouchbaseClient c = new CouchbaseClient(baseURIs, "default", "");
      for (President entry : Presidents) {
        String JSONentry = gson.toJson(entry);
        c.set(entry.presidency, 0, JSONentry).get();
        
        System.out.println(entry.presidency + " " +
                c.get(entry.presidency));
      }
      c.shutdown(3, TimeUnit.SECONDS);
    } catch (Exception e) {
      System.err.println("Error connecting to Couchbase: " + e.getMessage());
      System.exit(0);
    }
  System.exit(0);
  }
}

