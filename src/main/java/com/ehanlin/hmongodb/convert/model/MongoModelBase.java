package com.ehanlin.hmongodb.convert.model;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.ehanlin.hconvert.ConvertException;
import com.ehanlin.hconvert.Converter;
import com.ehanlin.hconvert.Model;
import com.ehanlin.hconvert.annotation.WeakReferencePolicy;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

public abstract class MongoModelBase<S, T> implements Model<S, T> {

    protected Converter converter = null;
    
    @Override
    public T convert(Object value, Type target){
        return convert(value, value.getClass(), target, WeakReferencePolicy.NONE);
    }
    @Override
    public T convert(Object value, Type source, Type target){
        return convert(value, source, target, WeakReferencePolicy.NONE);
    }
    @Override
    public T convert(Object value, Type target, WeakReferencePolicy weakRef){
        return convert(value, value.getClass(), target, weakRef);
    }
    @Override
    public abstract T convert(Object value, Type source, Type target, WeakReferencePolicy weakRef);


    @Override
    public S revert(Object value, Type source) {
        return revert(value, source, value.getClass());
    }
    @Override
    @SuppressWarnings("rawtypes")
    public S revert(Object value, Type source, Type target){
        try{
            if(value == null){
                return reNull(value, source, target);
            }
            if(value instanceof Boolean){
                return reBoolean((Boolean) value, source, target);
            }
            if(value instanceof Number){
                return reNumber((Number) value, source, target);
            }
            if(value instanceof String){
                return reString((String) value, source, target);
            }
            if(value instanceof Date){
                return reDate((Date) value, source, target);
            }
            if(value instanceof ObjectId){
                return reObjectId((ObjectId) value, source, target);
            }
            if(value instanceof DBRef){
                return reDBRef((DBRef) value, source, target);
            }
            if(value instanceof List){
                return reList((List) value, source, target);
            }
            if(value instanceof DBObject){
                return reDBObject((DBObject) value, source, target);
            }
            throw new ConvertException("the Target Type : "+target.toString()+" , is not support.");
        }catch(Exception e){
           throw new ConvertException(e);
        }
    }
    
    
    protected S reNull(Object value, Type source, Type target){
        return null;
    }
    
    @SuppressWarnings("unchecked")
    protected S reBoolean(Boolean value, Type source, Type target){
        return (S) value;
    }
    
    @SuppressWarnings("unchecked")
    protected S reNumber(Number value, Type source, Type target){
        return (S) value;
    }
    
    @SuppressWarnings("unchecked")
    protected S reString(String value, Type source, Type target){
        return (S) value;
    }
    
    @SuppressWarnings("unchecked")
    protected S reDate(Date value, Type source, Type target){
        return (S) value;
    }
    
    @SuppressWarnings("unchecked")
    protected S reObjectId(ObjectId value, Type source, Type target){
        return (S) value;
    }
    
    @SuppressWarnings("unchecked")
    protected S reDBRef(DBRef value, Type source, Type target){
        return (S) value;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected S reList(List value, Type source, Type target){
        return (S) value;
    }
    
    @SuppressWarnings("unchecked")
    protected S reDBObject(DBObject value, Type source, Type target){
        return (S) value;
    }

    @Override
    public Converter getConverter() {
        return converter;
    }

    @Override
    public void setConverter(Converter converter) {
        this.converter = converter;
    }

}
