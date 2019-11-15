package com.ehanlin.hmongodb;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.*;
import org.springframework.core.io.ClassPathResource;

/**
 * 用來統一取得 MongoDB 連線的工具類別
 * 
 * @author hotdog929
 */
public class ConnectTool {
    private static String dbURI = null;
    private static String dbUser = null;
    private static String dbPw = null;
    private static String dbAuthenticationDatabase = null;

    public static ConnectTool instance = new ConnectTool();

    private Integer defaultPort = 27017;
    
    private Lock clientLock = new ReentrantLock();
    private Lock dbLock = new ReentrantLock();
    private Lock collectionLock = new ReentrantLock();
    
    private Map<String, MongoClient> clientMap = new ConcurrentHashMap<String, MongoClient>();
    private Map<String, DB> dbMap = new ConcurrentHashMap<String, DB>();
    private Map<String, DBCollection> collectionMap = new ConcurrentHashMap<String, DBCollection>();

    private ConnectTool(){
        System.out.println("check mongodb-credential.properties");
        ClassPathResource mongodbCredentialResource = new ClassPathResource("mongodb-credential.properties");
        if(mongodbCredentialResource.exists()){
            System.out.println("mongodb-credential.properties existed");
            Properties mongodbCredentialProperties = new Properties();
            try {
                mongodbCredentialProperties.load(mongodbCredentialResource.getInputStream());
                mongodbCredentialProperties = propertiesReplaceByEnv(mongodbCredentialProperties);
                ConnectTool.dbURI = mongodbCredentialProperties.getProperty("uri");
                System.out.println("mongodb-credential.properties uri is " + ConnectTool.dbURI);
                ConnectTool.dbUser = mongodbCredentialProperties.getProperty("user");
                System.out.println("mongodb-credential.properties user is " + ConnectTool.dbUser);
                ConnectTool.dbPw = mongodbCredentialProperties.getProperty("pw");
                System.out.println("mongodb-credential.properties pw is " + ConnectTool.dbPw);
                ConnectTool.dbAuthenticationDatabase = mongodbCredentialProperties.getProperty("authenticationDatabase");
                System.out.println("mongodb-credential.properties authenticationDatabase is " + ConnectTool.dbAuthenticationDatabase);
            } catch (Throwable ex) {

            }
        }
    }

    private Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
    private String replaceByEnv(String value){
        String result = value;
        Matcher matcher = pattern.matcher(result);
        while(matcher.find()){
            result = matcher.replaceFirst(System.getenv(matcher.group(1)));
            matcher = pattern.matcher(result);
        }
        return result;
    }

    private Properties propertiesReplaceByEnv(Properties properties){
        Properties replaced = new Properties();
        Enumeration propertyNames = properties.propertyNames();
        while (propertyNames.hasMoreElements()){
            String key = propertyNames.nextElement().toString();
            String value = properties.getProperty(key);
            replaced.put(key, replaceByEnv(value));
        }
        return replaced;
    }
    
    public MongoClient getClient(String host, Integer port, String authenticationDatabase){
        String key = host+":"+port;
        if(clientMap.containsKey(key)){
            return clientMap.get(key);
        }else{
            clientLock.lock();
            try{
                if(!clientMap.containsKey(key)){
                    if(ConnectTool.dbURI != null){
                        clientMap.put(key, new MongoClient(new MongoClientURI(ConnectTool.dbURI)));
                    }else if(ConnectTool.dbUser != null && ConnectTool.dbPw != null){
                        MongoCredential credential = null;
                        if(authenticationDatabase != null){
                            credential = MongoCredential.createCredential(ConnectTool.dbUser, authenticationDatabase, ConnectTool.dbPw.toCharArray());
                        }
                        else if(ConnectTool.dbAuthenticationDatabase != null){
                            credential = MongoCredential.createCredential(ConnectTool.dbUser, ConnectTool.dbAuthenticationDatabase, ConnectTool.dbPw.toCharArray());
                        }else{
                            credential = MongoCredential.createCredential(ConnectTool.dbUser, "admin", ConnectTool.dbPw.toCharArray());
                        }
                        clientMap.put(key, new MongoClient(new ServerAddress(host, port), Arrays.asList(credential)));
                    }else{
                        clientMap.put(key, new MongoClient(host, port));
                    }
                }    
                return clientMap.get(key);
            }catch(Exception e){
                  return null;  
            }finally{
                clientLock.unlock();
            }
        }
    }

    public MongoClient getClient(String host, Integer port){
        return getClient(host, port, null);
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
