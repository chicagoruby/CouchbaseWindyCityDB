// Ex7 -- Run an instance of this program
// Then run another instance of this program and
// notice CAS mismatches

/**
 *
 * @author rags
 */
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;


import com.couchbase.client.CouchbaseClient;
import java.util.ArrayList;
import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;


public class Consumer {
  static final String KEY = "Chicago";
  static final int MAX = 12;

  public static void main(String args[]) {
    // Set the URIs and get a client

    List<URI> uris = new LinkedList<URI>();

    Random random = new Random();
    int value = 0;
    int i=1;

    uris.add(URI.create("http://localhost:8091/pools"));

    CouchbaseClient client = null;
    
    try {
      client = new CouchbaseClient(uris, "default", "");
    } catch (Exception e) {
      System.err.println("Error connecting to Couchbase: " + e.getMessage());
      System.exit(0);
    }
    
    System.out.println("Client: " + client);
    // Set the key
    value = random.nextInt(MAX);
    System.out.println("Value is " + value);
    client.set(KEY, 0, i);
    
    do {
      // Get the CAS Value
      CASValue<Object> casv = client.gets(KEY);
      // System.out.println("Cas really before sleep: " + casv);
      
      // Sleep for a random time between getting a CAS Value and set
      
      try {
        int sleepTime = random.nextInt(5000);
        // System.out.println("Sleeping for " + sleepTime);
        Thread.sleep(sleepTime);
      } catch (Exception e) {
        System.err.println("Exception while doing sleep: " + e.getMessage());
      }

      Future<CASResponse> setOp =
              client.asyncCAS(KEY, casv.getCas(), random.nextInt(MAX));
      
      // Wait for the CAS Response
      try {
        if (setOp.get().equals(CASResponse.OK)) {
          System.out.println("Set Succeeded");
        } else {
          System.err.println("Set failed: ");
        }
      } catch (Exception e) {
        System.err.println("Exception while doing set: " + e.getMessage());
      }

 
    } while (true); // forever
  }
}
