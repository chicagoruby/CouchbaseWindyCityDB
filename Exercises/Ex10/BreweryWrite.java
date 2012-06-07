// Ex10 -- Loading the breweries!!

/**
 *
 * @author rags
 */
import java.net.URI;
import java.util.List;

import com.couchbase.client.CouchbaseClient;
import java.io.FileReader;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

class BreweryDTO
{
  String id;      
  String type;   
  String name;   
  String state;
  String latitude;
  String longitude;
  String reviews;
}

public class BreweryWrite {

  public static void main(String[] args) throws Exception {
    
    // Read the Data
    
    Gson gson = new Gson();
    BreweryDTO[] myTypes = gson.fromJson(new 
            FileReader("breweries.50.json"),
            BreweryDTO[].class);

    try {

      URI local = new URI("http://localhost:8091/pools");
      List<URI> baseURIs = new ArrayList<URI>();
      baseURIs.add(local);

      CouchbaseClient c = new CouchbaseClient(baseURIs, "default", "");
      for (BreweryDTO entry : myTypes) {
        String JSONentry = gson.toJson(entry);
        c.set(entry.id, 0, JSONentry);
        
        System.out.println(entry.id + " " +
                c.get(entry.id));
      }
      c.shutdown(3, TimeUnit.SECONDS);
    } catch (Exception e) {
      System.err.println("Error connecting to Couchbase: " + e.getMessage());
      e.printStackTrace();
      System.exit(0);
    }
  System.exit(0);
  }
}

