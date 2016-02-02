package com.ehanlin.hmongodb.convert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import com.ehanlin.hconvert.annotation.EffectiveScope;
import com.ehanlin.hconvert.annotation.Skip;
import com.ehanlin.hconvert.annotation.WeakReference;
import com.ehanlin.hconvert.annotation.WeakReferencePolicy;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoConvertTest {

    public static MongoConvert convert = new MongoConvert();
    
    public static String[] strArr = new String[]{"s1", "s2", "s3"};
    public static int[] intArr = new int[]{1, 2, 3};
    public static ObjectId oid = new ObjectId("4e6872ac14398f0b28977b36");
    public static List<Object> list = new ArrayList<Object>();
    public static Map<String, Object> map = new HashMap<String, Object>();
    
    @BeforeClass
    public static void setupClass(){
        list.add("str");
        list.add(1);
        list.add(oid);
        list.add(strArr);
        list.add(intArr);
        
        map.put("name", "name");
        map.put("no", 123);
        map.put("_id", oid);
        map.put("strArr", strArr);
        map.put("intArr", intArr);
    }
    
    @Test
    public void testConvertMapAndList(){
        map.put("list", list);
        
        Object result = convert.convert(map, Object.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getClass(), BasicDBObject.class);
        BasicDBObject dbResult = (BasicDBObject) result;
        
        Assert.assertEquals(dbResult.get("name").getClass(), String.class);
        Assert.assertEquals(dbResult.get("no").getClass(), Integer.class);
        Assert.assertEquals(dbResult.get("_id").getClass(), ObjectId.class);
        Assert.assertEquals(dbResult.get("strArr").getClass(), BasicDBList.class);
        Assert.assertEquals(dbResult.get("intArr").getClass(), BasicDBList.class);
        Assert.assertEquals(dbResult.get("list").getClass(), BasicDBList.class);
        
        Assert.assertEquals(dbResult.get("name"), "name");
        Assert.assertEquals(dbResult.get("no"), 123);
        Assert.assertEquals(dbResult.get("_id").toString(), "4e6872ac14398f0b28977b36");
        
        BasicDBList dbStrArr = (BasicDBList) dbResult.get("strArr");
        Assert.assertEquals(dbStrArr.size(), 3);
        Assert.assertEquals(dbStrArr.get(1), "s2");
        
        BasicDBList dbIntArr = (BasicDBList) dbResult.get("intArr");
        Assert.assertEquals(dbIntArr.size(), 3);
        Assert.assertEquals(dbIntArr.get(1), 2);
        
        BasicDBList dbList = (BasicDBList) dbResult.get("list");
        Assert.assertEquals(dbList.size(), 5);
        Assert.assertEquals(dbList.get(0).getClass(), String.class);
        Assert.assertEquals(dbList.get(1).getClass(), Integer.class);
        Assert.assertEquals(dbList.get(2).getClass(), ObjectId.class);
        Assert.assertEquals(dbList.get(3).getClass(), BasicDBList.class);
        Assert.assertEquals(dbList.get(4).getClass(), BasicDBList.class);
        
        Assert.assertEquals(dbList.get(0), "str");
        Assert.assertEquals(dbList.get(1), 1);
        Assert.assertEquals(dbList.get(2).toString(), "4e6872ac14398f0b28977b36");
        
        BasicDBList dbListStrArr = (BasicDBList) dbList.get(3);
        Assert.assertEquals(dbListStrArr.size(), 3);
        Assert.assertEquals(dbListStrArr.get(1), "s2");
        
        BasicDBList dbListIntArr = (BasicDBList) dbList.get(4);
        Assert.assertEquals(dbListIntArr.size(), 3);
        Assert.assertEquals(dbListIntArr.get(1), 2);
        
        map.remove("list");
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
        @Skip(EffectiveScope.CONVERT)
        public String cvSkip = null;
        @Skip(EffectiveScope.REVERT)
        public String rvSkip = null;
        
        public User(){
            
        }
        
        public User(ObjectId _id, String name, int no, Integer age, Boolean alive, 
                User mate, User[] parent, List<User> child, String cvSkip, String rvSkip){
            this._id = _id;
            this.name = name;
            this.no = no;
            this.age = age;
            this.alive = alive;
            this.mate = mate;
            this.parent = parent;
            this.child = child;
            this.cvSkip = cvSkip;
            this.rvSkip = rvSkip;
        }
    }
    public static User user1 = new User(new ObjectId("4e6872ac14398f0b28977b31"), "user1", 1, 10, true, 
            null, null, null, "cvSkip1", "rvSkip1");
    public static User user2 = new User(new ObjectId("4e6872ac14398f0b28977b32"), "user2", 2, 20, true,
            user1, new User[]{user1}, Arrays.asList(user1), "cvSkip2", "rvSkip2");
    
    @Test
    public void testConvertModel(){
        Object result = convert.convert(user2, Object.class);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getClass(), BasicDBObject.class);
        BasicDBObject dbResult = (BasicDBObject) result;
        
        Assert.assertEquals(dbResult.get("_id").getClass(), ObjectId.class);
        Assert.assertEquals(dbResult.get("name").getClass(), String.class);
        Assert.assertEquals(dbResult.get("no").getClass(), Integer.class);
        Assert.assertEquals(dbResult.get("age").getClass(), Integer.class);
        Assert.assertEquals(dbResult.get("alive").getClass(), Boolean.class);
        Assert.assertEquals(dbResult.get("mate").getClass(), BasicDBObject.class);
        Assert.assertEquals(dbResult.get("parent").getClass(), BasicDBList.class);
        Assert.assertEquals(dbResult.get("child").getClass(), BasicDBList.class);
        Assert.assertNull(dbResult.get("cvSkip"));
        Assert.assertEquals(dbResult.get("rvSkip").getClass(), String.class);
        
        Assert.assertEquals(dbResult.get("_id").toString(), "4e6872ac14398f0b28977b32");
        Assert.assertEquals(dbResult.get("name"), "user2");
        Assert.assertEquals(dbResult.get("no"), 2);
        Assert.assertEquals(dbResult.get("age"), 20);
        Assert.assertEquals(dbResult.get("alive"), true);
        Assert.assertEquals(dbResult.get("rvSkip"), "rvSkip2");
        
        BasicDBObject resultMate = (BasicDBObject) dbResult.get("mate");
        Assert.assertEquals(resultMate.get("_id").getClass(), ObjectId.class);
        Assert.assertEquals(resultMate.get("name").getClass(), String.class);
        Assert.assertEquals(resultMate.get("no").getClass(), Integer.class);
        Assert.assertEquals(resultMate.get("age").getClass(), Integer.class);
        Assert.assertEquals(resultMate.get("alive").getClass(), Boolean.class);
        Assert.assertNull(resultMate.get("cvSkip"));
        Assert.assertEquals(resultMate.get("rvSkip").getClass(), String.class);
        Assert.assertEquals(resultMate.get("_id").toString(), "4e6872ac14398f0b28977b31");
        Assert.assertEquals(resultMate.get("name"), "user1");
        Assert.assertEquals(resultMate.get("no"), 1);
        Assert.assertEquals(resultMate.get("age"), 10);
        Assert.assertEquals(resultMate.get("alive"), true);
        Assert.assertEquals(resultMate.get("rvSkip"), "rvSkip1");
        Assert.assertNull(resultMate.get("mate"));
        Assert.assertNull(resultMate.get("parent"));
        Assert.assertNull(resultMate.get("child"));
        
        BasicDBList resultParent = (BasicDBList) dbResult.get("parent");
        Assert.assertEquals(resultParent.size(), 1);
        BasicDBObject resultParent0 = (BasicDBObject) resultParent.get(0);
        Assert.assertEquals(resultParent0.get("_id").getClass(), ObjectId.class);
        Assert.assertEquals(resultParent0.get("name").getClass(), String.class);
        Assert.assertEquals(resultParent0.get("no").getClass(), Integer.class);
        Assert.assertEquals(resultParent0.get("age").getClass(), Integer.class);
        Assert.assertEquals(resultParent0.get("alive").getClass(), Boolean.class);
        Assert.assertEquals(resultParent0.get("_id").toString(), "4e6872ac14398f0b28977b31");
        Assert.assertNull(resultParent0.get("cvSkip"));
        Assert.assertEquals(resultParent0.get("rvSkip").getClass(), String.class);
        Assert.assertEquals(resultParent0.get("name"), "user1");
        Assert.assertEquals(resultParent0.get("no"), 1);
        Assert.assertEquals(resultParent0.get("age"), 10);
        Assert.assertEquals(resultParent0.get("alive"), true);
        Assert.assertEquals(resultParent0.get("rvSkip"), "rvSkip1");
        Assert.assertNull(resultParent0.get("mate"));
        Assert.assertNull(resultParent0.get("parent"));
        Assert.assertNull(resultParent0.get("child"));
        
        BasicDBList resultChild = (BasicDBList) dbResult.get("child");
        Assert.assertEquals(resultChild.size(), 1);
        BasicDBObject resultChild0 = (BasicDBObject) resultChild.get(0);
        Assert.assertEquals(resultChild0.get("_id").getClass(), ObjectId.class);
        Assert.assertEquals(resultChild0.get("name").getClass(), String.class);
        Assert.assertEquals(resultChild0.get("no").getClass(), Integer.class);
        Assert.assertEquals(resultChild0.get("age").getClass(), Integer.class);
        Assert.assertEquals(resultChild0.get("alive").getClass(), Boolean.class);
        Assert.assertNull(resultChild0.get("cvSkip"));
        Assert.assertEquals(resultChild0.get("rvSkip").getClass(), String.class);
        Assert.assertEquals(resultChild0.get("_id").toString(), "4e6872ac14398f0b28977b31");
        Assert.assertEquals(resultChild0.get("name"), "user1");
        Assert.assertEquals(resultChild0.get("no"), 1);
        Assert.assertEquals(resultChild0.get("age"), 10);
        Assert.assertEquals(resultChild0.get("alive"), true);
        Assert.assertEquals(resultChild0.get("rvSkip"), "rvSkip1");
        Assert.assertNull(resultChild0.get("mate"));
        Assert.assertNull(resultChild0.get("parent"));
        Assert.assertNull(resultChild0.get("child"));
        
        User user3 = (User) convert.revert(result, User.class);
        Assert.assertNotNull(user3);
        Assert.assertEquals(user3._id.toString(), "4e6872ac14398f0b28977b32");
        Assert.assertEquals(user3.name, "user2");
        Assert.assertEquals(user3.no, 2);
        Assert.assertTrue(user3.age == 20);
        Assert.assertEquals(user3.alive, true);
        Assert.assertNotNull(user3.mate);
        Assert.assertNotNull(user3.parent);
        Assert.assertNotNull(user3.child);
        Assert.assertNull(user3.cvSkip);
        Assert.assertNull(user3.rvSkip);
        
        Assert.assertEquals(user3.mate._id.toString(), "4e6872ac14398f0b28977b31");
        Assert.assertEquals(user3.mate.name, "user1");
        Assert.assertEquals(user3.mate.no, 1);
        Assert.assertTrue(user3.mate.age == 10);
        Assert.assertEquals(user3.mate.alive, true);
        
        Assert.assertEquals(user3.parent[0]._id.toString(), "4e6872ac14398f0b28977b31");
        Assert.assertEquals(user3.parent[0].name, "user1");
        Assert.assertEquals(user3.parent[0].no, 1);
        Assert.assertTrue(user3.parent[0].age == 10);
        Assert.assertEquals(user3.parent[0].alive, true);
        Assert.assertNull(user3.parent[0].cvSkip);
        Assert.assertNull(user3.parent[0].rvSkip);
        
        Assert.assertEquals(user3.child.get(0)._id.toString(), "4e6872ac14398f0b28977b31");
        Assert.assertEquals(user3.child.get(0).name, "user1");
        Assert.assertEquals(user3.child.get(0).no, 1);
        Assert.assertTrue(user3.child.get(0).age == 10);
        Assert.assertEquals(user3.child.get(0).alive, true);
        Assert.assertNull(user3.child.get(0).cvSkip);
        Assert.assertNull(user3.child.get(0).rvSkip);
        
        
        Map user4 = (Map) convert.revert(result, Map.class);
        Assert.assertNotNull(user4);
        Assert.assertEquals(user4.get("_id").toString(), "4e6872ac14398f0b28977b32");
        Assert.assertEquals(user4.get("name"), "user2");
        Assert.assertEquals(user4.get("no"), 2);
        Assert.assertTrue((int) user4.get("age") == 20);
        Assert.assertEquals(user4.get("alive"), true);
        Assert.assertNotNull(user4.get("mate"));
        Assert.assertNotNull(user4.get("parent"));
        Assert.assertNotNull(user4.get("child"));
        Assert.assertNull(user4.get("cvSkip"));
        Assert.assertNotNull(user4.get("rvSkip"));
        
    }
    
    
    private static class Item{
        
        public String name = null;
        
        @WeakReference(value=WeakReferencePolicy.IMMEDIATE)
        public Item[] itemArr = null;
        @WeakReference(value=WeakReferencePolicy.MEDIATE)
        public List<Item> itemList = null;
        @WeakReference(value=WeakReferencePolicy.IMMEDIATE)
        public Item imm = null;
        @WeakReference(value=WeakReferencePolicy.MEDIATE)
        public Item med = null;
        
        public Item(){
            
        }
        
        public Item(String name){
            this.name = name;
        }
        
        public String toString(){
            return name;
        }
    }
    
    private static Item item1;
    private static Item item2;
    private static Item item3;
    @BeforeClass
    public static void setupItemClass(){
        item1 = new Item("item1");
        item2 = new Item("item2");
        item3 = new Item("item3");
        
        item1.itemArr = new Item[]{item2, item3};
        item2.itemArr = new Item[]{item1, item3};
        item3.itemArr = new Item[]{item1, item2};
        
        item1.itemList = Arrays.asList(item2, item3);
        item2.itemList = Arrays.asList(item1, item3);
        item3.itemList = Arrays.asList(item1, item2);
        
        item1.imm = item2;
        item1.med = item2;
        
        item2.imm = item3;
        item2.med = item3;
        
        item3.imm = item1;
        item3.med = item1;
    }
    
    @Test
    public void testWeakRef(){
        Object result = convert.convert(item1, Object.class);
        Assert.assertNotNull(result);
        
        BasicDBObject dbResult = (BasicDBObject) result;
        Assert.assertEquals(dbResult.get("name"), "item1");
        Assert.assertNotNull(dbResult.get("itemArr"));
        Assert.assertNotNull(dbResult.get("itemList"));
        Assert.assertNotNull(dbResult.get("imm"));
        Assert.assertNotNull(dbResult.get("med"));
        Assert.assertEquals(dbResult.get("imm"), "item2");
        
        BasicDBList dbItemArr = (BasicDBList) dbResult.get("itemArr");
        Assert.assertEquals(dbItemArr.size(), 2);
        Assert.assertEquals(dbItemArr.get(0), "item2");
        Assert.assertEquals(dbItemArr.get(1), "item3");
        
        BasicDBList dbItemList = (BasicDBList) dbResult.get("itemList");
        Assert.assertEquals(dbItemList.size(), 2);
        
        BasicDBObject dbItemList0 = (BasicDBObject) dbItemList.get(0);
        Assert.assertEquals(dbItemList0.get("name"), "item2");
        Assert.assertNotNull(dbItemList0.get("itemArr"));
        Assert.assertNotNull(dbItemList0.get("itemList"));
        Assert.assertNotNull(dbItemList0.get("imm"));
        Assert.assertNotNull(dbItemList0.get("med"));
        Assert.assertEquals(dbItemList0.get("imm"), "item3");
        Assert.assertEquals(dbItemList0.get("med"), "item3");
        BasicDBList dbItemList0ItemArr = (BasicDBList) dbItemList0.get("itemArr");
        Assert.assertEquals(dbItemList0ItemArr.size(), 2);
        Assert.assertEquals(dbItemList0ItemArr.get(0), "item1");
        Assert.assertEquals(dbItemList0ItemArr.get(1), "item3");
        BasicDBList dbItemList0ItemList = (BasicDBList) dbItemList0.get("itemList");
        Assert.assertEquals(dbItemList0ItemList.size(), 2);
        Assert.assertEquals(dbItemList0ItemList.size(), 2);
        Assert.assertEquals(dbItemList0ItemList.get(0), "item1");
        Assert.assertEquals(dbItemList0ItemList.get(1), "item3");
        
        BasicDBObject dbItemList1 = (BasicDBObject) dbItemList.get(1);
        Assert.assertEquals(dbItemList1.get("name"), "item3");
        Assert.assertNotNull(dbItemList1.get("itemArr"));
        Assert.assertNotNull(dbItemList1.get("itemList"));
        Assert.assertNotNull(dbItemList1.get("imm"));
        Assert.assertNotNull(dbItemList1.get("med"));
        Assert.assertEquals(dbItemList1.get("imm"), "item1");
        Assert.assertEquals(dbItemList1.get("med"), "item1");
        BasicDBList dbItemList1ItemArr = (BasicDBList) dbItemList1.get("itemArr");
        Assert.assertEquals(dbItemList1ItemArr.size(), 2);
        Assert.assertEquals(dbItemList1ItemArr.get(0), "item1");
        Assert.assertEquals(dbItemList1ItemArr.get(1), "item2");
        BasicDBList dbItemList1ItemList = (BasicDBList) dbItemList1.get("itemList");
        Assert.assertEquals(dbItemList1ItemList.size(), 2);
        Assert.assertEquals(dbItemList1ItemList.size(), 2);
        Assert.assertEquals(dbItemList1ItemList.get(0), "item1");
        Assert.assertEquals(dbItemList1ItemList.get(1), "item2");
        
        BasicDBObject dbMed = (BasicDBObject) dbResult.get("med");
        Assert.assertEquals(dbMed.get("name"), "item2");
        Assert.assertNotNull(dbMed.get("itemArr"));
        Assert.assertNotNull(dbMed.get("itemList"));
        Assert.assertNotNull(dbMed.get("imm"));
        Assert.assertNotNull(dbMed.get("med"));
        Assert.assertEquals(dbMed.get("imm"), "item3");
        Assert.assertEquals(dbMed.get("med"), "item3");
        BasicDBList dbMedItemArr = (BasicDBList) dbMed.get("itemArr");
        Assert.assertEquals(dbMedItemArr.size(), 2);
        Assert.assertEquals(dbMedItemArr.get(0), "item1");
        Assert.assertEquals(dbMedItemArr.get(1), "item3");
        BasicDBList dbMedItemList = (BasicDBList) dbMed.get("itemList");
        Assert.assertEquals(dbMedItemList.size(), 2);
        Assert.assertEquals(dbMedItemList.size(), 2);
        Assert.assertEquals(dbMedItemList.get(0), "item1");
        Assert.assertEquals(dbMedItemList.get(1), "item3");
        
        
        Item item4 = (Item) convert.revert(result, Item.class);
        Assert.assertNotNull(item4);
        Assert.assertEquals(item4.name, "item1");
        //因為 String -> Item 會還原失敗，然後傳出 exception 所以會讓整個 array 變 null
        Assert.assertNull(item4.itemArr);
        Assert.assertNotNull(item4.itemList);
        Assert.assertNull(item4.imm);
        Assert.assertNotNull(item4.med);
        
        Assert.assertEquals(item4.itemList.size(), 2);
        Assert.assertEquals(item4.itemList.get(0).name, "item2");
        Assert.assertNull(item4.itemList.get(0).itemArr);
        Assert.assertNull(item4.itemList.get(0).itemList);
        Assert.assertNull(item4.itemList.get(0).imm);
        Assert.assertNull(item4.itemList.get(0).med);
        Assert.assertEquals(item4.itemList.get(1).name, "item3");
        Assert.assertNull(item4.itemList.get(1).itemArr);
        Assert.assertNull(item4.itemList.get(1).itemList);
        Assert.assertNull(item4.itemList.get(1).imm);
        Assert.assertNull(item4.itemList.get(1).med);
        
        Assert.assertEquals(item4.med.name, "item2");
        Assert.assertNull(item4.med.itemArr);
        Assert.assertNull(item4.med.itemList);
        Assert.assertNull(item4.med.imm);
        Assert.assertNull(item4.med.med);
    }
}
