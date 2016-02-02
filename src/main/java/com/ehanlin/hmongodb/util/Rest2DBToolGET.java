package com.ehanlin.hmongodb.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.ehanlin.hmongodb.ConnectTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.util.JSON;

public class Rest2DBToolGET {

    private static String fieldCollectionCode = "_coll";
    private static String fieldQueryCode = "_query";
    private static Pattern referenceValuePattern = Pattern.compile("^@(.+)");
    
    private static ObjectMapper mapper = Rest2DBTool.mapper;

    @SuppressWarnings("rawtypes")
    private static Map emptyMap = Collections.emptyMap();
    
    private static Pattern beforePattern = Pattern.compile("^before-(.+)$");
    private static Pattern afterPattern = Pattern.compile("^after-(.+)$");
    private static Pattern fromPattern = Pattern.compile("^from-(.+)$");
    private static Pattern toPattern = Pattern.compile("^to-(.+)$");

    private static Pattern dotPattern = Pattern.compile("[^\\.]+");

    private static Integer defaultLimit = 1000;
    
    
    /**
     * 綱址只能是 /key/value/.../Colleciton/action?include={}&exclude={}&embed={}&skip=0&limit=10
     * value的值若有包含 , 號則會被解析成 BasicDBList 並使用 $in 來做查詢，
     * 若全是數字則會被解析成 Long，
     * 若有包含 . 則會被解析成 Double。
     * action 是或有選項。
     * include 和 exclude 不能同時作用，若都有值，只使用 include，二者都是 json 格式。
     * embed 也是 json 格式其值可以為 true, 或另一個 json object。
     * sort 也是 json 格式其值為 1 或 -1。
     * pageable 預設是 false，若是 false 回傳 List<DBObject>，若是 true 回傳 DBObject {list:List<DBObject>, total:Integer, skip:Integer, limit:Integer}
     * before- prefix 後接要用來比較的欄位,用來處理在現在時間之前的比較, > currentDate
     * after- prefix 後接要用來比較的欄位,用來處理在現在時間之後的比較, < currentDate
     * from- prefix 後接要用來比較的欄位,用來處理在現在時間之後的比較, <= currentDate
     * to- prefix 後接要用來比較的欄位,用來處理在現在時間之前的比較, >= currentDate
     */
    public static Object executeByRequest(HttpServletRequest request, ConnectTool.ConnectToolDBInstance dbInstance) 
    {
        String includeString = request.getParameter("include");
        String excludeString = request.getParameter("exclude");
        String embedString = request.getParameter("embed");
        String skipString = request.getParameter("skip");
        String limitString = request.getParameter("limit");
        String sortString = request.getParameter("sort");
        String pageableString = request.getParameter("pageable");
        
        Boolean pageable = false;
        try{
            pageable = Boolean.parseBoolean(pageableString);
        }catch(Exception e){
            
        }
        
        String collection = Rest2DBTool.parseCollectionName(request);
        
        DBObject query = buildQueryByRequest(request);
        
        if(pageable){
            return pageByQuery(query, collection, includeString, excludeString, embedString, skipString, limitString, sortString, dbInstance);
        }
        
        return findByQuery(query, collection, includeString, excludeString, embedString, skipString, limitString, sortString, dbInstance);
    }
    
    
    @SuppressWarnings("rawtypes")
    public static DBObject pageByQuery(
            DBObject query, String collection,
            String includeString, String excludeString, String embedString,
            String skipString, String limitString, String sortString,
            ConnectTool.ConnectToolDBInstance dbInstance)
    {
        DBObject keys = new BasicDBObject();
        
        Map include = null;
        Map exclude = null;
        
        if(includeString != null){
            try {
                include = mapper.readValue(includeString, Map.class);
                for(Object key : include.keySet()){
                    keys.put(key.toString(), true);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Rest2DBTool includeString error");
            }
        }else if(excludeString != null){
            try {
                exclude = mapper.readValue(excludeString, Map.class);
                for(Object key : exclude.keySet()){
                    keys.put(key.toString(), false);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Rest2DBTool excludeString error");
            }
        }
        
        DBCollection coll = dbInstance.getCollection(collection);
        DBCursor cursor = coll.find(query, keys);
        Integer total = cursor.count();
        Integer skip = null;
        Integer limit = null;
        
        try{
            if(sortString != null){
                cursor.sort((DBObject)JSON.parse(sortString));
            }
        }catch(Exception e){
        }
        
        try{
            skip = Integer.parseInt(skipString);
            cursor.skip(skip);
        }catch(Exception e){
        }
        
        try{
            limit = Integer.parseInt(limitString);
            cursor.limit(limit);
        }catch(Exception e){
            cursor.limit(defaultLimit);
        }
        
        List<DBObject> result = cursor.toArray();
        cursor.close();
        
        Map embed = null;
        try {
            if(embedString != null)
            embed = mapper.readValue(embedString, Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Rest2DBTool embedString error");
        }

        Map<String, DBObject> findOneCache = new ConcurrentHashMap<String, DBObject>();
        Map<String, List<DBObject>> findCache = new ConcurrentHashMap<String, List<DBObject>>();

        for(DBObject obj : result){
            embed(obj, embed, include, exclude, dbInstance, findOneCache, findCache);
        }
        
        BasicDBObjectBuilder resultBuilder =  BasicDBObjectBuilder.start()
            .add("list", result)
            .add("total", total);
        if(skip != null){
            resultBuilder.add("skip", skip);
        }
        if(limit != null){
            resultBuilder.add("limit", limit);
        }
        
        return resultBuilder.get();
    }
    
    @SuppressWarnings("unchecked")
    public static List<DBObject> findByQuery(
        DBObject query, String collection,
        String includeString, String excludeString, String embedString,
        String skipString, String limitString, String sortString,
        ConnectTool.ConnectToolDBInstance dbInstance)
    {
        return (List<DBObject>) pageByQuery(query, collection, includeString, excludeString, embedString, skipString, limitString, sortString, dbInstance).get("list");
    }
    
    public static DBObject pageByString(
        String query, String collection,
        String includeString, String excludeString, String embedString,
        String skipString, String limitString, String sortString,
        ConnectTool.ConnectToolDBInstance dbInstance)
    {
        return pageByQuery((DBObject)JSON.parse(query), collection, includeString, excludeString, embedString, skipString, limitString, sortString, dbInstance);
    }
    
    public static List<DBObject> findByString(
            String query, String collection,
            String includeString, String excludeString, String embedString,
            String skipString, String limitString, String sortString,
            ConnectTool.ConnectToolDBInstance dbInstance)
        {
            return findByQuery((DBObject)JSON.parse(query), collection, includeString, excludeString, embedString, skipString, limitString, sortString, dbInstance);
        }
    
    public static DBObject findById(
        String id, String collection,
        String includeString, String excludeString, String embedString,
        String skipString, String limitString, String sortString,
        ConnectTool.ConnectToolDBInstance dbInstance)
    {
        return findByQuery(new BasicDBObject("_id", id), collection, includeString, excludeString, embedString, skipString, limitString, sortString, dbInstance).get(0);
    }
    
    public static List<DBObject> findByIds(
        Collection<String> ids, String collection,
        String includeString, String excludeString, String embedString,
        String skipString, String limitString, String sortString,
        ConnectTool.ConnectToolDBInstance dbInstance)
    {
        List<DBObject> result = new ArrayList<DBObject>();
        for(String id : ids){
            result.add(findById(id, collection, includeString, excludeString, embedString, skipString, limitString, sortString, dbInstance));
        }
        return result;
    } 
    
    public static Object findByQueryAndRequest(DBObject query, HttpServletRequest request, ConnectTool.ConnectToolDBInstance dbInstance) 
    {
        String path = request.getServletPath();
        String includeString = request.getParameter("include");
        String excludeString = request.getParameter("exclude");
        String embedString = request.getParameter("embed");
        String skipString = request.getParameter("skip");
        String limitString = request.getParameter("limit");
        String sortString = request.getParameter("sort");
        String pageableString = request.getParameter("pageable");
        
        Boolean pageable = false;
        try{
            pageable = Boolean.parseBoolean(pageableString);
        }catch(Exception e){
            
        }
        
        String[] paths = path.split("/");
        String collection = ((paths.length % 2) == 0) ? paths[paths.length-1] : paths[paths.length-2];
        
        if(pageable){
            return pageByQuery(query, collection, includeString, excludeString, embedString, skipString, limitString, sortString, dbInstance);
        }
        
        return findByQuery(query, collection, includeString, excludeString, embedString, skipString, limitString, sortString, dbInstance);
    }

    @SuppressWarnings("rawtypes")
    public static void embedByString(DBObject obj, String embedString, String includeString, String excludeString, ConnectTool.ConnectToolDBInstance dbInstance, Map<String, DBObject> findOneCache, Map<String, List<DBObject>> findCache){
        if(embedString == null)
            return;

        Map embed = null;
        Map include = null;
        Map exclude = null;

        try {
            embed = mapper.readValue(embedString, Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Rest2DBTool embedString error");
        }

        if(includeString != null){
            try {
                include = mapper.readValue(includeString, Map.class);
            } catch (Exception e) {
                throw new IllegalArgumentException("Rest2DBTool includeString error");
            }
        }else if(excludeString != null){
            try {
                exclude = mapper.readValue(excludeString, Map.class);
            } catch (Exception e) {
                throw new IllegalArgumentException("Rest2DBTool excludeString error");
            }
        }

        embed(obj, embed, include, exclude, dbInstance, findOneCache, findCache);
    }
    

    public static void embedByString(DBObject obj, String embedString, String includeString, String excludeString, ConnectTool.ConnectToolDBInstance dbInstance){
        embedByString(obj, embedString, includeString, excludeString, dbInstance, new ConcurrentHashMap<String, DBObject>(), new ConcurrentHashMap<String, List<DBObject>>());
    }


    public static void embedByString(Collection<DBObject> objs, String embedString, String includeString, String excludeString, ConnectTool.ConnectToolDBInstance dbInstance, Map<String, DBObject> findOneCache, Map<String, List<DBObject>> findCache){
        for(DBObject obj : objs){
            embedByString(obj, embedString, includeString, excludeString, dbInstance, findOneCache, findCache);
        }
    }

    public static void embedByString(Collection<DBObject> objs, String embedString, String includeString, String excludeString, ConnectTool.ConnectToolDBInstance dbInstance){
        embedByString(objs, embedString, includeString, excludeString, dbInstance, new ConcurrentHashMap<String, DBObject>(), new ConcurrentHashMap<String, List<DBObject>>());
    }


    public static DBObject findOneWithCache(DBCollection coll, DBObject query, DBObject fieldKeys, Map<String, DBObject> cache) {
        try{
            String key = coll.getFullName()+query.toString()+fieldKeys.toString();
            if(!cache.containsKey(key)){
                DBObject result = coll.findOne(query, fieldKeys);
                cache.put(key, result);
            }
            return (DBObject) ((BasicDBObject) cache.get(key)).copy();
        }catch(Exception ex){
            return null;
        }
    }

    public static List<DBObject> findWithCache(DBCollection coll, DBObject query, DBObject fieldKeys, Map<String, List<DBObject>> cache) {
        try{
            String key = coll.getFullName()+query.toString()+fieldKeys.toString();
            if(!cache.containsKey(key)){
                List<DBObject> list = coll.find(query, fieldKeys).toArray();
                cache.put(key, list);
            }
            List<DBObject> list = cache.get(key);
            List<DBObject> result = new ArrayList<DBObject>();
            for(DBObject item : list){
                result.add((DBObject) ((BasicDBObject) item).copy());
            }
            return result;
        }catch(Exception ex){
            return null;
        }
    }

    public static void embed(DBObject obj, Object embed, Object include, Object exclude, ConnectTool.ConnectToolDBInstance dbInstance, Map<String, DBObject> findOneCache, Map<String, List<DBObject>> findCache) {
        if(obj != null && embed != null && embed instanceof Map){
            Map embedMap = (Map) embed;
            Map includeMap = (include != null && include instanceof Map) ? (Map)include : emptyMap;
            Map excludeMap = (exclude != null && exclude instanceof Map) ? (Map)exclude : emptyMap;

            Set<String> queryKeySet = new HashSet<String>();

            for(Object key : embedMap.keySet()){
                if(!key.toString().equals(fieldCollectionCode) && !key.toString().equals(fieldQueryCode)){

                    String collectionName = StringUtils.capitalize(key.toString());
                    if(embedMap.get(key) instanceof Map){
                        Map childMap = (Map) embedMap.get(key);
                        if(childMap.containsKey(fieldCollectionCode)){
                            collectionName = childMap.get(fieldCollectionCode).toString();
                        }
                        if(childMap.containsKey(fieldQueryCode) && childMap.get(fieldQueryCode) instanceof Map){
                            queryKeySet.add(key.toString());
                            continue;
                        }
                    }
                    DBCollection coll = dbInstance.getCollection(collectionName);

                    DBObject fieldKeys = buildFieldKeys(includeMap.get(key), excludeMap.get(key));

                    if(obj.containsField(key.toString())){
                        if(obj.get(key.toString()) instanceof Collection){
                            Collection sourceList = (Collection) obj.get(key.toString());
                            BasicDBList resultList = new BasicDBList();
                            for(Object source : sourceList){
                                if(source instanceof String){
                                    DBObject sourceObj = findOneWithCache(coll, new BasicDBObject("_id", source.toString()), fieldKeys, findOneCache);
                                    embed(sourceObj, embedMap.get(key), includeMap.get(key), excludeMap.get(key), dbInstance, findOneCache, findCache);
                                    resultList.add(sourceObj);    
                                }else if(source instanceof DBObject){
                                    embed((DBObject) source, embedMap.get(key), includeMap.get(key), excludeMap.get(key), dbInstance, findOneCache, findCache);
                                    resultList.add(source);
                                }
                            }
                            obj.put(key.toString(), resultList);
                        }else{
                            DBObject sourceObj = findOneWithCache(coll, new BasicDBObject("_id", obj.get(key.toString()).toString()), fieldKeys, findOneCache);
                            embed(sourceObj, embedMap.get(key), includeMap.get(key), excludeMap.get(key), dbInstance, findOneCache, findCache);
                            obj.put(key.toString(), sourceObj);
                        }
                    }
                }
            }

            for(String key : queryKeySet){

                String collectionName = StringUtils.capitalize(key);

                Map childMap = (Map) embedMap.get(key);
                if(childMap.containsKey(fieldCollectionCode)){
                    collectionName = childMap.get(fieldCollectionCode).toString();
                }

                DBCollection coll = dbInstance.getCollection(collectionName);

                DBObject fieldKeys = buildFieldKeys(includeMap.get(key), excludeMap.get(key));

                Map queryMap = (Map) childMap.get(fieldQueryCode);
                QueryBuilder queryBuilder = QueryBuilder.start();
                for(Object queryKey : queryMap.keySet()){
                    Object queryValue = null;
                    if(queryMap.get(queryKey) instanceof String){
                        String referenceValue = (String) queryMap.get(queryKey);
                        Matcher matcher = referenceValuePattern.matcher(referenceValue);
                        if(matcher.matches()){
                            queryValue = obj;
                            Matcher dotMatcher = dotPattern.matcher(matcher.group(1));
                            while(dotMatcher.find()){
                                queryValue = ((DBObject)queryValue).get(dotMatcher.group());
                            }
                        }else{
                            queryValue = queryMap.get(queryKey);
                        }
                    }else{
                        queryValue = queryMap.get(queryKey);
                    }
                    queryBuilder.and(queryKey.toString()).is(queryValue);
                }
                DBObject query = queryBuilder.get();

                List<DBObject> resultList = findWithCache(coll, query, fieldKeys, findCache);
                for(DBObject resultObj : resultList){
                    embed(resultObj, embedMap.get(key), includeMap.get(key), excludeMap.get(key), dbInstance, findOneCache, findCache);
                }
                obj.put(key.toString(), resultList);
            }

        }
    }
    
    /**
     * 副作用注意，將會改變傳入的 obj 的狀態，填入自動寫入的值。
     */
    @SuppressWarnings("rawtypes")
    public static void embed(DBObject obj, Object embed, Object include, Object exclude, ConnectTool.ConnectToolDBInstance dbInstance){
        embed(obj, embed, include, exclude, dbInstance, new ConcurrentHashMap<String, DBObject>(), new ConcurrentHashMap<String, List<DBObject>>());
    }

    /**
     * 副作用注意，將會改變傳入的 obj 的狀態，填入自動寫入的值。
     */
    public static void embed(List<DBObject> objs, Object embed, Object include, Object exclude, ConnectTool.ConnectToolDBInstance dbInstance){
        Map<String, DBObject> findOneCache = new ConcurrentHashMap<String, DBObject>();
        Map<String, List<DBObject>> findCache = new ConcurrentHashMap<String, List<DBObject>>();
        for(DBObject obj : objs){
            embed(obj, embed, include, exclude, dbInstance, findOneCache, findCache);
        }
    }

    @SuppressWarnings("rawtypes")
    public static DBObject buildFieldKeys(Object include, Object exclude){
        
        Map includeMap = (include != null && include instanceof Map) ? (Map)include : null;
        Map excludeMap = (exclude != null && exclude instanceof Map) ? (Map)exclude : null;
        
        DBObject keys = new BasicDBObject();
        if(includeMap != null){
            for(Object key : includeMap.keySet()){
                keys.put(key.toString(), true);
            }
        }else if(excludeMap != null){
            for(Object key : excludeMap.keySet()){
                keys.put(key.toString(), false);
            }
        }
        return keys;
    }
    
    
    public static DBObject buildQueryByRequest(HttpServletRequest request){
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
        
        
        Enumeration<String> parameters = request.getParameterNames();
        while(parameters.hasMoreElements()){
            String parameter = parameters.nextElement();
            
            Matcher beforeMatcher = beforePattern.matcher(parameter);
            if(beforeMatcher.matches()){
                queryBuilder.and(beforeMatcher.group(1)).greaterThan(new Date());
                continue;
            }
            
            Matcher afterMatcher = afterPattern.matcher(parameter);
            if(afterMatcher.matches()){                
                queryBuilder.and(afterMatcher.group(1)).lessThan(new Date());
                continue;
            }
            
            Matcher fromMatcher = fromPattern.matcher(parameter);
            if(fromMatcher.matches()){
                queryBuilder.and(fromMatcher.group(1)).lessThanEquals(new Date());
                continue;
            }
            
            Matcher toMatcher = toPattern.matcher(parameter);
            if(toMatcher.matches()){
                queryBuilder.and(toMatcher.group(1)).greaterThanEquals(new Date());
                continue;
            }
        }
        
        return queryBuilder.get();
    }
}
