package com.ehanlin.hmongodb.jackson;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;


public class MongoObjectMapper extends ObjectMapper {
    
    private static final long serialVersionUID = 7611580427736012094L;
    
    public MongoObjectMapper(){
        this(null, null, null);
    }
    
    public MongoObjectMapper(JsonFactory jf, DefaultSerializerProvider sp, DefaultDeserializationContext dc){
        super(jf, sp, dc);
        
        SimpleModule mongoDbModule = new SimpleModule("MongoDbModule");
        mongoDbModule.addSerializer(ObjectId.class, new ObjectIdJsonSerializer());
        mongoDbModule.addDeserializer(ObjectId.class, new ObjectIdJsonDeserializer());
        
        this.registerModule(mongoDbModule);
    }
}
