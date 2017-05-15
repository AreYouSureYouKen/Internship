/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managers;

import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.HashMap;
import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Named;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.DatastoreImpl;
import org.mongodb.morphia.Morphia;

/**
 *
 * @author Luke
 */
@Named
@Singleton
public class MongoManager {

    private MongoClient mongoClient = null;
    private final Morphia morphia = new Morphia();
    private HashMap<String,AdvancedDatastore> datastores = new HashMap<>();
    String mongoIpAddress = "127.0.0.1";
    Integer mongoPort = 27017;

    @Lock(LockType.READ)
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    @PostConstruct
    public void init() {
        morphia.mapPackage("models");
        mongoClient = new MongoClient(mongoIpAddress, mongoPort);
        
    }
    
    public AdvancedDatastore GetOrCreateDatastore(String storeName)
    {
        if(datastores.containsKey(storeName)) return datastores.get(storeName);
        
        AdvancedDatastore datastore = new DatastoreImpl(morphia,mongoClient,storeName);
        datastores.put(storeName, datastore);
        return datastore;
    }
}
