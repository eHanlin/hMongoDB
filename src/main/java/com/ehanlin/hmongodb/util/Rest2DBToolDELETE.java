package com.ehanlin.hmongodb.util;

import com.ehanlin.hmongodb.ConnectTool;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

import javax.servlet.http.HttpServletRequest;

public class Rest2DBToolDELETE {

    public static Object executeByRequest(HttpServletRequest request, ConnectTool.ConnectToolDBInstance dbInstance) {
        DBObject query = Rest2DBTool.buildQueryByRequestPath(request);
        String collection = Rest2DBTool.parseCollectionName(request);
        DBCollection coll = dbInstance.getCollection(collection);
        coll.remove(query, WriteConcern.SAFE);
        return true;
    }

}
