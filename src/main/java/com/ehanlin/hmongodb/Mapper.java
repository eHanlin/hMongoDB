package com.ehanlin.hmongodb;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

/**
 * 用來把 mongodb 傳回的值映射到對應的 Class 中。
 * 內容值只能是類別 String, Boolean, Integer, Long, Float, Double, Byte, Short, List<Object>, List<Map<String, Object>>, Map<String, Object>。
 * @author hotdog929
 */
public class Mapper {
    
    public static Mapper instance = new Mapper(); 
    
    /**
     * 僅處理 Number, Boolean, String,
     * ObjectId 可變 String 或 ObjectId。
     * DBRef 可變 String(id欄位) 、 Map 或 DBRef。
     * Map 、 List 不轉換，直接回傳。
     * 若要轉成的是 String ，在傳入值非上述所述之類別，將直接以 .toString() 傳回傳。
     * 若發生錯誤，或是無法轉換，將回傳 null 值。
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T dbValue2JavaValue(Object value, Class<T> modelClass){
        try{
            if(value instanceof Boolean){
                Boolean bool = (Boolean) value;
                if(String.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(String.class).newInstance(bool.toString());
                }else if(Boolean.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(boolean.class).newInstance(bool);
                }else if(Integer.class.isAssignableFrom(modelClass)){
                    return (bool)
                            ?modelClass.getDeclaredConstructor(int.class).newInstance(1)
                            :modelClass.getDeclaredConstructor(int.class).newInstance(0);
                }else if(Long.class.isAssignableFrom(modelClass)){
                    return (bool)
                            ?modelClass.getDeclaredConstructor(long.class).newInstance(1L)
                            :modelClass.getDeclaredConstructor(long.class).newInstance(0L);
                }else if(Float.class.isAssignableFrom(modelClass)){
                    return (bool)
                            ?modelClass.getDeclaredConstructor(float.class).newInstance(1.0F)
                            :modelClass.getDeclaredConstructor(float.class).newInstance(0.0F);
                }else if(Double.class.isAssignableFrom(modelClass)){
                    return (bool)
                            ?modelClass.getDeclaredConstructor(double.class).newInstance(1.0)
                            :modelClass.getDeclaredConstructor(double.class).newInstance(0.0);
                }else if(Byte.class.isAssignableFrom(modelClass)){
                    return (bool)
                            ?modelClass.getDeclaredConstructor(byte.class).newInstance(0x1)
                            :modelClass.getDeclaredConstructor(byte.class).newInstance(0x0);
                }else if(Short.class.isAssignableFrom(modelClass)){
                    return (bool)
                            ?modelClass.getDeclaredConstructor(short.class).newInstance(1)
                            :modelClass.getDeclaredConstructor(short.class).newInstance(0);
                }
            }else if(value instanceof Number){
                Number number = (Number) value;
                if(String.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(String.class).newInstance(number.toString());
                }else if(Boolean.class.isAssignableFrom(modelClass)){
                    return (number.intValue()==0)
                            ?modelClass.getDeclaredConstructor(boolean.class).newInstance(false)
                            :modelClass.getDeclaredConstructor(boolean.class).newInstance(true);
                }else if(Integer.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(int.class).newInstance(number.intValue());
                }else if(Long.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(long.class).newInstance(number.longValue());
                }else if(Float.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(float.class).newInstance(number.floatValue());
                }else if(Double.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(double.class).newInstance(number.doubleValue());
                }else if(Byte.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(byte.class).newInstance(number.byteValue());
                }else if(Short.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(short.class).newInstance(number.shortValue());
                }
            }else if(value instanceof String){
                String string = (String) value;
                if(String.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(String.class).newInstance(string);
                }else if(Boolean.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(String.class).newInstance(string);
                }else if(Integer.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(String.class).newInstance(string);
                }else if(Long.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(String.class).newInstance(string);
                }else if(Float.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(String.class).newInstance(string);
                }else if(Double.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(String.class).newInstance(string);
                }else if(Byte.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(String.class).newInstance(string);
                }else if(Short.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(String.class).newInstance(string);
                }
            }else if(value instanceof ObjectId){
                ObjectId objectId = (ObjectId) value;
                if(String.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(String.class).newInstance(objectId.toString());
                }else if(ObjectId.class.isAssignableFrom(modelClass)){
                    return (T) value;
                }
            }else if(value instanceof DBRef){
                DBRef dbRef = (DBRef) value;
                if(String.class.isAssignableFrom(modelClass)){
                    return modelClass.getDeclaredConstructor(String.class).newInstance(dbRef.getId().toString());
                }else if(Map.class.isAssignableFrom(modelClass)){
                    Map map = (Map) modelClass.getDeclaredConstructor().newInstance();
                    map.put("db", dbRef.getDatabaseName());
                    map.put("ref", dbRef.getCollectionName());
                    map.put("id", dbRef.getId());
                    return (T) map;
                }else if(DBRef.class.isAssignableFrom(modelClass)){
                    return (T) value;
                }
            }else if(value instanceof Map){
                if(Map.class.isAssignableFrom(modelClass)){
                    return (T) value;
                }
            }else if(value instanceof List){
                if(List.class.isAssignableFrom(modelClass)){
                    return (T) value;
                }
            }
            
            if(String.class.isAssignableFrom(modelClass)){
                return (T) value.toString();
            }
        }catch(Exception e){
            
        }
        
        return null;
    }
    
    /**
     * 把 Java 物件轉成 mongodb 物件。
     * 值不轉換，只有在 _id 欄位，若是為 null 就直接不寫入 _id 欄位。
     */
    @SuppressWarnings("rawtypes")
    public DBObject model2DbObject(Object model){
        if(model == null){
            return null;
        }
        DBObject result = new BasicDBObject();
        if(model instanceof Map){
            Map map = (Map) model;
            for(Object key : map.keySet()){
                if(key.equals("_id")){
                    Object idFieldData = map.get(key);
                    if(idFieldData != null){
                        result.put("_id", idFieldData);
                    }
                }else{
                    result.put(key.toString(), map.get(key));
                }
            }
        }else{
            Field[] fields = model.getClass().getFields();
            for(int i=0 ; i<fields.length ; i++){
                try {
                    Field field = fields[i];
                    field.setAccessible(true);
                    if(field.getName().equals("_id")){
                        Object idFieldData = field.get(model);
                        if(idFieldData != null){
                            result.put("_id", idFieldData);
                        }
                    }else{
                        result.put(field.getName(), field.get(model));
                    }
                } catch (Exception e) {
                    
                }
            }
        }
        return result;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map model2Map(Object model){
        if(model == null){
            return null;
        }
        if(model instanceof Map){
            return (Map) model;
        }
        Map result = new ConcurrentHashMap();
        Field[] fields = model.getClass().getFields();
        for(int i=0 ; i<fields.length ; i++){
            try {
                Field field = fields[i];
                field.setAccessible(true);
                if(field.getName().equals("_id")){
                    Object idFieldData = field.get(model);
                    if(idFieldData != null){
                        result.put("_id", idFieldData);
                    }
                }else{
                    result.put(field.getName(), field.get(model));
                }
            } catch (Exception e) {
                
            }
        }
        return result;
    }
    
    /**
     * 把 mongodb 物件轉成 java 物件。
     */
    public <T> T dbObject2Model(DBObject data, Class<T> modelClass) 
    {
        if(data == null){
            return null;
        }
        try {
            T instance = modelClass.getConstructor().newInstance();
            for(String key : data.keySet()){
                try{
                    Field field = modelClass.getDeclaredField(key);
                    field.setAccessible(true);
                    field.set(instance, dbValue2JavaValue(data.get(key), field.getType()));
                }catch (Exception ex){
                    
                }
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @SuppressWarnings("rawtypes")
    public <T> T map2Model(Map data, Class<T> modelClass)
    {
        if(data == null){
            return null;
        }
        try {
            T instance = modelClass.getConstructor().newInstance();
            for(Object key : data.keySet()){
                try{
                    Field field = modelClass.getDeclaredField(key.toString());
                    field.setAccessible(true);
                    field.set(instance, dbValue2JavaValue(data.get(key), field.getType()));
                }catch (Exception ex){
                    
                }
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
