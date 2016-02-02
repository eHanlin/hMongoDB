package com.ehanlin.hmongodb.util;

import com.ehanlin.hmongodb.ConnectTool;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public class Rest2DBToolPUT {

    public static Object executeByRequest(HttpServletRequest request, ConnectTool.ConnectToolDBInstance dbInstance){
        if(request.getContentType().trim().toLowerCase().indexOf(Rest2DBTool.MIME_JSON) >= 0){
            try {
                Map updateData = Rest2DBTool.mapper.readValue(request.getReader(), Map.class);
                DBObject query = Rest2DBTool.buildQueryByRequestPath(request);
                String collection = Rest2DBTool.parseCollectionName(request);
                DBCollection coll = dbInstance.getCollection(collection);
                coll.update(query, new BasicDBObject(updateData), true, false, WriteConcern.SAFE);
            } catch (IOException e) {
                throw new IllegalArgumentException("can not read content");
            }
        }else{
            throw new IllegalArgumentException("content type not application/json");
        }

        return true;
    }

}
