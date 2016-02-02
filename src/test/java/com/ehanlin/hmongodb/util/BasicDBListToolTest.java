package com.ehanlin.hmongodb.util;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.mongodb.BasicDBList;

public class BasicDBListToolTest {

    @Test
    public void testCreate(){
        BasicDBList dbList = BasicDBListTool.create();
        Assert.assertEquals(dbList.size(), 0);
        
        dbList = BasicDBListTool.create("item1");
        Assert.assertEquals(dbList.size(), 1);
        Assert.assertEquals(dbList.get(0), "item1");
        
        dbList = BasicDBListTool.create("item1", "item2");
        Assert.assertEquals(dbList.size(), 2);
        Assert.assertEquals(dbList.get(0), "item1");
        Assert.assertEquals(dbList.get(1), "item2");
    }
    
    @Test
    public void createStringList(){
        ArrayList<String> list = new ArrayList<String>();
        list.add("item1");
        list.add("item2");
        list.add(null);
        list.add("item4");
        
        BasicDBList dbList = BasicDBListTool.createStringList(list);
        Assert.assertEquals(dbList.get(0), "item1");
        Assert.assertEquals(dbList.get(1), "item2");
        Assert.assertEquals(dbList.get(2), null);
        Assert.assertEquals(dbList.get(3), "item4");
    }
}
