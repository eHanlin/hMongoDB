package com.ehanlin.hmongodb;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.DBObject;
import java.util.List;

public class AdvancedDBCollectionTest {
    
    public static AdvancedDBCollection coll = null;
    
    @BeforeClass
    public static void setupClass(){
        coll = new AdvancedDBCollection(ConnectTool.instance.getCollection("10.13.103.111", 37017, "test_AdvancedDBCollectionTest", "users"));
    }
    
    @Test
    public void testCRUD(){
        
        coll.insert("{'_id':{'$oid':'4e6872ac14398f0b28977b31'}, 'name':'user1', 'no':1, 'age':10, 'alive':true}");
        
        DBObject dbUser1 = coll.findOne("{'_id':{$oid:'4e6872ac14398f0b28977b31'}}");
        Assert.assertNotNull(dbUser1);
        Assert.assertEquals(dbUser1.get("_id").toString(), "4e6872ac14398f0b28977b31");
        Assert.assertEquals(dbUser1.get("name"), "user1");
        Assert.assertEquals(dbUser1.get("no"), 1);
        Assert.assertTrue((int) dbUser1.get("age") == 10);
        Assert.assertEquals(dbUser1.get("alive"), true);
        
        coll.getDB().dropDatabase();
    }

    @Test
    public void testMultiQueies(){

        coll.getDB().dropDatabase();
        coll.insert("{'_id':'4e6872ac14398f0b2897b31', 'name':'user1',  'no':1 , 'age':10, 'alive':false}");
        coll.insert("{'_id':'4e6872ac14398f0b2897b32', 'name':'user2',  'no':2 , 'age':10, 'alive':true}");
        coll.insert("{'_id':'4e6872ac14398f0b2897b33', 'name':'user3',  'no':3 , 'age':10, 'alive':true}");
        coll.insert("{'_id':'4e6872ac14398f0b2897b34', 'name':'user4',  'no':4 , 'age':10, 'alive':true}");
        coll.insert("{'_id':'4e6872ac14398f0b2897b35', 'name':'user1',  'no':5 , 'age':10, 'alive':true}");
        coll.insert("{'_id':'4e6872ac14398f0b2897b36', 'name':'user5',  'no':6 , 'age':20, 'alive':false}");

        List<DBObject> list = coll.findByQueries("{age:10}","{name:'user1'}","{alive:false}").toArray();

        //for ( DBObject dbObj : list ) System.out.println( dbObj.get("_id").toString() );
        //System.out.println(list.size());

        Assert.assertTrue( list.size() == 1 );
        coll.getDB().dropDatabase();
    }
}
