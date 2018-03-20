package com.ehanlin.hmongodb;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.*;
import com.mongodb.util.JSON;
import org.bson.types.ObjectId;

/**
 * 可支援字串查詢及設定 writeConcern 和 readPreference 的 DBCollection
 */
public class AdvancedDBCollection {

    private DBCollection dbCollection = null;
    
    private WriteConcern writeConcern = null;
    private ReadPreference readPreference = null;
    
    public AdvancedDBCollection(DBCollection dbCollection){
        this(dbCollection, null, null);
    }
    
    public AdvancedDBCollection(DBCollection dbCollection, WriteConcern writeConcern){
        this(dbCollection, writeConcern, null);
    }
    
    public AdvancedDBCollection(DBCollection dbCollection, ReadPreference readPreference){
        this(dbCollection, null, readPreference);
    }
    
    public AdvancedDBCollection(DBCollection dbCollection, WriteConcern writeConcern, ReadPreference readPreference){
        this.dbCollection = dbCollection;
        this.writeConcern = writeConcern;
        this.readPreference = readPreference;
    }
    
    
    public WriteResult insert(String o){
        return insert(o, getWriteConcern());
    }
    
    public WriteResult insert(String o, WriteConcern concern){
        return insert((DBObject) JSON.parse(o), concern);
    }
    
    
    public WriteResult update( String q , String o , boolean upsert , boolean multi , WriteConcern concern ){
        return update((DBObject) JSON.parse(q), (DBObject) JSON.parse(o), upsert, multi, concern);
    }
    
    public WriteResult update( String q , String o , boolean upsert , boolean multi){
        return update(q, o, upsert, multi, getWriteConcern());
    }
    
    public WriteResult update( String q , String o){
        return update(q, o, false, false, getWriteConcern());
    }
    
    public WriteResult updateMulti( String q , String o ){
        return update(q, o, false, true, getWriteConcern());
    }
    
    
    public WriteResult remove( String o , WriteConcern concern ){
        return remove((DBObject) JSON.parse(o), concern);
    }
    
    public WriteResult remove( String o ){
        return remove(o, getWriteConcern());
    }
    
    
    public DBCursor find( String ref ){
        return find((DBObject) JSON.parse(ref));
    }
    
    public DBCursor find( String ref , DBObject keys ){
        return find((DBObject) JSON.parse(ref), keys);
    }
    
    public DBCursor find( String ref , String keys ){
        return find((DBObject) JSON.parse(ref), (DBObject) JSON.parse(keys));
    }
    
    
    public DBObject findOne( String o ){
        return findOne((DBObject) JSON.parse(o));
    }

    public DBObject findOne( String o, DBObject fields ) {
        return findOne(o, fields, null, getReadPreference());
    }
    
    public DBObject findOne( String o, DBObject fields, String orderBy){
        return findOne(o, fields, orderBy, getReadPreference());
    }
    
    public DBObject findOne( String o, DBObject fields, ReadPreference readPref ){
        return findOne(o, fields, null, readPref);
    }
    
    public DBObject findOne( String o, DBObject fields, String orderBy, ReadPreference readPref ){
        return findOne((DBObject) JSON.parse(o), fields, (DBObject) JSON.parse(orderBy), readPref);
    }
    
    public DBObject findOne( String o, String fields ) {
        return findOne(o, fields, null, getReadPreference());
    }
    
    public DBObject findOne( String o, String fields, ReadPreference readPref ){
        return findOne(o, fields, null, readPref);
    }
    
    public DBObject findOne( String o, String fields, String orderBy){
        return findOne(o, fields, orderBy, getReadPreference());
    }
    
    public DBObject findOne( String o, String fields, String orderBy, ReadPreference readPref ){
        return findOne((DBObject) JSON.parse(o), (DBObject) JSON.parse(fields), (DBObject) JSON.parse(orderBy), readPref);
    }
    
    
    public DBObject findAndModify(String query, DBObject fields, String sort, boolean remove, String update, boolean returnNew, boolean upsert){
        return findAndModify((DBObject) JSON.parse(query), fields, (DBObject) JSON.parse(sort), remove, (DBObject) JSON.parse(update), returnNew, upsert);
    }
    
    public DBObject findAndModify( String query , String sort , String update) {
        return findAndModify(query, null, sort, false, update, false, false);
    }
    
    public DBObject findAndModify( String query , String update ){
        return findAndModify(query, null, null, false, update, false, false);
    }
    
    public DBObject findAndRemove( String query ) {
        return findAndModify(query, null, null, true, null, false, false);
    }
    
    
    public WriteResult save( String jo ){
        return save(jo, getWriteConcern());
    }
    
    public WriteResult save( String jo, WriteConcern concern ){
        return save((DBObject) JSON.parse(jo), concern);
    }
    
    
    public long count(String query){
        return count(query, getReadPreference());
    }
    
    public long count(String query, ReadPreference readPrefs ){
        return count((DBObject) JSON.parse(query), readPrefs);
    }
    
    
    public long getCount(String query){
        return getCount(query, null, 0, 0, getReadPreference());
    }
    
    public long getCount(String query, DBObject fields){
        return getCount(query, fields, 0, 0, getReadPreference());
    }
    
    public long getCount(String query, DBObject fields, ReadPreference readPrefs){
        return getCount(query, fields, 0, 0, readPrefs);
    }
    
    public long getCount(String query, DBObject fields, long limit, long skip){
        return getCount(query, fields, limit, skip, getReadPreference());
    }
    
    public long getCount(String query, DBObject fields, long limit, long skip, ReadPreference readPrefs ){
        return getCount((DBObject) JSON.parse(query), fields, limit, skip, readPrefs);
    }
    
    
    public DBObject group( String key , String cond , String initial , String reduce ){
        return group(key, cond, initial, reduce, null, getReadPreference());
    }
    
    public DBObject group( String key , String cond , String initial , String reduce , String finalize ){
        return group(key, cond, initial, reduce, finalize, getReadPreference());
    }
    
    public DBObject group( String key , String cond , String initial , String reduce , String finalize, ReadPreference readPrefs ){
        return group((DBObject) JSON.parse(key), (DBObject) JSON.parse(cond), (DBObject) JSON.parse(initial), reduce, finalize, readPrefs);
    }
    
    
    @SuppressWarnings("rawtypes")
    public List distinct( String key , String query ){
        return distinct(key, query, getReadPreference());
    }
    
    @SuppressWarnings("rawtypes")
    public List distinct( String key , String query, ReadPreference readPrefs ){
        return distinct(key, (DBObject) JSON.parse(query), readPrefs);
    }
    
    
    public MapReduceOutput mapReduce( String map , String reduce , String outputTarget , String query ){
        return mapReduce(map, reduce, outputTarget, MapReduceCommand.OutputType.REPLACE, query, getReadPreference());
    }
    
    public MapReduceOutput mapReduce( String map , String reduce , String outputTarget , MapReduceCommand.OutputType outputType , String query ){
        return mapReduce(map, reduce, outputTarget, outputType, query, getReadPreference());
    }
    
    public MapReduceOutput mapReduce( String map , String reduce , String outputTarget , MapReduceCommand.OutputType outputType , String query, ReadPreference readPrefs ){
        return mapReduce(map, reduce, outputTarget, outputType, (DBObject) JSON.parse(query), readPrefs);
    }
    
    
    public WriteResult insert(DBObject[] arr , WriteConcern concern ){
        return dbCollection.insert(arr, concern);
    }
    
    public WriteResult insert(DBObject[] arr , WriteConcern concern, DBEncoder encoder) {
        return dbCollection.insert(arr, concern, encoder);
    }
    
    public WriteResult insert(DBObject o , WriteConcern concern ){
        return dbCollection.insert(o, concern);
    }
    
    public WriteResult insert(DBObject ... arr){
        return dbCollection.insert(arr, getWriteConcern());
    }
    
    public WriteResult insert(WriteConcern concern, DBObject ... arr){
        return dbCollection.insert(concern, arr);
    }
    
    public WriteResult insert(List<DBObject> list ){
        return dbCollection.insert(list, getWriteConcern());
    }
    
    public WriteResult insert(List<DBObject> list, WriteConcern concern ){
        return dbCollection.insert(list, concern);
    }
    
    public WriteResult insert(List<DBObject> list, WriteConcern concern, DBEncoder encoder){
        return dbCollection.insert(list, concern, encoder);
    }
    
    public WriteResult update( DBObject q , DBObject o , boolean upsert , boolean multi , WriteConcern concern ){
        return dbCollection.update(q, o, upsert, multi, concern);
    }
    
    public WriteResult update( DBObject q , DBObject o , boolean upsert , boolean multi , WriteConcern concern, DBEncoder encoder){
        return dbCollection.update(q, o, upsert, multi, concern, encoder);
    }
    
    public WriteResult update( DBObject q , DBObject o , boolean upsert , boolean multi ){
        return dbCollection.update(q, o, upsert, multi, getWriteConcern());
    }
    
    public WriteResult update( DBObject q , DBObject o ){
        return dbCollection.update(q, o, false, false, getWriteConcern());
    }
    
    public WriteResult updateMulti( DBObject q , DBObject o ){
        return dbCollection.update(q, o, false, true, getWriteConcern());
    }
    
    public WriteResult remove( DBObject o , WriteConcern concern ){
        return dbCollection.remove(o, concern);
    }
    
    public WriteResult remove( DBObject o , WriteConcern concern, DBEncoder encoder ){
        return dbCollection.remove(o, concern, encoder);
    }

    public WriteResult remove( DBObject o ){
        return dbCollection.remove(o, getWriteConcern());
    }
    
    public DBObject findOne( Object obj ){
        return dbCollection.findOne(obj);
    }
    
    public DBObject findOne( Object obj, DBObject fields ){
        return dbCollection.findOne(obj, fields);
    }
    
    public DBObject findAndModify(DBObject query, DBObject fields, DBObject sort, boolean remove, DBObject update, boolean returnNew, boolean upsert){
        return dbCollection.findAndModify(query, fields, sort, remove, update, returnNew, upsert);
    }
    
    public DBObject findAndModify( DBObject query , DBObject sort , DBObject update) {
        return dbCollection.findAndModify(query, null, sort, false, update, false, false);
    }
    
    public DBObject findAndModify( DBObject query , DBObject update ){
        return dbCollection.findAndModify(query, null, null, false, update, false, false);
    }
    
    public DBObject findAndRemove( DBObject query ) {
        return dbCollection.findAndModify( query, null, null, true, null, false, false);
    }
    
    public void createIndex( final DBObject keys ){
        dbCollection.createIndex(keys);
    }
    
    public void createIndex( DBObject keys , DBObject options ){
        dbCollection.createIndex(keys, options);
    }
    
    public void setHintFields( List<DBObject> lst ){
        dbCollection.setHintFields(lst);
    }

    public DBCursor findByQueries( DBObject... queries ) {
        DB db = dbCollection.getDB();
        List<String> list = new ArrayList<String>();
        DBCollection coll = dbCollection;
        DBCursor cursorResult = null;
        int index = 0;

        for ( DBObject query : queries ) {

          String tmpCollName = "z" + getName() + (new ObjectId()).toString();

          if ( index < ( queries.length - 1) ) {
            list.add( tmpCollName );
            List<DBObject> result = coll.find(query).toArray();
            coll = db.getCollection( tmpCollName );
            coll.insert( result );
          } else {
            cursorResult = coll.find(query);
          }
          index++;
        }

        for ( String collName : list ) {
          //db.getCollection( collName ).drop();
        }

        return cursorResult;
    }

    public DBCursor findByQueries( String... queries ) {

        DBObject[] queryArray = new DBObject[queries.length];

        for ( int i = 0; i < queries.length; i++ ) queryArray[i] = (DBObject) JSON.parse(queries[i]);

        return findByQueries( queryArray );
    }
    
    public DBCursor find( DBObject ref ){
        return find(ref, null);
    }
    
    public DBCursor find( DBObject ref , DBObject keys ){
        return new DBCursor(dbCollection, ref, keys, getReadPreference());
    }
    
    public DBCursor find(){
        return find(new BasicDBObject());
    }
    
    public DBObject findOne(){
        return dbCollection.findOne(new BasicDBObject(), null, getReadPreference());
    }
    
    public DBObject findOne( DBObject o ){
        return dbCollection.findOne(o, null, getReadPreference());
    }

    public DBObject findOne( DBObject o, DBObject fields ) {
        return dbCollection.findOne(o, fields, getReadPreference());
    }
    
    public DBObject findOne( DBObject o, DBObject fields, DBObject orderBy){
        return dbCollection.findOne(o, fields, orderBy, getReadPreference());
    }
    
    public DBObject findOne( DBObject o, DBObject fields, ReadPreference readPref ){
        return dbCollection.findOne(o, fields, readPref);
    }
    
    public DBObject findOne( DBObject o, DBObject fields, DBObject orderBy, ReadPreference readPref ){
        return dbCollection.findOne(o, fields, orderBy, readPref);
    }
    
    public WriteResult save( DBObject jo ){
        return dbCollection.save(jo, getWriteConcern());
    }
    
    public WriteResult save( DBObject jo, WriteConcern concern ){
        return dbCollection.save(jo, concern);
    }
    
    public void dropIndexes(){
        dbCollection.dropIndexes();
    }
    
    public void dropIndexes( String name ){
        dbCollection.dropIndex(name);
    }
    
    public void drop(){
        dbCollection.drop();
    }
    
    public long count(){
        return dbCollection.count(new BasicDBObject(), getReadPreference());
    }
    
    public long count(DBObject query){
        return dbCollection.count(query, getReadPreference());
    }
    
    public long count(DBObject query, ReadPreference readPrefs ){
        return dbCollection.count(query, readPrefs);
    }
    
    public long getCount(){
        return dbCollection.getCount(getReadPreference());
    }
    
    public long getCount(ReadPreference readPrefs){
        return dbCollection.getCount(readPrefs);
    }
    
    public long getCount(DBObject query){
        return dbCollection.getCount(query, null, getReadPreference());
    }
    
    public long getCount(DBObject query, DBObject fields){
        return dbCollection.getCount(query, fields, getReadPreference());
    }
    
    public long getCount(DBObject query, DBObject fields, ReadPreference readPrefs){
        return dbCollection.getCount(query, fields, readPrefs);
    }
    
    public long getCount(DBObject query, DBObject fields, long limit, long skip){
        return dbCollection.getCount(query, fields, limit, skip, getReadPreference());
    }
    
    public long getCount(DBObject query, DBObject fields, long limit, long skip, ReadPreference readPrefs ){
        return dbCollection.getCount(query, fields, limit, skip, readPrefs);
    }
    
    public AdvancedDBCollection rename( String newName ){
        return new AdvancedDBCollection (dbCollection.rename(newName), getWriteConcern(), getReadPreference());
    }
    
    public AdvancedDBCollection rename( String newName, boolean dropTarget ){
        return new AdvancedDBCollection (dbCollection.rename(newName, dropTarget), getWriteConcern(), getReadPreference());
    }
    
    public DBObject group( DBObject key , DBObject cond , DBObject initial , String reduce ){
        return dbCollection.group(key, cond, initial, reduce, null, getReadPreference());
    }
    
    public DBObject group( DBObject key , DBObject cond , DBObject initial , String reduce , String finalize ){
        return dbCollection.group(key, cond, initial, reduce, finalize, getReadPreference());
    }
    
    public DBObject group( DBObject key , DBObject cond , DBObject initial , String reduce , String finalize, ReadPreference readPrefs ){
        return dbCollection.group(key, cond, initial, reduce, finalize, readPrefs);
    }
    
    public DBObject group( GroupCommand cmd ) {
        return dbCollection.group(cmd, getReadPreference());
    }
    
    public DBObject group( GroupCommand cmd, ReadPreference readPrefs ) {
        return dbCollection.group(cmd, readPrefs);
    }
    
    @SuppressWarnings("rawtypes")
    public List distinct( String key ){
        return dbCollection.distinct(key, getReadPreference());
    }
    
    @SuppressWarnings("rawtypes")
    public List distinct( String key, ReadPreference readPrefs ){
        return dbCollection.distinct(key, readPrefs);
    }
    
    @SuppressWarnings("rawtypes")
    public List distinct( String key , DBObject query ){
        return dbCollection.distinct(key, query, getReadPreference());
    }
    
    @SuppressWarnings("rawtypes")
    public List distinct( String key , DBObject query, ReadPreference readPrefs ){
        return dbCollection.distinct(key, query, readPrefs);
    }
    
    public MapReduceOutput mapReduce( String map , String reduce , String outputTarget , DBObject query ){
        return dbCollection.mapReduce(map, reduce, outputTarget, MapReduceCommand.OutputType.REPLACE, query, getReadPreference());
    }
    
    public MapReduceOutput mapReduce( String map , String reduce , String outputTarget , MapReduceCommand.OutputType outputType , DBObject query ){
        return dbCollection.mapReduce(map, reduce, outputTarget, outputType, query, getReadPreference());
    }
    
    public MapReduceOutput mapReduce( String map , String reduce , String outputTarget , MapReduceCommand.OutputType outputType , DBObject query, ReadPreference readPrefs ){
        return dbCollection.mapReduce(map, reduce, outputTarget, outputType, query, readPrefs);
    }
    
    public MapReduceOutput mapReduce( MapReduceCommand command ){
        return dbCollection.mapReduce(command); 
    }

    @Deprecated
    public AggregationOutput aggregate( DBObject firstOp, DBObject ... additionalOps){
        return dbCollection.aggregate(firstOp, additionalOps);
    }

    private List<DBObject> pipelineStrConvert(String[] pipeline) {
        List<DBObject> ops = new ArrayList<DBObject>();
        for(String op : pipeline){
            ops.add((DBObject)JSON.parse(op));
        }
        return ops;
    }

    public AggregationOutput aggregate(String ... pipeline) {
        return dbCollection.aggregate(pipelineStrConvert(pipeline));
    }

    public AggregationOutput aggregate(List<DBObject> pipeline) {
        return dbCollection.aggregate(pipeline);
    }

    public AggregationOutput aggregate(ReadPreference readPreference, String ... pipeline) {
        return dbCollection.aggregate(pipelineStrConvert(pipeline), readPreference);
    }

    public AggregationOutput aggregate(List<DBObject> pipeline, ReadPreference readPreference) {
        return dbCollection.aggregate(pipeline, readPreference);
    }

    public Cursor aggregate(AggregationOptions options, String ... pipeline) {
        return dbCollection.aggregate(pipelineStrConvert(pipeline), options);
    }

    public Cursor aggregate(List<DBObject> pipeline, AggregationOptions options) {
        return dbCollection.aggregate(pipeline, options);
    }

    public Cursor aggregate(AggregationOptions options, ReadPreference readPreference, String ... pipeline) {
        return dbCollection.aggregate(pipelineStrConvert(pipeline), options, readPreference);
    }

    public Cursor aggregate(List<DBObject> pipeline, AggregationOptions options, ReadPreference readPreference) {
        return dbCollection.aggregate(pipeline, options, readPreference);
    }

    public List<Cursor> parallelScan(ParallelScanOptions parallelScanOptions) {
        return dbCollection.parallelScan(parallelScanOptions);
    }


    public List<DBObject> getIndexInfo() {
        return dbCollection.getIndexInfo();
    }

    public void dropIndex( DBObject keys ){
        dbCollection.dropIndex(keys);
    }
    
    public void dropIndex( String name ){
        dbCollection.dropIndex(name);
    }
    
    public CommandResult getStats() {
        return dbCollection.getStats();
    }
    
    public boolean isCapped() {
        return dbCollection.isCapped();
    }
    
    public DBCollection getCollection( String n ){
        return dbCollection.getCollection(n);
    }
    
    public String getName(){
        return dbCollection.getName();
    }
    
    public String getFullName(){
        return dbCollection.getFullName();
    }
    
    public DB getDB(){
        return dbCollection.getDB();
    }
    
    public int hashCode(){
        return dbCollection.hashCode();
    }
    
    public boolean equals( Object o ){
        return o == this;
    }
    
    public String toString(){
        return "ModelDBCollection : " + dbCollection.toString();
    }
    
    @SuppressWarnings("rawtypes")
    public void setObjectClass( Class c ){
        dbCollection.setObjectClass(c);
    }
    
    @SuppressWarnings("rawtypes")
    public Class getObjectClass(){
        return dbCollection.getObjectClass();
    }
    
    @SuppressWarnings("rawtypes")
    public void setInternalClass( String path , Class c ){
        dbCollection.setInternalClass(path, c);
    }
    
    public void setWriteConcern( WriteConcern concern ){
        dbCollection.setWriteConcern(concern);
    }
    
    public WriteConcern getWriteConcern(){
        if(writeConcern != null){
            return writeConcern;
        }
        return dbCollection.getWriteConcern();
    }
    
    public void setReadPreference( ReadPreference preference ){
        dbCollection.setReadPreference(preference);
    }
    
    public ReadPreference getReadPreference(){
        if(readPreference != null){
            return readPreference;
        }
        return dbCollection.getReadPreference();
    }
    
    public void addOption( int option ){
        dbCollection.addOption(option);
    }
    
    public void setOptions( int options ){
        dbCollection.setOptions(options);
    }
    
    public void resetOptions(){
        dbCollection.resetOptions();
    }
    
    public int getOptions(){
        return dbCollection.getOptions();
    }
    
    public void setDBDecoderFactory(DBDecoderFactory fact) {
        dbCollection.setDBDecoderFactory(fact);
    }
    
    public DBDecoderFactory getDBDecoderFactory() {
        return dbCollection.getDBDecoderFactory();
    }
    
    public void setDBEncoderFactory(DBEncoderFactory fact) {
        dbCollection.setDBEncoderFactory(fact);
    }
    
    public DBEncoderFactory getDBEncoderFactory() {
        return dbCollection.getDBEncoderFactory();
    }

    public DBCollection getDbCollection() {
        return dbCollection;
    }

    public void setDbCollection(DBCollection dbCollection) {
        this.dbCollection = dbCollection;
    }
}
