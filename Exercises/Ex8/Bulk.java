// Ex8 -- Bulk Get

import java.net.URI;
import java.util.List;

import com.couchbase.client.CouchbaseClient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Bulk {
  public static void main(String args[]) {
    try {

      URI local = new URI("http://127.0.0.1:8091/pools");
      List<URI> baseURIs = new ArrayList<URI>();
      baseURIs.add(local);

      CouchbaseClient c = new CouchbaseClient(baseURIs, "default", "");
      c.set("white", 30, "Hello White!").get();
      System.out.println(c.get("white"));
      c.set("sox", 30, "Hello Sox!").get();
      System.out.println(c.get("sox"));
     
      // Get as strings
      Map<String, Object> mp=new HashMap<String, Object>();
      mp = c.getBulk("white", "sox");
      System.out.println(mp);
      
      Collection<String> input = new ArrayList<String>();
      
      input.add("white");
      input.add("sox");
      
      // Get as Collection
      mp = c.getBulk(input);
      System.out.println(input);
      
      System.out.println(mp);

      c.shutdown(3, TimeUnit.SECONDS);
      
    } catch (Exception e) {
      System.err.println("Error connecting to Couchbase: " + e.getMessage());
    } finally {
      System.exit(0);
    }

  }
}

