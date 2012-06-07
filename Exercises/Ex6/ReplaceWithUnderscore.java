// Ex6 -- Replaces the first occurence of space with an Underscore
import com.couchbase.client.CouchbaseClient;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.SerializingTranscoder;
import net.spy.memcached.transcoders.Transcoder;

/**
 *
 * @author rags
 */
public class ReplaceWithUnderscore {

  public static void main(String args[]) {
  
    /**
     * A Transcoder for strings that just delegates to using
     * a SerializingTranscoder.
     */
    class ReplaceFirstSpaceWithUnderscore implements Transcoder<String> {

        final SerializingTranscoder delegate = new SerializingTranscoder();
        
        public boolean asyncDecode(CachedData d) {
            return delegate.asyncDecode(d);
        }

        public String decode(CachedData d) {
            StringBuffer s;
            return (String) delegate.decode(d);
        }

      public CachedData encode(String o) {

        StringBuffer thingToStore = new StringBuffer(o);

        //return delegate.encode(thingToStore);

        int position = thingToStore.indexOf(" ");

        if (position != -1) {

          thingToStore.replace(position, position + 1, "_");

        }
        

        return delegate.encode(thingToStore);
      }

        public int getMaxSize() {
            return delegate.getMaxSize();
        }
        
    }
    

    try {

      Transcoder<String> transcoder = new ReplaceFirstSpaceWithUnderscore();
      
      String aValue = "Madam Bovary";
      
      URI local = new URI("http://localhost:8091/pools");
      List<URI> baseURIs = new ArrayList<URI>();
      baseURIs.add(local);

      CouchbaseClient c = new CouchbaseClient(baseURIs, "default", "");
      c.set("key", 2, aValue, transcoder).get();
      System.out.println(c.get("key"));
      c.shutdown(3, TimeUnit.SECONDS);
      
    } catch (Exception e) {
      System.err.println("Error connecting to Couchbase: " + e.getMessage());
    }
      finally {
      System.exit(0);
    }

  }
}

