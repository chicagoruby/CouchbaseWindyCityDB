// Ex8 -- The Presidents Data must be loaded prior to running this
import java.net.URI;
import java.util.List;

import com.couchbase.client.CouchbaseClient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.spy.memcached.internal.BulkFuture;

public class AsyncBulk {
  public static void main(String args[]) {
    try {

      URI local = new URI("http://127.0.0.1:8091/pools");
      List<URI> baseURIs = new ArrayList<URI>();
      baseURIs.add(local);

      CouchbaseClient c = new CouchbaseClient(baseURIs, "default", "");

      // Get as strings
      Map<String, Object> mp=new HashMap<String, Object>();
      
      Collection<String> input = new ArrayList<String>();
      
      // Get the Presidents
      
      for (int i=1; i <= 45; i++)
        input.add(String.format("%d", i));
      
      // Get as Collection
      BulkFuture<Map<String, Object>> bf  = c.asyncGetBulk(input);
      
      System.out.println(input);
      
      mp = bf.get();
      System.out.println(mp);

      c.shutdown(3, TimeUnit.SECONDS);
    } catch (Exception e) {
      System.err.println("Error connecting to Couchbase: " + e.getMessage());
    } finally {
      System.exit(0);
    }

  }
}

