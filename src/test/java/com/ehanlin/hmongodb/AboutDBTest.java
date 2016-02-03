package com.ehanlin.hmongodb;


import com.ehanlin.hmongodb.util.Rest2DBTool;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.bson.types.ObjectId;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@net.jcip.annotations.NotThreadSafe
public class AboutDBTest {

    private static MongodExecutable mongodExecutable = null;

    private static String host = "localhost";
    private static Integer port = 37017;

    public static ConnectTool.ConnectToolDBInstance dbInstance = null;

    @BeforeClass
    public static void setupClass() throws IOException {
        mongodExecutable = MongodStarter.getDefaultInstance().prepare(new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(port, Network.localhostIsIPv6()))
                .build());
        mongodExecutable.start();

        dbInstance = new ConnectTool.ConnectToolDBInstance(host, port, "test_Rest2DBToolTest");
        AdvancedDBCollection subjectColl = new AdvancedDBCollection(ConnectTool.instance.getCollection(host, port, "test_Rest2DBToolTest", "Subject"), WriteConcern.SAFE);
        AdvancedDBCollection questionColl = new AdvancedDBCollection(ConnectTool.instance.getCollection(host, port, "test_Rest2DBToolTest", "Question"), WriteConcern.SAFE);
        AdvancedDBCollection userColl = new AdvancedDBCollection(ConnectTool.instance.getCollection(host, port, "test_Rest2DBToolTest", "User"), WriteConcern.SAFE);
        AdvancedDBCollection examColl = new AdvancedDBCollection(ConnectTool.instance.getCollection(host, port, "test_Rest2DBToolTest", "Exam"), WriteConcern.SAFE);

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
    public static void cleanupClass() {
        ConnectTool.instance.getDB(host, port, "test_Rest2DBToolTest").dropDatabase();
        mongodExecutable.stop();
    }

    @Test
    public void testCRUD(){
        AdvancedDBCollection coll = new AdvancedDBCollection(ConnectTool.instance.getCollection(host, port, "test_AdvancedDBCollectionTest", "users"));

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
        AdvancedDBCollection coll = new AdvancedDBCollection(ConnectTool.instance.getCollection(host, port, "test_AdvancedDBCollectionTest", "users"));

        coll.getDB().dropDatabase();
        coll.insert("{'_id':'4e6872ac14398f0b2897b31', 'name':'user1',  'no':1 , 'age':10, 'alive':false}");
        coll.insert("{'_id':'4e6872ac14398f0b2897b32', 'name':'user2',  'no':2 , 'age':10, 'alive':true}");
        coll.insert("{'_id':'4e6872ac14398f0b2897b33', 'name':'user3',  'no':3 , 'age':10, 'alive':true}");
        coll.insert("{'_id':'4e6872ac14398f0b2897b34', 'name':'user4',  'no':4 , 'age':10, 'alive':true}");
        coll.insert("{'_id':'4e6872ac14398f0b2897b35', 'name':'user1',  'no':5 , 'age':10, 'alive':true}");
        coll.insert("{'_id':'4e6872ac14398f0b2897b36', 'name':'user5',  'no':6 , 'age':20, 'alive':false}");

        List<DBObject> list = coll.findByQueries("{age:10}","{name:'user1'}","{alive:false}").toArray();

        Assert.assertTrue( list.size() == 1 );
        coll.getDB().dropDatabase();
    }


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

    @Test
    public void testModelCRUD(){
        ModelDBCollection<User> coll = new ModelDBCollection<User>(ConnectTool.instance.getCollection(host, port, "test_ModelDBCollectionTest", "users"), User.class);

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
    @Test
    public void embedByStringTest(){
        AdvancedDBCollection examColl = new AdvancedDBCollection(ConnectTool.instance.getCollection(host, port, "test_Rest2DBToolTest", "Exam"), WriteConcern.SAFE);

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
        AdvancedDBCollection examColl = new AdvancedDBCollection(ConnectTool.instance.getCollection(host, port, "test_Rest2DBToolTest", "Exam"), WriteConcern.SAFE);

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
