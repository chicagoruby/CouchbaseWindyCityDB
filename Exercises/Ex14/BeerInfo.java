// Ex14 -- Gets the "loaded" Beers and Breweries!
/**
 *
 * @author rags
 */

import com.couchbase.client.CouchbaseClient;


import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;

import com.google.gson.Gson;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BeerInfo {
    public static void main(String[] args) throws Exception {
    
    try {

      URI local = new URI("http://localhost:8091/pools");
      List<URI> baseURIs = new ArrayList<URI>();
      baseURIs.add(local);

      CouchbaseClient client = new CouchbaseClient(baseURIs, "default", "");
      View view = client.getView("beers", "beers");

      if (view == null) {
          System.err.println("View is null");
          System.exit(0);
       }

       Query query = new Query();

       query.setReduce(false).setLimit(9);
       query.setIncludeDocs(true);
       query.setStale(Stale.FALSE);
       query.setDescending(true);
       

       ViewResponse result = client.query(view, query);
       // Paginator op = client.paginatedQuery(view, query, 10);

       Iterator<ViewRow> itr = result.iterator();
       
       ViewRow row, row2;

       while (itr.hasNext()) {
          row = itr.next();
          String doc = (String) row.getDocument();
          // System.out.println("doc = " + doc);
          BeerDTO beer = new Gson().fromJson(doc, BeerDTO.class);

          System.out.println(String.format("%s: %s %s %f", beer.id,
                  beer.name, beer.breweryId, beer.abv));

          View view2 = client.getView("breweries", "breweries");
          
          Query query2 = new Query();
          
          // You can set all properties on a single statement
          query2.setIncludeDocs(true);
          query2.setStale(Stale.FALSE);
          
          // query2.setRange(beer.breweryId, beer.breweryId);
          query2.setKey(beer.breweryId);
          
          ViewResponse result2 = client.query(view2, query2);

          Iterator<ViewRow> itr2 = result2.iterator();
       
          while (itr2.hasNext()) {
             row2 = itr2.next();
             doc = (String) row2.getDocument();
             BreweryDTO brewery = new Gson().fromJson(doc, BreweryDTO.class);
             System.err.println(String.format("BREWERY: %s %s %s", brewery.id, 
                     brewery.name, brewery.state));
          }

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

