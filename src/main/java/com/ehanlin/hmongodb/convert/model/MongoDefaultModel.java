package com.ehanlin.hmongodb.convert.model;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.CharUtils;
import org.bson.types.ObjectId;

import com.ehanlin.hconvert.ConvertException;
import com.ehanlin.hconvert.annotation.EffectiveScope;
import com.ehanlin.hconvert.annotation.Skip;
import com.ehanlin.hconvert.annotation.WeakReference;
import com.ehanlin.hconvert.annotation.WeakReferencePolicy;
import com.ehanlin.reflect.GenericTool;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

public class MongoDefaultModel<S> extends MongoModelBase<S, Object>{

    @SuppressWarnings("rawtypes")
    @Override
    public Object convert(Object value, Type source, Type target, WeakReferencePolicy weakRef) {
        try{
            if(value == null){
                return null;
            }
            if(value instanceof ObjectId){
                return objectIdCvDb((ObjectId) value, source, target, weakRef);
            }
            if(value instanceof DBRef){
                return dbRefCvDb((DBRef) value, source, target, weakRef);
            }
            if(value instanceof DBObject){
                return dbCvDb((DBObject) value, source, target, weakRef);
            }
            if(value instanceof Boolean){
                return booleanCvDb((Boolean) value, source, target, weakRef);
            }
            if(value instanceof Number){
                return numberCvDb((Number) value, source, target, weakRef);
            }
            if(value instanceof Character){
                return characterCvDb((Character) value, source, target, weakRef);
            }
            if(value instanceof String){
                return stringCvDb((String) value, source, target, weakRef);
            }
            if(value instanceof Date){
                return dateCvDb((Date) value, source, target, weakRef);
            }
            if(value instanceof Collection){
                return collectionCvDb((Collection) value, source, target, weakRef);
            }
            if(value instanceof Map){
                return mapCvDb((Map) value, source, target, weakRef);
            }
            if(value.getClass().isArray()){
                return arrayCvDb(value, source, target, weakRef);
            }
            return objectCvDb(value, source, target, weakRef);
        }catch(Exception e){
            throw new ConvertException(e);
        }
    }
    
    protected Object nullCvDb(Object value, Type source, Type target, WeakReferencePolicy weakRef){
        return null;
    }
    
    protected Object objectIdCvDb(ObjectId value, Type source, Type target, WeakReferencePolicy weakRef){
        return value;
    }
    
    protected Object dbRefCvDb(DBRef value, Type source, Type target, WeakReferencePolicy weakRef){
        return value;
    }
    
    protected Object dbCvDb(DBObject value, Type source, Type target, WeakReferencePolicy weakRef){
        return value;
    }
    
    protected Object booleanCvDb(Boolean value, Type source, Type target, WeakReferencePolicy weakRef){
        return value;
    }
    
    protected Object numberCvDb(Number value, Type source, Type target, WeakReferencePolicy weakRef){
        return value;
    }
    
    protected Object characterCvDb(Character value, Type source, Type target, WeakReferencePolicy weakRef){
        return value;
    }
    
    protected Object stringCvDb(String value, Type source, Type target, WeakReferencePolicy weakRef){
        return value;
    }
    
    protected Date dateCvDb(Date value, Type source, Type target, WeakReferencePolicy weakRef){
        return value;
    }
    
    @SuppressWarnings("rawtypes")
    protected Object collectionCvDb(Collection value, Type source, Type target, WeakReferencePolicy weakRef){
        BasicDBList list = new BasicDBList();
        for(Object item : value){
            list.add(getConverter().convert(item, Object.class, weakRef));
        }
        return list;
    }
    
    @SuppressWarnings("rawtypes")
    protected Object mapCvDb(Map value, Type source, Type target, WeakReferencePolicy weakRef){
        BasicDBObject obj = new BasicDBObject();
        for(Object key : value.keySet()){
            obj.put(key.toString(), getConverter().convert(value.get(key), Object.class, weakRef));
        }
        return obj;
    }
    
    protected Object arrayCvDb(Object value, Type source, Type target, WeakReferencePolicy weakRef){
        BasicDBList list = new BasicDBList();
        Integer size = Array.getLength(value);
        for(Integer i=0 ; i<size ; i++){
            list.add(getConverter().convert(Array.get(value, i), Object.class, weakRef));
        }
        return list;
    }
    
    protected Object objectCvDb(Object value, Type source, Type target, WeakReferencePolicy weakRef) throws IllegalArgumentException, IllegalAccessException{
        if(weakRef.equals(WeakReferencePolicy.IMMEDIATE)){
            return objectCvDbImmeditate(value, source, target);
        }
        
        BasicDBObject obj = new BasicDBObject();
        Field[] fields = value.getClass().getDeclaredFields();
        for(Field field : fields){
            field.setAccessible(true);
            Skip fieldSkip = field.getAnnotation(Skip.class);
            if(fieldSkip != null){
                if(fieldSkip.value().equals(EffectiveScope.CONVERT) || fieldSkip.value().equals(EffectiveScope.ALL)){
                        continue;
                }
            }
            Object fieldValue = field.get(value);
            WeakReference fieldWeakRef = field.getAnnotation(WeakReference.class);
            //如果是間接弱參照中的非 WeakReferencePolicy.NONE 的弱參照且在轉換範圍中，則一律以直接弱參照轉換。
            if(fieldWeakRef != null && !fieldWeakRef.value().equals(WeakReferencePolicy.NONE)){
                if(weakRef.equals(WeakReferencePolicy.MEDIATE)){
                    obj.put(field.getName(), getConverter().convert(fieldValue, Object.class, WeakReferencePolicy.IMMEDIATE));
                }else{
                    obj.put(field.getName(), getConverter().convert(fieldValue, Object.class, fieldWeakRef.value()));
                }
            }else{
                obj.put(field.getName(), getConverter().convert(fieldValue, Object.class, weakRef));
            }
        }
        
        return obj;
    }
    
    protected Object objectCvDbImmeditate(Object value, Type source, Type target){
        return value.toString();
    }
    


    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected S reBoolean(Boolean value, Type source, Type target) {

        Class clazz = GenericTool.getRawClass(source);
        switch(checkSupportType(clazz)){
            case OBJECT:
            case BOOLEAN:
                return (S) value;
            case PRIMITIVE:
                if(Boolean.TYPE.equals(source)) return (S) value;
                if(Byte.TYPE.equals(source)) return (S) (value?new Byte((byte) 1):new Byte((byte) 0));
                if(Short.TYPE.equals(source)) return (S) (value?new Short((short) 1):new Short((short) 0));
                if(Integer.TYPE.equals(source)) return (S) (value?new Integer(1):new Integer(0));
                if(Long.TYPE.equals(source)) return (S) (value?new Long(1L):new Long(0L));
                if(Float.TYPE.equals(source)) return (S) (value?new Float(1.0F):new Float(0.0F));
                if(Double.TYPE.equals(source)) return (S) (value?new Double(1.0):new Double(0.0));
                if(Character.TYPE.equals(source)) return (S) (value?new Character('1'):new Character('0'));
                if(Void.TYPE.equals(source)) throw new ConvertException("Target Type : "+target.toString()+" , Revert to Source Type : "+source.toString()+" , is not support.");
            case NUMBER:
                if(clazz.isAssignableFrom(Byte.class)) return (S) (value?new Byte((byte) 1):new Byte((byte) 0));
                if(clazz.isAssignableFrom(Short.class)) return (S) (value?new Short((short) 1):new Short((short) 0));
                if(clazz.isAssignableFrom(Integer.class)) return (S) (value?new Integer(1):new Integer(0));
                if(clazz.isAssignableFrom(AtomicInteger.class)) return (S) (value?new AtomicInteger(1):new AtomicInteger(0));
                if(clazz.isAssignableFrom(BigInteger.class)) return (S) (value?BigInteger.ONE:BigInteger.ZERO);
                if(clazz.isAssignableFrom(BigDecimal.class)) return (S) (value?BigDecimal.ONE:BigDecimal.ZERO);
                if(clazz.isAssignableFrom(Long.class)) return (S) (value?new Long(1L):new Long(0L));
                if(clazz.isAssignableFrom(AtomicLong.class)) return (S) (value?new AtomicLong(1L):new AtomicLong(0L));
                if(clazz.isAssignableFrom(Float.class)) return (S) (value?new Float(1.0F):new Float(0.0F));
                if(clazz.isAssignableFrom(Double.class)) return (S) (value?new Double(1.0):new Double(0.0));
                break;
            case STRING:
                return (S) value.toString();
            case COLLECTION:
                Collection coll = null;
                try{
                    coll = getCollectionInstance(clazz);
                }catch(Exception e){
                    throw new ConvertException(e);
                }
                if(source instanceof ParameterizedType){
                    coll.add(getConverter().revert(value, GenericTool.getGenericType(source)));
                }else{
                    coll.add(getConverter().revert(value, Object.class));
                }
                return (S) coll;
            case ARRAY:
                Object array = Array.newInstance(clazz.getComponentType(), 1);
                Array.set(array, 0, getConverter().revert(value, clazz.getComponentType()));
                return (S) array;
            case DATE:
            case OBJECT_ID:
            case DB_REF:
            case MAP:
            case DB_OBJECT:
            case NOT_SUPPORT:
                throw new ConvertException("Target Type : "+target.toString()+" , Revert to Source Type : "+source.toString()+" , is not support.");
        }
        
        throw new ConvertException("Target Type : "+target.toString()+" , Revert to Source Type : "+source.toString()+" , is not support.");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected S reNumber(Number value, Type source, Type target) {
        
        Class clazz = GenericTool.getRawClass(source);
        switch(checkSupportType(clazz)){
            case OBJECT:
                return (S) value;
            case BOOLEAN:
                return (value.intValue()==0) ? (S) new Boolean(false) : (S) new Boolean(true);
            case NUMBER:
                if(clazz.isAssignableFrom(Byte.class)) return (S) new Byte(value.byteValue());
                if(clazz.isAssignableFrom(Short.class)) return (S) new Short(value.shortValue());
                if(clazz.isAssignableFrom(Integer.class)) return (S) new Integer(value.intValue());
                if(clazz.isAssignableFrom(AtomicInteger.class)) return (S) new AtomicInteger(value.intValue());
                if(clazz.isAssignableFrom(BigInteger.class)) return (S) BigInteger.valueOf(value.longValue());
                if(clazz.isAssignableFrom(BigDecimal.class)) return (S) new BigDecimal(value.longValue());
                if(clazz.isAssignableFrom(Long.class)) return (S) new Long(value.longValue());
                if(clazz.isAssignableFrom(AtomicLong.class)) return (S) new AtomicLong(value.longValue());
                if(clazz.isAssignableFrom(Float.class)) return (S) new Float(value.floatValue());
                if(clazz.isAssignableFrom(Double.class)) return (S) new Double(value.doubleValue());       
            case PRIMITIVE:
                if(Boolean.TYPE.equals(source)) return (value.intValue()==0) ? (S) new Boolean(false) : (S) new Boolean(true);
                if(Byte.TYPE.equals(source)) return (S) new Byte(value.byteValue());
                if(Short.TYPE.equals(source)) return (S) new Short(value.shortValue());
                if(Integer.TYPE.equals(source)) return (S) new Integer(value.intValue());
                if(Long.TYPE.equals(source)) return (S) new Long(value.longValue());
                if(Float.TYPE.equals(source)) return (S) new Float(value.floatValue());
                if(Double.TYPE.equals(source)) return (S) new Double(value.doubleValue());
                if(Character.TYPE.equals(source)) return (S) new Character((char) value.intValue());
                if(Void.TYPE.equals(source)) throw new ConvertException("Target Type : "+target.toString()+" , Revert to Source Type : "+source.toString()+" , is not support.");
            case CHARACTER:
                return (S) new Character((char) value.intValue());
            case STRING:
                return (S) value.toString();
            case DATE:
                return (S) new Date(value.longValue());
            case COLLECTION:
                Collection coll = null;
                try{
                    coll = getCollectionInstance(clazz);
                }catch(Exception e){
                    throw new ConvertException(e);
                }
                if(source instanceof ParameterizedType){
                    coll.add(getConverter().revert(value, GenericTool.getGenericType(source)));
                }else{
                    coll.add(getConverter().revert(value, Object.class));
                }
                return (S) coll;
            case ARRAY:
                Object array = Array.newInstance(clazz.getComponentType(), 1);
                Array.set(array, 0, getConverter().revert(value, clazz.getComponentType()));
                return (S) array;
            case OBJECT_ID:
            case DB_REF:
            case MAP:
            case DB_OBJECT:
            case NOT_SUPPORT:
                throw new ConvertException("the Source Type : "+source.toString()+" , is not support.");
        }
        
        throw new ConvertException("Target Type : "+target.toString()+" , Revert to Source Type : "+source.toString()+" , is not support.");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected S reString(String value, Type source, Type target) {

        Class clazz = GenericTool.getRawClass(source);
        switch(checkSupportType(clazz)){
            case OBJECT:
                return (S) value;
            case BOOLEAN:
                return (S) new Boolean(value);
            case PRIMITIVE:
                if(Boolean.TYPE.equals(source)) return (S) new Boolean(value);
                if(Byte.TYPE.equals(source)) return (S) new Byte(value);
                if(Short.TYPE.equals(source)) return (S) new Short(value);
                if(Integer.TYPE.equals(source)) return (S) new Integer(value);
                if(Long.TYPE.equals(source)) return (S) new Long(value);
                if(Float.TYPE.equals(source)) return (S) new Float(value);
                if(Double.TYPE.equals(source)) return (S) new Double(value);
                if(Character.TYPE.equals(source)) return (S) new Character(CharUtils.toChar(value));
                if(Void.TYPE.equals(source)) throw new ConvertException("Target Type : "+target.toString()+" , Revert to Source Type : "+source.toString()+" , is not support.");
            case NUMBER:
                if(clazz.isAssignableFrom(Byte.class)) return (S) new Byte(value);
                if(clazz.isAssignableFrom(Short.class)) return (S) new Short(value);
                if(clazz.isAssignableFrom(Integer.class)) return (S) new Integer(value);
                if(clazz.isAssignableFrom(AtomicInteger.class)) return (S) new AtomicInteger(new Integer(value));
                if(clazz.isAssignableFrom(BigInteger.class)) return (S) new BigInteger(value);
                if(clazz.isAssignableFrom(BigDecimal.class)) return (S) new BigDecimal(value);
                if(clazz.isAssignableFrom(Long.class)) return (S) new Long(value);
                if(clazz.isAssignableFrom(AtomicLong.class)) return (S) new AtomicLong(new Long(value));
                if(clazz.isAssignableFrom(Float.class)) return (S) new Float(value);
                if(clazz.isAssignableFrom(Double.class)) return (S) new Double(value);
            case CHARACTER:
                return (S) new Character(CharUtils.toChar(value));
            case STRING:
                return (S) value.toString();
            case DATE:
                try {
                    return (S) DateFormat.getInstance().parseObject(value);
                } catch (Exception e) {
                    throw new ConvertException(e);
                }
            case OBJECT_ID:
                return (S) new ObjectId(value);
            case COLLECTION:
                Collection coll = null;
                try{
                    coll = getCollectionInstance(clazz);
                }catch(Exception e){
                    throw new ConvertException(e);
                }
                if(source instanceof ParameterizedType){
                    coll.add(getConverter().revert(value, GenericTool.getGenericType(source)));
                }else{
                    coll.add(getConverter().revert(value, Object.class));
                }
                return (S) coll;
            case ARRAY:
                Object array = Array.newInstance(clazz.getComponentType(), 1);
                Array.set(array, 0, getConverter().revert(value, clazz.getComponentType()));
                return (S) array;
            case DB_REF:
            case MAP:
            case DB_OBJECT:
            case NOT_SUPPORT:
                throw new ConvertException("the Source Type : "+source.toString()+" , is not support.");
        }
        
        throw new ConvertException("Target Type : "+target.toString()+" , Revert to Source Type : "+source.toString()+" , is not support.");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected S reDate(Date value, Type source, Type target) {
        
        Class clazz = GenericTool.getRawClass(source);
        switch(checkSupportType(clazz)){
            case OBJECT:
            case DATE:
                return (S) value;
            case NUMBER:
                if(clazz.isAssignableFrom(Byte.class)) return (S) new Byte((byte) value.getTime());
                if(clazz.isAssignableFrom(Short.class)) return (S) new Short((short) value.getTime());
                if(clazz.isAssignableFrom(Integer.class)) return (S) new Integer((int) value.getTime());
                if(clazz.isAssignableFrom(AtomicInteger.class)) return (S) new AtomicInteger((int) value.getTime());
                if(clazz.isAssignableFrom(BigInteger.class)) return (S) BigInteger.valueOf(value.getTime());
                if(clazz.isAssignableFrom(BigDecimal.class)) return (S) new BigDecimal(value.getTime());
                if(clazz.isAssignableFrom(Long.class)) return (S) new Long(value.getTime());
                if(clazz.isAssignableFrom(AtomicLong.class)) return (S) new AtomicLong(value.getTime());
                if(clazz.isAssignableFrom(Float.class)) return (S) new Float(value.getTime());
                if(clazz.isAssignableFrom(Double.class)) return (S) new Double(value.getTime());       
            case PRIMITIVE:
                if(Byte.TYPE.equals(source)) return (S) new Byte((byte) value.getTime());
                if(Short.TYPE.equals(source)) return (S) new Short((short) value.getTime());
                if(Integer.TYPE.equals(source)) return (S) new Integer((int) value.getTime());
                if(Long.TYPE.equals(source)) return (S) new Long(value.getTime());
                if(Float.TYPE.equals(source)) return (S) new Float(value.getTime());
                if(Double.TYPE.equals(source)) return (S) new Double(value.getTime());
                if(Boolean.TYPE.equals(source)) throw new ConvertException("Target Type : "+target.toString()+" , Revert to Source Type : "+source.toString()+" , is not support.");
                if(Character.TYPE.equals(source)) throw new ConvertException("Target Type : "+target.toString()+" , Revert to Source Type : "+source.toString()+" , is not support.");
                if(Void.TYPE.equals(source)) throw new ConvertException("Target Type : "+target.toString()+" , Revert to Source Type : "+source.toString()+" , is not support.");
            case STRING:
                return (S) value.toString();
            case COLLECTION:
                Collection coll = null;
                try{
                    coll = getCollectionInstance(clazz);
                }catch(Exception e){
                    throw new ConvertException(e);
                }
                if(source instanceof ParameterizedType){
                    coll.add(getConverter().revert(value, GenericTool.getGenericType(source)));
                }else{
                    coll.add(getConverter().revert(value, Object.class));
                }
                return (S) coll;
            case ARRAY:
                Object array = Array.newInstance(clazz.getComponentType(), 1);
                Array.set(array, 0, getConverter().revert(value, clazz.getComponentType()));
                return (S) array;
            case MAP:
            case DB_OBJECT:
            case BOOLEAN:
            case CHARACTER:
            case OBJECT_ID:
            case DB_REF:
            case NOT_SUPPORT:
                throw new ConvertException("the Source Type : "+source.toString()+" , is not support.");
        }
        
        throw new ConvertException("Target Type : "+target.toString()+" , Revert to Source Type : "+source.toString()+" , is not support.");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected S reObjectId(ObjectId value, Type source, Type target) {
        
        Class clazz = GenericTool.getRawClass(source);
        switch(checkSupportType(clazz)){
            case OBJECT:
            case OBJECT_ID:
                return (S) value;
            case STRING:
                return (S) value.toString();
            case MAP:
                Map map = null;
                try{
                    map = getMapInstance(clazz);
                }catch(Exception e){
                    throw new ConvertException(e);
                }
                if(source instanceof ParameterizedType){
                    Type[] mapTypes = GenericTool.getGenericTypes(source);
                    map.put(getConverter().revert("time", mapTypes[0], String.class), getConverter().revert(value.getTime(), mapTypes[1], Long.TYPE));
                    map.put(getConverter().revert("machine", mapTypes[0], String.class), getConverter().revert(value.getMachine(), mapTypes[1], Integer.TYPE));
                    map.put(getConverter().revert("inc", mapTypes[0], String.class), getConverter().revert(value.getInc(), mapTypes[1], Integer.TYPE));
                }else{
                    map.put(getConverter().revert("time", Object.class, String.class), getConverter().revert(value.getTime(), Object.class, Long.TYPE));
                    map.put(getConverter().revert("machine", Object.class, String.class), getConverter().revert(value.getMachine(), Object.class, Integer.TYPE));
                    map.put(getConverter().revert("inc", Object.class, String.class), getConverter().revert(value.getInc(), Object.class, Integer.TYPE));
                }
                return (S) map;
            case COLLECTION:
                Collection coll = null;
                try{
                    coll = getCollectionInstance(clazz);
                }catch(Exception e){
                    throw new ConvertException(e);
                }
                if(source instanceof ParameterizedType){
                    coll.add(getConverter().revert(value, GenericTool.getGenericType(source)));
                }else{
                    coll.add(getConverter().revert(value, Object.class));
                }
                return (S) coll;
            case ARRAY:
                Object array = Array.newInstance(clazz.getComponentType(), 1);
                Array.set(array, 0, getConverter().revert(value, clazz.getComponentType()));
                return (S) array;
            case BOOLEAN:
            case NUMBER:
            case CHARACTER:
            case DATE:
            case DB_REF:
            case DB_OBJECT:
            case PRIMITIVE:
            case NOT_SUPPORT:
                throw new ConvertException("the Source Type : "+source.toString()+" , is not support.");
        }
        
        throw new ConvertException("Target Type : "+target.toString()+" , Revert to Source Type : "+source.toString()+" , is not support.");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected S reDBRef(DBRef value, Type source, Type target) {
        
        Class clazz = GenericTool.getRawClass(source);
        switch(checkSupportType(clazz)){
            case OBJECT:
            case DB_REF:
                return (S) value;
            case STRING:
                return (S) value.toString();
            case MAP:
                Map map = null;
                try{
                    map = getMapInstance(clazz);
                }catch(Exception e){
                    throw new ConvertException(e);
                }
                
                if(source instanceof ParameterizedType){
                    Type[] mapTypes = GenericTool.getGenericTypes(source);
                    map.put(getConverter().revert("$ref", mapTypes[0], String.class), getConverter().revert(value.getRef(), mapTypes[1], String.class));
                    map.put(getConverter().revert("$id", mapTypes[0], String.class), getConverter().revert(value.getId(), mapTypes[1]));
                    map.put(getConverter().revert("$db", mapTypes[0], String.class), getConverter().revert(value.getDB().getName(), mapTypes[1], String.class));
                }else{
                    map.put(getConverter().revert("$ref", Object.class, String.class), getConverter().revert(value.getRef(), Object.class, String.class));
                    map.put(getConverter().revert("$id", Object.class, String.class), getConverter().revert(value.getId(), Object.class));
                    map.put(getConverter().revert("$db", Object.class, String.class), getConverter().revert(value.getDB().getName(), Object.class, String.class));
                }
                return (S) map;
            case COLLECTION:
                Collection coll = null;
                try{
                    coll = getCollectionInstance(clazz);
                }catch(Exception e){
                    throw new ConvertException(e);
                }
                if(source instanceof ParameterizedType){
                    coll.add(getConverter().revert(value, GenericTool.getGenericType(source)));
                }else{
                    coll.add(getConverter().revert(value, Object.class));
                }
                return (S) coll;
            case ARRAY:
                Object array = Array.newInstance(clazz.getComponentType(), 1);
                Array.set(array, 0, getConverter().revert(value, clazz.getComponentType()));
                return (S) array;
            case BOOLEAN:
            case NUMBER:
            case CHARACTER:
            case DATE:
            case OBJECT_ID:
            case DB_OBJECT:
            case PRIMITIVE:
            case NOT_SUPPORT:
                throw new ConvertException("the Source Type : "+source.toString()+" , is not support.");
        }
        
        throw new ConvertException("Target Type : "+target.toString()+" , Revert to Source Type : "+source.toString()+" , is not support.");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected S reList(List value, Type source, Type target) {
        
        Class clazz = GenericTool.getRawClass(source);
        switch(checkSupportType(clazz)){
            case OBJECT:
                return (S) value;
            case STRING:
                return (S) value.toString();
            case COLLECTION:
                Collection coll = null;
                try{
                    coll = getCollectionInstance(clazz);
                }catch(Exception e){
                    throw new ConvertException(e);
                }
                if(source instanceof ParameterizedType){
                    for(Object item : value){
                        coll.add(getConverter().revert(item, GenericTool.getGenericType(source)));
                    }
                }else{
                    for(Object item : value){
                        coll.add(getConverter().revert(item, Object.class));
                    }
                }
                return (S) coll;
            case ARRAY:
                Object array = Array.newInstance(clazz.getComponentType(), value.size());
                for(Integer i = 0 ; i<value.size() ; i++){
                    Object item = value.get(i);
                    Array.set(array, i, getConverter().revert(item, clazz.getComponentType()));
                }
                return (S) array;
            case BOOLEAN:
            case MAP:
            case DB_OBJECT:
            case NUMBER:
            case DATE:
            case OBJECT_ID:
            case DB_REF:
            case CHARACTER:
            case PRIMITIVE:
            case NOT_SUPPORT:
                throw new ConvertException("the Source Type : "+source.toString()+" , is not support.");
        }
        
        throw new ConvertException("Target Type : "+target.toString()+" , Revert to Source Type : "+source.toString()+" , is not support.");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected S reDBObject(DBObject value, Type source, Type target) {
        
        Class clazz = GenericTool.getRawClass(source);
        switch(checkSupportType(clazz)){
            case OBJECT:
                return (S) value;
            case MAP:
                Map map = null;
                try {
                    map = getMapInstance(clazz);
                } catch (Exception e) {
                    throw new ConvertException(e);
                }
                if(source instanceof ParameterizedType){
                    Type[] mapTypes = GenericTool.getGenericTypes(source);
                    for(String key : value.keySet()){
                        Object item = value.get(key);
                        map.put(getConverter().revert(key, mapTypes[0], String.class), getConverter().revert(item, mapTypes[1]));
                    }
                }else{
                    for(String key : value.keySet()){
                        Object item = value.get(key);
                        map.put(getConverter().revert(key, Object.class, String.class), getConverter().revert(item, Object.class));
                    }
                }
                return (S) map;
            case DB_OBJECT:
                DBObject obj = null;
                try {
                    Constructor constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    obj = (DBObject) constructor.newInstance();
                } catch (Exception e) {
                    throw new ConvertException(e);
                }
                for(String key : value.keySet()){
                    Object item = value.get(key);
                    obj.put(key, getConverter().revert(item, Object.class));
                }
                return (S) obj;
            case STRING:
                return (S) value.toString();
            case COLLECTION:
                Collection coll = null;
                try{
                    coll = getCollectionInstance(clazz);
                }catch(Exception e){
                    throw new ConvertException(e);
                }
                if(source instanceof ParameterizedType){
                    coll.add(getConverter().revert(value, GenericTool.getGenericType(source)));
                }else{
                    coll.add(getConverter().revert(value, Object.class));
                }
                return (S) coll;
            case ARRAY:
                Object array = Array.newInstance(clazz.getComponentType(), 1);
                Array.set(array, 0, getConverter().revert(value, clazz.getComponentType()));
                return (S) array;
            case NOT_SUPPORT:
                //target 是 DBObject 時，情況比較特殊，因為是個類 Map 的物件，所以不管 source 是什麼，都可以試著塞回去。
                Object other = null;
                try {
                    Constructor constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    other = constructor.newInstance();
                } catch (Exception e) {
                    throw new ConvertException(e);
                }
                for(String key : value.keySet()){
                    Object item = value.get(key);
                    try{
                        Field field = clazz.getDeclaredField(key);
                        if(field != null){
                            field.setAccessible(true);
                            Skip fieldSkip = field.getAnnotation(Skip.class);
                            if(fieldSkip != null){
                                if(fieldSkip.value().equals(EffectiveScope.REVERT) || fieldSkip.value().equals(EffectiveScope.ALL)){
                                    continue;
                                }
                            }
                            field.set(other, getConverter().revert(item, field.getGenericType()));
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                return (S) other;
            case DATE:
            case OBJECT_ID:
            case DB_REF:
            case BOOLEAN:
            case NUMBER:
            case CHARACTER:
            case PRIMITIVE:
                throw new ConvertException("the Source Type : "+source.toString()+" , is not support.");
        }
        
        throw new ConvertException("Target Type : "+target.toString()+" , Revert to Source Type : "+source.toString()+" , is not support.");
    }
    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Collection getCollectionInstance(Class clazz) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        if(clazz.isInterface()){
            //TODO 在這裡應該補上各種 Collection 可能的實作。
            return new ArrayList();
        }else{
            Constructor constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (Collection) constructor.newInstance();
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Map getMapInstance(Class clazz) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException{
        if(clazz.isInterface()){
            //TODO 在這裡應該補上各種 Map 可能的實作。
            return new HashMap();
        }else{
            Constructor constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (Map) constructor.newInstance();
        }
    }
    
    
    private static final int NOT_SUPPORT = -1;
    private static final int OBJECT = 0;
    private static final int BOOLEAN = 1;
    private static final int NUMBER = 2;
    private static final int CHARACTER = 3;
    private static final int STRING = 4;
    private static final int DATE = 5;
    private static final int OBJECT_ID = 6;
    private static final int DB_REF = 7;
    private static final int COLLECTION = 8;
    private static final int MAP = 9;
    private static final int DB_OBJECT = 10;
    private static final int ARRAY = 11;
    private static final int PRIMITIVE = 12;
    
    @SuppressWarnings("rawtypes")
    private Integer checkSupportType(Class clazz){
        if(Object.class.equals(clazz)){
            return OBJECT;
        }
        if(Boolean.class.isAssignableFrom(clazz)){
            return BOOLEAN;
        }
        if(Number.class.isAssignableFrom(clazz)){
            return NUMBER;
        }
        if(Character.class.isAssignableFrom(clazz)){
            return CHARACTER;
        }
        if(String.class.isAssignableFrom(clazz)){
            return STRING;
        }
        if(Date.class.isAssignableFrom(clazz)){
            return DATE;
        }
        if(ObjectId.class.isAssignableFrom(clazz)){
            return OBJECT_ID;
        }
        if(DBRef.class.isAssignableFrom(clazz)){
            return DB_REF;
        }
        if(Collection.class.isAssignableFrom(clazz)){
            return COLLECTION;
        }
        if(Map.class.isAssignableFrom(clazz)){
            return MAP;
        }
        if(DBObject.class.isAssignableFrom(clazz)){
            return DB_OBJECT;
        }
        if(clazz.isArray()){
            return ARRAY;
        }
        if(clazz.isPrimitive()){
            return PRIMITIVE;
        }
        return NOT_SUPPORT;
    }
    

}
