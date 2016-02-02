package com.ehanlin.hmongodb.util;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

public class DBObjectCheckerTest {

    public static DBObject dbObj = null;
    
    @BeforeClass
    public static void setupClass(){
        dbObj = BasicDBObjectBuilder.start()
                .add("_id", new ObjectId("4e6872b014398f0b28977b3b"))
                .add("name", "user1")
                .add("no", 1)
                .add("enabled", false)
                .add("parent", null).get();
    }
    
    @Test
    public void testExistedAndNotNull(){
        Assert.assertTrue(DBObjectChecker.existedAndNotNull(dbObj, "_id"));
        Assert.assertTrue(DBObjectChecker.existedAndNotNull(dbObj, "name"));
        Assert.assertTrue(DBObjectChecker.existedAndNotNull(dbObj, "no"));
        Assert.assertTrue(DBObjectChecker.existedAndNotNull(dbObj, "enabled"));
        Assert.assertFalse(DBObjectChecker.existedAndNotNull(dbObj, "parent"));
        Assert.assertFalse(DBObjectChecker.existedAndNotNull(dbObj, "child"));
    }
    
    @Test
    public void testExistedAndEqual(){
        Assert.assertTrue(DBObjectChecker.existedAndEqual(dbObj, "_id", new ObjectId("4e6872b014398f0b28977b3b")));
        Assert.assertTrue(DBObjectChecker.existedAndEqual(dbObj, "name", "user1"));
        Assert.assertTrue(DBObjectChecker.existedAndEqual(dbObj, "no", 1));
        Assert.assertTrue(DBObjectChecker.existedAndEqual(dbObj, "enabled", false));
        Assert.assertTrue(DBObjectChecker.existedAndEqual(dbObj, "parent", null));
        
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "_id", new ObjectId("4e6872b014398f0b28977b3a")));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "_id", 1));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "_id", null));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "name", "user2"));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "name", 1));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "name", null));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "no", "user2"));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "no", 2));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "no", null));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "enabled", "user2"));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "enabled", 2));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "enabled", true));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "enabled", null));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "parent", "user2"));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "parent", 2));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "parent", false));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "child", "user2"));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "child", 2));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "child", true));
        Assert.assertFalse(DBObjectChecker.existedAndEqual(dbObj, "child", null));
    }
    
    @Test
    public void testGetOrPutDefault(){
        Assert.assertEquals(DBObjectChecker.getOrPutDefault(dbObj, "_id", null), new ObjectId("4e6872b014398f0b28977b3b"));
        Assert.assertEquals(DBObjectChecker.getOrPutDefault(dbObj, "name", null), "user1");
        Assert.assertEquals(DBObjectChecker.getOrPutDefault(dbObj, "no", null), 1);
        Assert.assertEquals(DBObjectChecker.getOrPutDefault(dbObj, "enabled", null), false);
        Assert.assertEquals(DBObjectChecker.getOrPutDefault(dbObj, "parent", "parent"), null);
        
        Assert.assertEquals(DBObjectChecker.getOrPutDefault(dbObj, "name2", "user2"), "user2");
        Assert.assertEquals(dbObj.get("name2"), "user2");
        Assert.assertEquals(DBObjectChecker.getOrPutDefault(dbObj, "name2", null), "user2");
        Assert.assertEquals(dbObj.get("name2"), "user2");
    }
}
