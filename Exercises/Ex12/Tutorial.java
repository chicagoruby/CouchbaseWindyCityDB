// Run this from the command line by including the Couchbase Client
// JARs and all its dependencies

import java.io.IOException;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import net.spy.memcached.CASMutation;
import net.spy.memcached.CASMutator;
import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.CachedData;
import net.spy.memcached.ConnectionObserver;
import net.spy.memcached.tapmessage.ResponseMessage;
import net.spy.memcached.transcoders.SerializingTranscoder;
import net.spy.memcached.transcoders.Transcoder;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactory;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;
import com.couchbase.client.TapClient;
import java.util.Scanner;


public class Tutorial {

    private static CouchbaseClient client;
    private long userId = 0;
    private long userCount = 0;
    private Thread messageThread;

    /**
     * Main Program for a Couchbase chat room application using
     * the couchbase-client and spymemcached libraries.
     */
    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("usage: serveraddresses");
            System.exit(1);
        }

        try {
            
            new Tutorial().run(args[0]);
            
        } catch (Exception ex) {
            System.err.println(ex);
            client.shutdown();
        }
    }
    
    public void run(String serverAddress) throws Exception {

        System.setProperty("net.spy.log.LoggerImpl",
            "net.spy.memcached.compat.log.SunLogger");

        System.out.println(String
                .format("Connecting to %s", serverAddress));

        connect(serverAddress);
        
        tap(serverAddress);

        if (register()) {
            startMessageThread();
            Runtime.getRuntime().addShutdownHook(new unregisterThread());
            processInput();
            unregister();
            messageThread.interrupt();
        }
        client.shutdown(1, TimeUnit.MINUTES);
        System.exit(0);
    }

    /**
     * Tap demonstration method. Shows how to instantiate a TAP client
     * and use it to query for keys.
     * 
     * @param serverAddress
     */
    private void tap(String serverAddress) throws Exception {
        
        URI base = new URI(String.format("http://%s:8091/pools",serverAddress));
        List<URI> uris = new LinkedList<URI>();
        uris.add(base);
        
        TapClient tapClient = new TapClient(uris, "default", "");
        
        tapClient.tapDump(""); 
       
        while(tapClient.hasMoreMessages()) {
            ResponseMessage response = tapClient.getNextMessage();
            if (response != null) {
                System.out.println(response.getKey() + " -> " 
                        + new String (response.getValue()));
            }
        }
        
        tapClient.shutdown();
    }

    /**
     * Connect to the server, or servers given.
     * @param serverAddress the server addresses to connect with.
     * @throws IOException if there is a problem with connecting.
     * @throws URISyntaxException 
     * @throws ConfigurationException 
     */
    private void connect(String serverAddress) throws Exception {

        URI base = new URI(
                String.format("http://%s:8091/pools",serverAddress));
        //URI fallback = new URI(
        //        String.format("http://%s:8091/pools","192.168.3.104"));
        List<URI> baseURIs = new ArrayList<URI>();
        baseURIs.add(base);
        // baseURIs.add(fallback);
        CouchbaseConnectionFactoryBuilder cfb = new
                CouchbaseConnectionFactoryBuilder();
        // wait up to 10 seconds for an operation to succeed
        cfb.setOpTimeout(10000);

        CouchbaseConnectionFactory cf = 
            cfb.buildCouchbaseConnection(baseURIs, "default", "", "");        
        //         new 
        //        CouchbaseConnectionFactory(baseURIs, "default", "");
        
        client = new CouchbaseClient(cf);
       
        client.addObserver(new ConnectionObserver() {

        @Override
        public void connectionLost(SocketAddress sa) {
          System.out.println("Connection lost to " + sa.toString());
        }

        @Override
        public void connectionEstablished(SocketAddress sa,
                int reconnectCount) {
          System.out.println("Connection established with "
                  + sa.toString());
          System.out.println("Reconnected count: " + reconnectCount);
        }
      });
    }

    /**
     * Get a user name token for the current user.
     * @return the token to use.
     */
    private String getUserNameToken() {
        return String.format("<User-%d>", userId);
    }

    /**
     * Register the user with the chat room.
     * @return true if the registration succeeded, false otherwise.
     */
    private boolean register() throws Exception {

        userId = client.incr("UserId", 1, 1);
        System.out.println("You are user " + userId + ".");

        CASValue<Object> casValue = client.gets("CurrentUsers");

        if (casValue == null) {

            System.out.println("First user ever!");

            client.set("CurrentUsers", Integer.MAX_VALUE,
                    getUserNameToken()).get();

        } else {

            Future<Boolean> appendDone = client.append(casValue.getCas(),
                    "CurrentUsers", getUserNameToken());

            if (appendDone.get()) {
                System.out.println("Registration succeeded.");
            } else {
                System.out.println("Sorry registration failed.");
                return false;
            }

        }

        userCount = client.incr("UserCount", 1, 1);
        System.out.println("There are currently " + userCount
                + " connected.");

        return true;
    }

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
            return (String)delegate.decode(d);
        }

        public CachedData encode(String o) {
            return delegate.encode(o);
        }

        public int getMaxSize() {
            return delegate.getMaxSize();
        }
        
    }
    
    /**
     * Unregister the current user from the chat room.
     */
    private void unregister() throws Exception {

        CASMutation<String> mutation = new CASMutation<String>() { 
            public String getNewValue(String current) {
                return current.replaceAll(getUserNameToken(), "");
            }
        };
        
        Transcoder<String> transcoder = new StringTranscoder();
        CASMutator<String> mutator = new CASMutator<String>(client, transcoder);
        mutator.cas("CurrentUsers", null, 0, mutation);
        
        client.decr("UserCount", 1);
        System.out.println("Unregistered.");

    }

    /**
     * Print a number of messages.
     * @param startId the first message id to output.
     * @param endId the last message id to output.
     */
    private void printMessages(long startId, long endId) {

        for (long i = startId; i <= endId; i++) {
            String message = (String)client.get("Message:" + i);
            if (message != null)
                System.out.println(message);
        }

    }

    /**
     * Finds the first message id that has not yet expired.
     * @param currentId the last message id to start with.
     * @return the first message id known in the system at the time.
     */
    private long findFirstMessage(long currentId) {
        CASValue<Object> cas = null;
        long firstId = currentId;
        do {
            firstId -= 1;
            cas = client.gets("Message:" + firstId);
        } while (cas != null);

        return firstId + 1;
    }

    /**
     * Start up the message display thread.
     */
    private void startMessageThread() {

        messageThread = new Thread(new Runnable() {
            public void run() {

                long messageId = -1;
                CASValue<Object> prevCas = null;

                try {
                    while (!Thread.interrupted()) {

                        CASValue<Object> msgCountCas = client
                                .gets("Messages");
                        
                        CASValue<Object> newCas = null;
                    
                        newCas = client.gets("CurrentUsers");
                    
                        if ((prevCas == null) || 
                                (newCas.getCas() != prevCas.getCas())) {
                            System.out.println("Users: " + client.get("CurrentUsers"));
                        }
                        prevCas = newCas;
                        

                        if (msgCountCas == null) {
                            Thread.sleep(250);
                            continue;
                        }

                        long current = Long.parseLong((String)msgCountCas
                                .getValue());

                        if (messageId == -1) {
                            printMessages(findFirstMessage(current),
                                    current);
                        } else if (current > messageId) {
                            printMessages(messageId + 1, current);
                        } else {
                            Thread.sleep(250);
                            continue;
                        }

                        messageId = current;

                    }


                } catch (InterruptedException ex) {
                } catch (RuntimeException ex) {
                }

                System.out.println("Stopped message thread.");
            }
        });
        
        messageThread.start();
    }
   
    /**
     * Handle shutdown by unregistering
     */
    private class unregisterThread extends Thread {

    
    public void run() {
      try {

        unregister();
        messageThread.interrupt();
        client.shutdown(1, TimeUnit.MINUTES);
        super.run();;
      } catch (Exception e) {
      }
    }
  }
    
    /**
     * Processes input from the user, and sends messages to the virtual
     * chat room.
     */
    private void processInput() {
        boolean quit = false;

        System.out.
          println("Enter text, or /who to see user list, or /quit to exit.");

        try {
        do {
            String input = System.console().readLine();
            System.out.println(input);
            if (input.startsWith("/quit")) {
                quit = true;
            } else if (input.startsWith("/who")) {
                System.out.println("Users connected: "
                        + client.get("CurrentUsers"));
            } else if (input.startsWith("/cas")) {
                runCasTest();
            } else if (input.startsWith("/user")) {
                Scanner scan = new Scanner(input);
                String prompt = scan.next();

                System.out.println(client.get("Messages:" +
                        (scan.hasNextInt() ? "<User-" + scan.nextInt() + ">" :
                        getUserNameToken())));
            } else {
                // Send a new message to the chat
                long messageId = client.incr("Messages", 1, 1);
                client.set("Message:" + messageId, 3600,
                        getUserNameToken() + ": " + input);
                List<String> messages;
                messages = (ArrayList<String>) 
                        client.get("Messages:" + getUserNameToken());
                if (messages == null) {
                    messages = new ArrayList<String>();
                }
                messages.add(getUserNameToken() + ": " + input);
                client.set("Messages:" + getUserNameToken(), 3600, messages).get();
            }

        } while (!quit);
        } catch (Exception e) {
          e.printStackTrace();
        }
     
    }
    
    private void runCasTest() {
        System.out.println("Testing a CAS operation.");
        CASValue<Object> cas = client.gets("CasTest");
        
        if (cas == null) {
            // Must create it first
            System.out.println("Creating CasTest value.");
            client.set("CasTest", 120, "InitialValue");
            return;
        }
        
        System.out.println("CAS for CasTest = "+cas.getCas());
        System.out.println("Sleeping for 10 seconds.");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
        }
        
        CASResponse response = 
            client.cas("CasTest", cas.getCas(), "ReplacedValue");
        if (response.equals(CASResponse.OK)) {
            System.out.println("OK response.");
        }
        else if (response.equals(CASResponse.EXISTS)) {
            System.out.println("EXISTS response.");
        }
        else if (response.equals(CASResponse.NOT_FOUND)) {
            System.out.println("NOT_FOUND response.");
        }
        
        cas = client.gets("CasTest");
        System.err.println("CAS after = "+cas.getCas());
    }
}

