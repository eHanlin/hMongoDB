package com.ehanlin.hmongodb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

/**
 * 用來統一取得 MongoDB 連線的工具類別
 * 
 * @author hotdog929
 */
public class ConnectTool {
    public static ConnectTool instance = new ConnectTool();
    
    private Integer defaultPort = 27017;
    
    private Lock clientLock = new ReentrantLock();
    private Lock dbLock = new ReentrantLock();
    private Lock collectionLock = new ReentrantLock();
    
    private Map<String, MongoClient> clientMap = new ConcurrentHashMap<String, MongoClient>();
    private Map<String, DB> dbMap = new ConcurrentHashMap<String, DB>();
    private Map<String, DBCollection> collectionMap = new ConcurrentHashMap<String, DBCollection>();
    
    public MongoClient getClient(String host, Integer port){
        String key = host+":"+port;
        if(clientMap.containsKey(key)){
            return clientMap.get(key);
        }else{
            clientLock.lock();
            try{
                if(!clientMap.containsKey(key)){
                    clientMap.put(key, new MongoClient(host, port));
                }    
                return clientMap.get(key);
            }catch(Exception e){
                  return null;  
            }finally{
                clientLock.unlock();
            }
        }
    }
    
    public MongoClient getClient(String host){
        return getClient(host, defaultPort);
    }
    
    public DB getDB(String host, Integer port, String dbName){
        String key = host+":"+port+":"+dbName;
        if(dbMap.containsKey(key)){
            return dbMap.get(key);
        }else{
            dbLock.lock();
            try{
                if(!dbMap.containsKey(key)){
                    dbMap.put(key, getClient(host, port).getDB(dbName));
                }
                return dbMap.get(key);
            }catch(Exception e){
                return null;
            }finally{
                dbLock.unlock(); 
            }
        }
    }
    
    public DB getDB(String host, String dbName){
        return getDB(host, defaultPort, dbName);
    }
    
    public DBCollection getCollection(String host, Integer port, String dbName, String collectionName){
        String key = host+":"+port+":"+dbName+":"+collectionName;
        if(collectionMap.containsKey(key)){
            return collectionMap.get(key);
        }else{
            collectionLock.lock();
            try{
                if(!collectionMap.containsKey(key)){
                    collectionMap.put(key, getDB(host, port, dbName).getCollection(collectionName));
                }
                return collectionMap.get(key);
            }catch(Exception e){
                return null;
            }finally{
                collectionLock.unlock(); 
            }
        }
    }
    
    public DBCollection getCollection(String host, String dbName, String collectionName){
        return getCollection(host, defaultPort, dbName, collectionName);
    } 
    
    public static class ConnectToolDBInstance{
        private String host = null;
        private Integer port = null;
        private String dbName = null;
        
        public ConnectToolDBInstance(String host, Integer port, String dbName){
            this.host = host;
            this.port = port;
            this.dbName = dbName;
        }
        
        public DBCollection getCollection(String collectionName){
            return ConnectTool.instance.getCollection(host, port, dbName, collectionName);
        }
    }
}
