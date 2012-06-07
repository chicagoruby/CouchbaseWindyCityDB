// Ex10 -- Loading the Beers!!

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

class BeerDTO
{
  String id;      
  String type;     
  String breweryId;   
  String name;
  float abv;
}

public class BeerWrite {

  public static void main(String[] args) throws Exception {
    
    // Read the Data
    
    Gson gson = new Gson();
    BeerDTO[] myTypes = gson.fromJson(new 
            FileReader("beers.50.json"),
             BeerDTO[].class);

    try {

      URI local = new URI("http://localhost:8091/pools/");
      List<URI> baseURIs = new ArrayList<URI>();
      baseURIs.add(local);

      CouchbaseClient c = new CouchbaseClient(baseURIs, "default", "");
      for (BeerDTO entry : myTypes) {
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

