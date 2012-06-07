// Ex3 -- Sets a parameter on the CouchbaseConnectionFactoryBuilder

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NoOptimize {

  CouchbaseClient cbc;

    public static void main(String args[]) {
    try {

      URI local = new URI("http://localhost:8091/pools");
      List<URI> baseURIs = new ArrayList<URI>();
      baseURIs.add(local);
      
        
      CouchbaseConnectionFactoryBuilder cfb = new CouchbaseConnectionFactoryBuilder();
      cfb.setShouldOptimize(false);

      CouchbaseClient myclient = new 
              CouchbaseClient(cfb.buildCouchbaseConnection(baseURIs,
                  "default", "", ""));
      
      myclient.set("key", 30, "Hello Chicago!").get();
      System.out.println(myclient.get("key"));
      myclient.shutdown(3, TimeUnit.SECONDS);

    } catch (Exception e) {
          System.err.println("Error connecting to Couchbase: " + e.getMessage());
    }
      finally {
          System.exit(0);
    }
  }
}
