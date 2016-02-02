package com.ehanlin.hmongodb;

import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

public class ModelDBCollectionTest {
    
    private static class User{
        public ObjectId _id = null;
        public String name = null;
        public int no = 0;
        public Integer age = null;
        public Boolean alive = null;
        public User mate = null;
        public User[] parent = null;
        public List<User> child = null;
        
        public User(){
            
        }
        
        public User(ObjectId _id, String name, int no, Integer age, Boolean alive, 
                User mate, User[] parent, List<User> child){
            this._id = _id;
            this.name = name;
            this.no = no;
            this.age = age;
            this.alive = alive;
            this.mate = mate;
            this.parent = parent;
            this.child = child;
        }
    }
    
    public static ModelDBCollection<User> coll = null;
    
    @BeforeClass
    public static void setupClass(){
        coll = new ModelDBCollection<User>(ConnectTool.instance.getCollection("10.13.103.111", 37017, "test_ModelDBCollectionTest", "users"), User.class);
    }
    
    @Test
    public void testCRUD(){
        User user1 = new User(new ObjectId("4e6872ac14398f0b28977b31"), "user1", 1, 10, true, 
                null, null, null);
        User user2 = new User(new ObjectId("4e6872ac14398f0b28977b32"), "user2", 2, 20, true,
                user1, new User[]{user1}, Arrays.asList(user1));
        
        coll.insertModel(user1);
        coll.insertModel(user2);
        
        User dbUser1 = coll.findOne("{'_id':{$oid:'4e6872ac14398f0b28977b31'}}");
        Assert.assertNotNull(dbUser1);
        Assert.assertEquals(dbUser1._id.toString(), "4e6872ac14398f0b28977b31");
        Assert.assertEquals(dbUser1.name, "user1");
        Assert.assertEquals(dbUser1.no, 1);
        Assert.assertTrue(dbUser1.age == 10);
        Assert.assertEquals(dbUser1.alive, true);
        Assert.assertNull(dbUser1.mate);
        Assert.assertNull(dbUser1.parent);
        Assert.assertNull(dbUser1.child);
        
        User dbUser2 = coll.findOne("{'_id':{$oid:'4e6872ac14398f0b28977b32'}}");
        Assert.assertNotNull(dbUser2);
        Assert.assertEquals(dbUser2._id.toString(), "4e6872ac14398f0b28977b32");
        Assert.assertEquals(dbUser2.name, "user2");
        Assert.assertEquals(dbUser2.no, 2);
        Assert.assertTrue(dbUser2.age == 20);
        Assert.assertEquals(dbUser2.alive, true);
        Assert.assertNotNull(dbUser2.mate);
        Assert.assertNotNull(dbUser2.parent);
        Assert.assertNotNull(dbUser2.child);
        
        Assert.assertEquals(dbUser2.mate._id.toString(), "4e6872ac14398f0b28977b31");
        Assert.assertEquals(dbUser2.mate.name, "user1");
        Assert.assertEquals(dbUser2.mate.no, 1);
        Assert.assertTrue(dbUser2.mate.age == 10);
        Assert.assertEquals(dbUser2.mate.alive, true);
        
        Assert.assertEquals(dbUser2.parent[0]._id.toString(), "4e6872ac14398f0b28977b31");
        Assert.assertEquals(dbUser2.parent[0].name, "user1");
        Assert.assertEquals(dbUser2.parent[0].no, 1);
        Assert.assertTrue(dbUser2.parent[0].age == 10);
        Assert.assertEquals(dbUser2.parent[0].alive, true);
        
        Assert.assertEquals(dbUser2.child.get(0)._id.toString(), "4e6872ac14398f0b28977b31");
        Assert.assertEquals(dbUser2.child.get(0).name, "user1");
        Assert.assertEquals(dbUser2.child.get(0).no, 1);
        Assert.assertTrue(dbUser2.child.get(0).age == 10);
        Assert.assertEquals(dbUser2.child.get(0).alive, true);
        
        coll.getDB().dropDatabase();
    }
}
