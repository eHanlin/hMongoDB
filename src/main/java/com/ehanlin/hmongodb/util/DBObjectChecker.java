package com.ehanlin.hmongodb.util;

import com.mongodb.DBObject;

/**
 * 用來做一些 DBObject 的檢查工具
 */
public class DBObjectChecker {
    
    /**
     * 用來判斷這個欄位存不存在，且值不是 null
     */
    public static Boolean existedAndNotNull(DBObject dbObj, String field){
        if(dbObj.containsField(field)){
            if(dbObj.get(field) != null){
                return true;
            }
        }            
        return false;
    }
    
    /**
     * 用來判斷這個欄位是不是存在，且等於傳入值
     */
    public static Boolean existedAndEqual(DBObject dbObj, String field, Object value){
        if(dbObj.containsField(field)){
            Object fieldValue = dbObj.get(field);
            if(fieldValue == value){
                return true;
            }
            if(fieldValue != null){
                return fieldValue.equals(value);
            }
        }
        return false;
    }
    
    /**
     * 用來取得欄位值，若欄位不存在，則寫入預設值後傳回預設值
     */
    public static Object getOrPutDefault(DBObject dbObj, String field, Object defaultValue){
        if(dbObj.containsField(field)){
            return dbObj.get(field);
        }
        dbObj.put(field, defaultValue);
        return defaultValue;
    }
}
