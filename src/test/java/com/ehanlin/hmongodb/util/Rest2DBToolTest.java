package com.ehanlin.hmongodb.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import com.ehanlin.hmongodb.AdvancedDBCollection;
import com.ehanlin.hmongodb.ConnectTool;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

public class Rest2DBToolTest {

    public static ConnectTool.ConnectToolDBInstance dbInstance = null;
    
    @BeforeClass
    public static void setupClass(){
        dbInstance = new ConnectTool.ConnectToolDBInstance("10.13.103.111", 37017, "test_Rest2DBToolTest");
        AdvancedDBCollection subjectColl = new AdvancedDBCollection(ConnectTool.instance.getCollection("10.13.103.111", 37017, "test_Rest2DBToolTest", "Subject"), WriteConcern.SAFE);
        AdvancedDBCollection questionColl = new AdvancedDBCollection(ConnectTool.instance.getCollection("10.13.103.111", 37017, "test_Rest2DBToolTest", "Question"), WriteConcern.SAFE);
        AdvancedDBCollection userColl = new AdvancedDBCollection(ConnectTool.instance.getCollection("10.13.103.111", 37017, "test_Rest2DBToolTest", "User"), WriteConcern.SAFE);
        AdvancedDBCollection examColl = new AdvancedDBCollection(ConnectTool.instance.getCollection("10.13.103.111", 37017, "test_Rest2DBToolTest", "Exam"), WriteConcern.SAFE);
        
        subjectColl.insert("{'_id':'pc','name':'國文','code':'pc'}");
        subjectColl.insert("{'_id':'ma','name':'數學','code':'ma'}");
        
        questionColl.insert("{'_id':'q_1','name':'q_1','subject':'pc'}");
        questionColl.insert("{'_id':'q_2','name':'q_2','subject':'ma'}");
        questionColl.insert("{'_id':'q_3','name':'q_3','subject':'pc'}");
        
        userColl.insert("{'_id':'u_1','name':'學生一'}");
        userColl.insert("{'_id':'u_2','name':'學生二','friends':['u_3','u_4']}");
        userColl.insert("{'_id':'u_3','name':'學生三','friends':['u_2']}");
        userColl.insert("{'_id':'u_4','name':'學生四','friends':['u_2']}");
     
        examColl.insert("{'_id':'e_1','code':'e_1','name':'考一','subject':'pc','users':['u_1','u_2','u_3'],'questions':['q_1','q_2','q_3']}");
    }
    
    @AfterClass
    public static void cleanClass(){
        ConnectTool.instance.getDB("10.13.103.111", 37017, "test_Rest2DBToolTest").dropDatabase();
    }
    
    @Test
    public void embedByStringTest(){
        AdvancedDBCollection examColl = new AdvancedDBCollection(ConnectTool.instance.getCollection("10.13.103.111", 37017, "test_Rest2DBToolTest", "Exam"), WriteConcern.SAFE);
        
        DBObject dbExam = examColl.findOne("{'_id':'e_1'}");
        Rest2DBTool.embedByString(dbExam, 
            "{\"subject\":true,\"questions\":{\"_coll\":\"Question\",\"subject\":true},\"users\":{\"_coll\":\"User\",\"friends\":{\"_coll\":\"User\"}}}", 
            "{\"subject\":{\"name\":true},\"questions\":{\"name\":true,\"subject\":true},\"users\":{\"name\":true,\"friends\":{\"name\":true,\"friends\":true}}}", 
            null, dbInstance);
        
        Assert.assertEquals(((DBObject)dbExam.get("subject")).get("name"), "國文");
        Assert.assertEquals(((DBObject)((BasicDBList)dbExam.get("questions")).get(0)).get("name"), "q_1");
        Assert.assertEquals(((DBObject)((DBObject)((BasicDBList)dbExam.get("questions")).get(0)).get("subject")).get("name"), "國文");
        Assert.assertEquals(((DBObject)((BasicDBList)dbExam.get("users")).get(0)).get("name"), "學生一");
        Assert.assertTrue(((DBObject)((BasicDBList)dbExam.get("users")).get(0)).get("friends") == null);
        Assert.assertEquals(((DBObject)((BasicDBList)((DBObject)((BasicDBList)dbExam.get("users")).get(1)).get("friends")).get(0)).get("name"), "學生三");
    }
    
    @Test
    public void embedTest(){
        AdvancedDBCollection examColl = new AdvancedDBCollection(ConnectTool.instance.getCollection("10.13.103.111", 37017, "test_Rest2DBToolTest", "Exam"), WriteConcern.SAFE);
        
        Map embed = new HashMap();
        embed.put("subject", true);
        
        Map include = new HashMap();
        Map subjectInclude = new HashMap();
        subjectInclude.put("name", true);
        include.put("subject", subjectInclude);
        
        
        Map questionEmbed = new HashMap();
        questionEmbed.put("_coll", "Question");
        questionEmbed.put("subject", true);
        embed.put("questions", questionEmbed);
        
        Map questionsInclude = new HashMap();
        questionsInclude.put("name", true);
        questionsInclude.put("subject", true);
        include.put("questions", questionsInclude);
        
        
        Map userEmbed = new HashMap();
        userEmbed.put("_coll", "User");
        Map userFriendsEmbed = new HashMap();
        userFriendsEmbed.put("_coll", "User");
        userEmbed.put("friends", userFriendsEmbed);
        embed.put("users", userEmbed);
        
        Map userInclude = new HashMap();
        userInclude.put("name", true);
        Map userFriendsInclude = new HashMap();
        userFriendsInclude.put("name", true);
        userFriendsInclude.put("friends", true);
        userInclude.put("friends", userFriendsInclude);
        include.put("users", userInclude);
        
        DBObject dbExam = examColl.findOne("{'_id':'e_1'}");
        Rest2DBTool.embed(dbExam, embed, include, null, dbInstance);
        
        Assert.assertEquals(((DBObject)dbExam.get("subject")).get("name"), "國文");
        Assert.assertEquals(((DBObject)((BasicDBList)dbExam.get("questions")).get(0)).get("name"), "q_1");
        Assert.assertEquals(((DBObject)((DBObject)((BasicDBList)dbExam.get("questions")).get(0)).get("subject")).get("name"), "國文");
        Assert.assertEquals(((DBObject)((BasicDBList)dbExam.get("users")).get(0)).get("name"), "學生一");
        Assert.assertTrue(((DBObject)((BasicDBList)dbExam.get("users")).get(0)).get("friends") == null);
        Assert.assertEquals(((DBObject)((BasicDBList)((DBObject)((BasicDBList)dbExam.get("users")).get(1)).get("friends")).get(0)).get("name"), "學生三");
    }
    
    
    @Test
    public void buildFieldKeysTest(){
        Map mapA = new HashMap();
        mapA.put("a", true);
        mapA.put("b", "string");
        
        Map mapB = new HashMap();
        mapB.put("c", mapA);
        mapB.put("d", null);
        
        DBObject keys = Rest2DBTool.buildFieldKeys(mapB, mapA);
        Assert.assertEquals(keys.get("c"), true);
        Assert.assertEquals(keys.get("d"), true);
        Assert.assertTrue(keys.get("a") == null);
        Assert.assertTrue(keys.get("b") == null);
        
        keys = Rest2DBTool.buildFieldKeys(null, mapA);
        Assert.assertEquals(keys.get("a"), false);
        Assert.assertEquals(keys.get("b"), false);
        
        keys = Rest2DBTool.buildFieldKeys("string", mapA);
        Assert.assertEquals(keys.get("a"), false);
        Assert.assertEquals(keys.get("b"), false);
    }
    
    
    @Test
    public void convertPathValueTest(){
        Object stringValue = Rest2DBTool.convertPathValue("abc");
        Assert.assertTrue(stringValue instanceof String);
        Assert.assertEquals(stringValue, "abc");
        
        stringValue = Rest2DBTool.convertPathValue("abc.def");
        Assert.assertTrue(stringValue instanceof String);
        Assert.assertEquals(stringValue, "abc.def");
        
        
        Object doubleValue = Rest2DBTool.convertPathValue("1.0");
        Assert.assertTrue(doubleValue instanceof Double);
        Assert.assertEquals(doubleValue, 1.0);
        
        doubleValue = Rest2DBTool.convertPathValue(".1");
        Assert.assertTrue(doubleValue instanceof Double);
        Assert.assertEquals(doubleValue, 0.1);
        
        Object longValue = Rest2DBTool.convertPathValue("1");
        Assert.assertTrue(longValue instanceof Long);
        Assert.assertEquals(longValue, 1L);
        
        Object booleanValue = Rest2DBTool.convertPathValue("true");
        Assert.assertTrue(booleanValue instanceof Boolean);
        Assert.assertEquals(booleanValue, true);
        
        booleanValue = Rest2DBTool.convertPathValue("True");
        Assert.assertTrue(booleanValue instanceof Boolean);
        Assert.assertEquals(booleanValue, true);
        
        booleanValue = Rest2DBTool.convertPathValue("TRUE");
        Assert.assertTrue(booleanValue instanceof Boolean);
        Assert.assertEquals(booleanValue, true);
        
        booleanValue = Rest2DBTool.convertPathValue("false");
        Assert.assertTrue(booleanValue instanceof Boolean);
        Assert.assertEquals(booleanValue, false);
        
        booleanValue = Rest2DBTool.convertPathValue("False");
        Assert.assertTrue(booleanValue instanceof Boolean);
        Assert.assertEquals(booleanValue, false);
        
        booleanValue = Rest2DBTool.convertPathValue("FALSE");
        Assert.assertTrue(booleanValue instanceof Boolean);
        Assert.assertEquals(booleanValue, false);
        
        Object listValue = Rest2DBTool.convertPathValue("abc,abc.def,1.0,.1,1,true,false");
        List list = (List) listValue;
        Assert.assertTrue(list.get(0) instanceof String);
        Assert.assertEquals(list.get(0), "abc");
        Assert.assertTrue(list.get(1) instanceof String);
        Assert.assertEquals(list.get(1), "abc.def");
        Assert.assertTrue(list.get(2) instanceof Double);
        Assert.assertEquals(list.get(2), 1.0);
        Assert.assertTrue(list.get(3) instanceof Double);
        Assert.assertEquals(list.get(3), 0.1);
        Assert.assertTrue(list.get(4) instanceof Long);
        Assert.assertEquals(list.get(4), 1L);
    }
}
