package edu.stanford.smi.protege.server;

import java.rmi.*;

import edu.stanford.smi.protege.util.*;

/**
 * TODO Class Comment
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class Shutdown {

    public static void main(String[] args) {
        try {
            String host = (args.length == 0) ? "localhost" : args[0];
            String name = "//" + host + "/" + Server.getBoundName();
            Log.getLogger().info("Attempting to shut down " + name);
            RemoteServer server = (RemoteServer) Naming.lookup(name);
            server.shutdown();
            Log.getLogger().info("The server has shut down.");
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
        }
    }
}