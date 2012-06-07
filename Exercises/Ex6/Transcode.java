// Ex6 -- Reverses a String when persisting

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
public class TransCode {

  public static void main(String args[]) {
  
    /**
     * A Transcoder for strings that just delegates to using
     * a SerializingTranscoder.
     */
    class StringTranscoder implements Transcoder<String> {

        final SerializingTranscoder delegate = new SerializingTranscoder();
        
        public boolean asyncDecode(CachedData d) {
            return delegate.asyncDecode(d);
        }

        public String decode(CachedData d) {
            StringBuffer s;
            return (String) delegate.decode(d);
        }

        public CachedData encode(String o) {
            return delegate.encode(new StringBuffer(o).reverse().toString());
        }

        public int getMaxSize() {
            return delegate.getMaxSize();
        }
        
    }
    

    try {

      Transcoder<String> transcoder = new StringTranscoder();
      
      URI local = new URI("http://localhost:8091/pools");
      List<URI> baseURIs = new ArrayList<URI>();
      baseURIs.add(local);

      CouchbaseClient c = new CouchbaseClient(baseURIs, "default", "");
      c.set("key", 2, "Madam", transcoder).get();
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

