// Ex14 -- Get count of Beers by ABV
import com.couchbase.client.CouchbaseClient;


import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BeerAbvCount {
    public static void main(String[] args) throws Exception {
      
    try {

      URI local = new URI("http://localhost:8091/pools");
      List<URI> baseURIs = new ArrayList<URI>();
      baseURIs.add(local);

      CouchbaseClient client = new CouchbaseClient(baseURIs, "default", "");
      View view = client.getView("beers", "beers_count_abv");

      if (view == null) {
          System.err.println("View is null");
          System.exit(0);
       }

       Query query = new Query();

       query.setReduce(true);
       query.setStale(Stale.FALSE);
       query.setDescending(true);
       query.setGroup(true);
       //query.setLimit(15);

       ViewResponse result = client.query(view, query);

       Iterator<ViewRow> itr = result.iterator();
       
       ViewRow row;

       while (itr.hasNext()) {
          row = itr.next();

          System.out.println(String.format("%s: %s",
                  row.getKey(), row.getValue()));

       }
       client.shutdown(3, TimeUnit.SECONDS);      
      
    } catch (Exception e) {
      System.err.println("Error connecting to Couchbase: " + e.getMessage());
      e.printStackTrace();
      System.exit(0);
    }
  System.exit(0);
  }
}

