package com.ehanlin.hmongodb.jackson;

import java.io.IOException;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class ObjectIdJsonDeserializer extends JsonDeserializer<ObjectId> {

    @Override
    public ObjectId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        try{
            JsonToken t = jp.getCurrentToken();
            if(t == JsonToken.VALUE_STRING){
                return new ObjectId(jp.getValueAsString());
            }
        }catch(Exception e){
            
        }
        return null;
    }

}
