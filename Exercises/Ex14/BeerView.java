// Ex14 -- Gets the "loaded" Beers!
import com.couchbase.client.CouchbaseClient;


import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;

import com.google.gson.Gson;

import java.io.FileReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BeerView {
    public static void main(String[] args) throws Exception {
    
    // Read the Data
    
    try {

      URI local = new URI("http://localhost:8091/pools");
      List<URI> baseURIs = new ArrayList<URI>();
      baseURIs.add(local);

      CouchbaseClient client = new CouchbaseClient(baseURIs, "default", "");
      View view = client.getView("beers", "beerview");

      if (view == null) {
          System.err.println("View is null");
          System.exit(0);
       }

       Query query = new Query();

       query.setReduce(false);
       query.setStale(Stale.FALSE);
       query.setIncludeDocs(true);
       query.setDescending(true);
       query.setLimit(5);

       ViewResponse result = client.query(view, query);
       // Paginator op = client.paginatedQuery(view, query, 10);

       Iterator<ViewRow> itr = result.iterator();
       
       ViewRow row, row2;

       while (itr.hasNext()) {
          row = itr.next();

          System.out.println(String.format("Key is: %s",
                  row.getKey()));
          
          System.out.println(String.format("Document is: %s",
                  row.getDocument()));

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

