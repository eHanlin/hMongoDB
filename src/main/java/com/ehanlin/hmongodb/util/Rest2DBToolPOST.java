package com.ehanlin.hmongodb.util;

import com.ehanlin.hmongodb.ConnectTool;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import org.bson.types.ObjectId;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public class Rest2DBToolPOST {

    public static Object executeByRequest(HttpServletRequest request, ConnectTool.ConnectToolDBInstance dbInstance) {
        if(request.getContentType().trim().toLowerCase().equals(Rest2DBTool.MIME_JSON)){
            try {
                BasicDBObject updateData = new BasicDBObject(Rest2DBTool.mapper.readValue(request.getReader(), Map.class));
                String _id = null;
                if(updateData.containsField("_id")){
                    _id = updateData.get("_id").toString();
                }else{
                    _id = new ObjectId().toString();
                    updateData.put("_id", _id);
                }
                String collection = Rest2DBTool.parseCollectionName(request);
                DBCollection coll = dbInstance.getCollection(collection);
                coll.insert(updateData, WriteConcern.SAFE);
                return _id;
            } catch (IOException e) {
                throw new IllegalArgumentException("can not read content");
            }
        }else{
            throw new IllegalArgumentException("content type not application/json");
        }
    }

}
