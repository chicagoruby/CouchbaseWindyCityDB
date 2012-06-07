// Ex13 -- Programmatically does Logging
import java.net.URI;
import java.util.List;

import com.couchbase.client.CouchbaseClient;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloLog {

  public static void main(String args[]) {
    // Set the URIs and get a client
    // Tell spy to use the SunLogger
    Properties systemProperties = System.getProperties();
    systemProperties.put("net.spy.log.LoggerImpl",
            "net.spy.memcached.compat.log.SunLogger");
    System.setProperties(systemProperties);

    Logger topLogger = java.util.logging.Logger.getLogger("");

    // Handler for console (reuse it if it already exists)
    Handler consoleHandler = null;
    //see if there is already a console handler
    for (Handler handler : topLogger.getHandlers()) {
      if (handler instanceof ConsoleHandler) {
        //found the console handler
        consoleHandler = handler;
        break;
      }
    }

    if (consoleHandler == null) {
      //there was no console handler found, create a new one
      consoleHandler = new ConsoleHandler();
      topLogger.addHandler(consoleHandler);
    }

    //set the console handler to fine:
    consoleHandler.setLevel(java.util.logging.Level.FINEST);

    Logger.getLogger("net.spy.memcached").setLevel(Level.FINEST);
    Logger.getLogger("com.couchbase.client").setLevel(Level.FINEST);

    try {

      URI local = new URI("http://localhost:8091/pools");
      List<URI> baseURIs = new ArrayList<URI>();
      baseURIs.add(local);

      CouchbaseClient c = new CouchbaseClient(baseURIs, "default", "");
      c.set("key", 30, "Hello Training!").get();
      System.out.println(c.get("key"));
      System.out.println("Status of Set " + 
          c.set("key", 30, "Goodbye Training!").getStatus().getMessage());
      c.shutdown(3, TimeUnit.SECONDS);
    } catch (Exception e) {
      System.err.println("Error connecting to Couchbase: " + e.getMessage());
    } finally {
      System.exit(0);
    }

  }
}

