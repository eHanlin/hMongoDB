package com.ehanlin.hmongodb.util;

import java.util.Collection;

import com.mongodb.BasicDBList;

/**
 * 和 BasicDBList 相關的操作工具
 */
public class BasicDBListTool {
    
    /**
     * 依傳入的值產生 BasicDBList
     */
    public static BasicDBList create(Object ... items){
        BasicDBList list = new BasicDBList();
        if(items == null){
            return list;
        }
        for(int i=0 ; i<items.length ; i++){
            list.add(items[i]);
        }
        return list;
    }
    
    public static BasicDBList createStringList(Collection<?> collection){
        BasicDBList list = new BasicDBList();
        if(collection == null){
            return list;
        }
        
        for(Object item : collection){
            if(item == null){
                list.add(null);
            }else{
                list.add(item.toString());
            }
        }
        return list;
    }
}
