package com.ehanlin.hmongodb.util;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;

import com.ehanlin.hmongodb.ConnectTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

public class Rest2DBTool {
    
    static ObjectMapper mapper = new ObjectMapper();

    public final static String MIME_JSON = "application/json";
    
    
    public static Object executeByRequest(HttpServletRequest request, ConnectTool.ConnectToolDBInstance dbInstance){
        switch (request.getMethod().toUpperCase()){
            case "GET" :
                return Rest2DBToolGET.executeByRequest(request, dbInstance);
            case "PATCH" :
                return Rest2DBToolPATCH.executeByRequest(request, dbInstance);
            case "DELETE" :
                return Rest2DBToolDELETE.executeByRequest(request, dbInstance);
            case "PUT" :
                return Rest2DBToolPUT.executeByRequest(request, dbInstance);
            case "POST" :
                return Rest2DBToolPOST.executeByRequest(request, dbInstance);
                
            default : 
                throw new IllegalArgumentException("Not suport HTTP method");
            
        }
    }


    public static String parseCollectionName(HttpServletRequest request){
        String[] paths = request.getServletPath().split("/");
        return ((paths.length % 2) == 0) ? paths[paths.length-1] : paths[paths.length-2];
    }

    public static Pattern objectIdRegex = Pattern.compile("^[0-9a-f]{24}$");
    public static Object convertPathValue(String pathValue){
        
        try{
            pathValue = java.net.URLDecoder.decode(pathValue, "UTF-8").trim();
        }catch(Exception e){
            
        }

        Matcher matcher  = objectIdRegex.matcher(pathValue);
        if(matcher.matches()){
            return pathValue;
        }
        
        try{
            return Long.parseLong(pathValue);
        }catch(Exception e){
            
        }
        
        try{
            return Double.parseDouble(pathValue);
        }catch(Exception e){

        }
        
        try{
            if(pathValue.toLowerCase().equals("true")){
                return Boolean.TRUE;
            }
            if(pathValue.toLowerCase().equals("false")){
                return Boolean.FALSE;
            }
        }catch(Exception e){
            
        }
        
        if(pathValue.indexOf(",") >=0){
            String[] values = pathValue.split(",");
            BasicDBList result = new BasicDBList();
            for(String value : values){
                result.add(convertPathValue(value));
            }
            return result;
        }
        
        return pathValue;
    }


    public static DBObject buildQueryByRequestPath(HttpServletRequest request) {
        String path = request.getServletPath();
        String[] paths = path.split("/");
        QueryBuilder queryBuilder = QueryBuilder.start();

        if((paths.length % 2) == 0){
            for(int i=1 ; i<paths.length-1 ; i+=2){
                String key = paths[i];
                Object value = Rest2DBTool.convertPathValue(paths[i+1]);
                if(value instanceof BasicDBList){
                    queryBuilder.and(key).in(value);
                }else{
                    queryBuilder.and(key).is(value);
                }
            }
        }else{
            for(int i=1 ; i<paths.length-2 ; i+=2){
                String key = paths[i];
                Object value = Rest2DBTool.convertPathValue(paths[i+1]);
                if(value instanceof BasicDBList){
                    queryBuilder.and(key).in(value);
                }else{
                    queryBuilder.and(key).is(value);
                }
            }
        }

        return queryBuilder.get();
    }


        @Deprecated
    public static DBObject pageByQuery(
            DBObject query, String collection,
            String includeString, String excludeString, String embedString,
            String skipString, String limitString, String sortString,
            ConnectTool.ConnectToolDBInstance dbInstance)
    {
        return Rest2DBToolGET.pageByQuery(query, collection, includeString, excludeString, embedString, skipString, limitString, sortString, dbInstance);
    }
        
    @Deprecated
    public static List<DBObject> findByQuery(
        DBObject query, String collection,
        String includeString, String excludeString, String embedString,
        String skipString, String limitString, String sortString,
        ConnectTool.ConnectToolDBInstance dbInstance)
    {
        return Rest2DBToolGET.findByQuery(query, collection, includeString, excludeString, embedString, skipString, limitString, sortString, dbInstance);
    }
    
    @Deprecated
    public static DBObject pageByString(
        String query, String collection,
        String includeString, String excludeString, String embedString,
        String skipString, String limitString, String sortString,
        ConnectTool.ConnectToolDBInstance dbInstance)
    {
        return Rest2DBToolGET.pageByString(query, collection, includeString, excludeString, embedString, skipString, limitString, sortString, dbInstance);
    }
    
    @Deprecated
    public static List<DBObject> findByString(
        String query, String collection,
        String includeString, String excludeString, String embedString,
        String skipString, String limitString, String sortString,
        ConnectTool.ConnectToolDBInstance dbInstance)
    {
        return Rest2DBToolGET.findByString(query, collection, includeString, excludeString, embedString, skipString, limitString, sortString, dbInstance);
    }
    
    @Deprecated
    public static DBObject findById(
        String id, String collection,
        String includeString, String excludeString, String embedString,
        String skipString, String limitString, String sortString,
        ConnectTool.ConnectToolDBInstance dbInstance)
    {
        return Rest2DBToolGET.findById(id, collection, includeString, excludeString, embedString, skipString, limitString, sortString, dbInstance);
    }
    
    @Deprecated
    public static List<DBObject> findByIds(
        Collection<String> ids, String collection,
        String includeString, String excludeString, String embedString,
        String skipString, String limitString, String sortString,
        ConnectTool.ConnectToolDBInstance dbInstance)
    {
        return Rest2DBToolGET.findByIds(ids, collection, includeString, excludeString, embedString, skipString, limitString, sortString, dbInstance);
    }
    
    @Deprecated
    public static Object findByRequest(HttpServletRequest request, ConnectTool.ConnectToolDBInstance dbInstance) 
    {
        return Rest2DBToolGET.executeByRequest(request, dbInstance);
    }
    
    @Deprecated
    public static Object findByQueryAndRequest(DBObject query, HttpServletRequest request, ConnectTool.ConnectToolDBInstance dbInstance) 
    {
        return Rest2DBToolGET.findByQueryAndRequest(query, request, dbInstance);
    }
    
    @Deprecated
    public static void embedByString(DBObject obj, String embedString, String includeString, String excludeString, ConnectTool.ConnectToolDBInstance dbInstance){
        Rest2DBToolGET.embedByString(obj, embedString, includeString, excludeString, dbInstance);
    }
    
    @Deprecated
    public static void embedByString(Collection<DBObject> objs, String embedString, String includeString, String excludeString, ConnectTool.ConnectToolDBInstance dbInstance){
        Rest2DBToolGET.embedByString(objs, embedString, includeString, excludeString, dbInstance);
    }
    
    @Deprecated
    public static void embed(DBObject obj, Object embed, Object include, Object exclude, ConnectTool.ConnectToolDBInstance dbInstance){
        Rest2DBToolGET.embed(obj, embed, include, exclude, dbInstance);
    }
    
    @Deprecated
    public static DBObject buildFieldKeys(Object include, Object exclude){
        return Rest2DBToolGET.buildFieldKeys(include, exclude);
    }

    @Deprecated
    public static DBObject buildQueryByRequest(HttpServletRequest request){
        return Rest2DBToolGET.buildQueryByRequest(request);
    }
    
}
