package net.beer.crud;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.google.gson.Gson;

import net.spy.memcached.CASValue;
import net.spy.memcached.CASResponse;
import net.spy.memcached.internal.OperationFuture;

class BeerDTO {
	public String id;
	public String type;
	public String breweryId;
	public String name;
	public float abv;
}

public class CouchbaseHelper {
	private String id;
	private String type;
	private String breweryid;
	private String name;
	private float abv;
	private long cas=0L;
	private String status="sucess";
	private String value=null;
	
    private int totalBeers;
    private List<String> beers = new ArrayList<String>();
    private int totalBreweries;
    private List<String> breweries = new ArrayList<String>();
    private CouchbaseClient client = null;

	public CouchbaseClient open(String host) {
		try {

			URI local = new URI(String.format("http://%s:8091/pools", host));
			List<URI> baseURIs = new ArrayList<URI>();
			baseURIs.add(local);

			client = new CouchbaseClient(baseURIs, "default", "");
		} catch (Exception e) {
			System.err.println("Error connecting to Couchbase: "
					+ e.getMessage());
			e.printStackTrace();
		}
		return client;
	}

    public CouchbaseHelper() {
    	
    	id = null;
    	type="beer";
    	breweryid=null;
    	name=null;
    	abv=0;
    	
    	
        try {

            CouchbaseClient client = open("localhost");
            
            View view = client.getView("beers", "beerview");

            if (view == null) {
                System.err.println("View beers/beerview is null");
             }

             Query query = new Query();

             query.setReduce(false);
             query.setStale(Stale.FALSE);
             query.setIncludeDocs(true);
             query.setDescending(true);
             // query.setLimit(20);

             ViewResponse result = client.query(view, query);

             Iterator<ViewRow> itr = result.iterator();
             
             ViewRow row;

             while (itr.hasNext()) {
                row = itr.next();

                beers.add(row.getKey());

             }
             view = client.getView("beers", "breweryview");

             if (view == null) {
                 System.err.println("View beers/breweryview is null");
              }


              query.setReduce(false);
              query.setStale(Stale.FALSE);
              query.setIncludeDocs(true);
              query.setDescending(true);
              // query.setLimit(20);

              result = client.query(view, query);

              itr = result.iterator();
              
              while (itr.hasNext()) {
                 row = itr.next();
                 breweries.add(row.getKey());
              }            
            
          } catch (Exception e) {
            System.err.println("Error connecting to Couchbase: " + e.getMessage());
            e.printStackTrace();
          }
        totalBreweries = breweries.size();
        totalBeers = beers.size();
    }
 
    public List<String> getBeers(String query) {
        String beer = null;
        query = query.toLowerCase();
        List<String> matched = new ArrayList<String>();
        for(int i=0; i<totalBeers; i++) {
            beer = beers.get(i).toLowerCase();
            if(beer.startsWith(query)) {
                matched.add(beers.get(i));
            }
        }
        return matched;
    }
    
    public List<String> getBreweries(String query) {
        String brewery = null;
        query = query.toLowerCase();
        List<String> matched = new ArrayList<String>();
        for(int i=0; i<totalBreweries; i++) {
            brewery = breweries.get(i).toLowerCase();
            if(brewery.startsWith(query)) {
                matched.add(breweries.get(i));
            }
        }
        return matched;
    }
    
    public void setId(String Id) {

    	String json=null;
    	id = Id;
    	CASValue<Object> casv = client.gets(id);
    	if (casv != null) {
    		json = (String) casv.getValue();
    		cas = casv.getCas();
    	}
    	
    	if (json != null) {
    	    BeerDTO beer = new Gson().fromJson(json, BeerDTO.class);
    	
    	    id = beer.id;
    	    type = beer.type;
    	    breweryid = beer.breweryId;
    	    name = beer.name;
    	    abv = beer.abv;
    	}
    	
    }
    public void setName(String n) {
    	name = n;
    }
    
    public void setBreweryId(String n) {
        breweryid = n;
    }
    
    public void setUpdate(String s) {
    	BeerDTO beer = new BeerDTO();
    	beer.id = id;
    	beer.name = name;
    	beer.type = type;
    	beer.breweryId = breweryid;
    	beer.abv = abv;
    	
    	value = s;
    	
    	String json = new Gson().toJson(beer);
    	
    	Future<CASResponse> setOp = client.asyncCAS(id, cas, json);
    	try {
    	    if (setOp.get().equals(CASResponse.OK)) {
                System.out.println("Update Succeeded");
                status = "success";
            } else {
                System.err.println("Update failed: ");
                status = "Update failed due to modification by other client (CAS failure)";
            }
    	} catch (Exception e) {
    		status = e.getMessage();
    	}
    }
    
    public void setDelete(String s) {
    	BeerDTO beer = new BeerDTO();
    	beer.id = id;
    	beer.name = name;
    	beer.type = type;
    	beer.breweryId = breweryid;
    	beer.abv = abv;
    	
    	value = s;
    	
    	String json = new Gson().toJson(beer);
    	
    	OperationFuture<Boolean> delOp = client.delete(id);
    	try {
    	    if (delOp.get() == true) {
                System.out.println("Delete Succeeded");
                status = "success";
            } else {
                System.err.println("Delete failed: ");
                status = delOp.getStatus().getMessage();
            }
    	} catch (Exception e) {
    		status = e.getMessage();
    	}
    }
 
    public void setCreate(String s) {
    	BeerDTO beer = new BeerDTO();
    	beer.id = id;
    	beer.name = name;
    	beer.type = type;
    	beer.breweryId = breweryid;
    	beer.abv = abv;
    	
    	value = s;
    	
    	String json = new Gson().toJson(beer);
    	
    	OperationFuture<Boolean> setOp = client.set(id, 3600, json);
    	try {
    	    if (setOp.get() == true) {
                System.out.println("Set Succeeded");
                status = "success";
            } else {
                System.err.println("Set failed: ");
                status = setOp.getStatus().getMessage();
            }
    	} catch (Exception e) {
    		status = e.getMessage();
    	}
    }
    public void setAbv(String n) {
    	abv = Float.valueOf(n);
    }
    
    public void setAbv(float f) {
    	abv = f;
    }
    
    public String getId() {
    	return id;
    }
    public String getName() {
    	return name;
    }

    public String getBreweryId() {
    	return breweryid;
    }
    public float getAbv() {
    	return abv;
    }
    public String getStatus() {
    	return status;
    }

}
