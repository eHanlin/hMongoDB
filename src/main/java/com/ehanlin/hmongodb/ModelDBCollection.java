package com.ehanlin.hmongodb;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBDecoderFactory;
import com.mongodb.DBEncoder;
import com.mongodb.DBEncoderFactory;
import com.mongodb.DBObject;
import com.mongodb.GroupCommand;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

import com.ehanlin.hconvert.Converter;
import com.ehanlin.hmongodb.convert.MongoConvert;

public class ModelDBCollection<T> {
    private Class<T> modelClass = null;
    private DBObject modelFields = null;

    private DBCollection dbCollection = null;
    private Converter converter = null;
    private WriteConcern writeConcern = null;
    private ReadPreference readPreference = null;
    
    public ModelDBCollection(DBCollection dbCollection, Class<T> modelClass){
        this(dbCollection, null, null, modelClass, null);
    }
    
    public ModelDBCollection(DBCollection dbCollection, WriteConcern writeConcern, Class<T> modelClass){
        this(dbCollection, writeConcern, null, modelClass, null);
    }
    
    public ModelDBCollection(DBCollection dbCollection, ReadPreference readPreference, Class<T> modelClass){
        this(dbCollection, null, readPreference, modelClass, null);
    }
    
    public ModelDBCollection(DBCollection dbCollection, Class<T> modelClass, Converter converter){
        this(dbCollection, null, null, modelClass, converter);
    }
    
    public ModelDBCollection(DBCollection dbCollection, WriteConcern writeConcern, Class<T> modelClass, Converter converter){
        this(dbCollection, writeConcern, null, modelClass, converter);
    }
    
    public ModelDBCollection(DBCollection dbCollection, ReadPreference readPreference, Class<T> modelClass, Converter converter){
        this(dbCollection, null, readPreference, modelClass, converter);
    }
    
    public ModelDBCollection(DBCollection dbCollection, WriteConcern writeConcern, ReadPreference readPreference, Class<T> modelClass, Converter converter){
        this.dbCollection = dbCollection;
        this.modelClass = modelClass;
        this.writeConcern = writeConcern;
        this.readPreference = readPreference;
        modelFields = new BasicDBObject();
        Field[] fields = modelClass.getDeclaredFields();
        for(Field field : fields){
            modelFields.put(field.getName(), 1);
        }
        if(converter == null){
            this.converter = MongoConvert.instance;
        }else{
            this.converter = converter;
        }
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
    
    
    public ModelDBCursor<T> find( String ref ){
        return find((DBObject) JSON.parse(ref), modelFields);
    }
    
    public ModelDBCursor<T> find( String ref , DBObject keys ){
        return find((DBObject) JSON.parse(ref), keys);
    }
    
    public ModelDBCursor<T> find( String ref , String keys ){
        return find((DBObject) JSON.parse(ref), (DBObject) JSON.parse(keys));
    }
    
    
    public T findOne( String o ){
        return findOne(o, modelFields, null, getReadPreference());
    }

    public T findOne( String o, DBObject fields ) {
        return findOne(o, fields, null, getReadPreference());
    }
    
    public T findOne( String o, DBObject fields, String orderBy){
        return findOne(o, fields, orderBy, getReadPreference());
    }
    
    public T findOne( String o, DBObject fields, ReadPreference readPref ){
        return findOne(o, fields, null, readPref);
    }
    
    public T findOne( String o, DBObject fields, String orderBy, ReadPreference readPref ){
        return findOne((DBObject) JSON.parse(o), fields, (DBObject) JSON.parse(orderBy), readPref);
    }
    
    public T findOne( String o, String fields ) {
        return findOne(o, fields, null, getReadPreference());
    }
    
    public T findOne( String o, String fields, ReadPreference readPref ){
        return findOne(o, fields, null, readPref);
    }
    
    public T findOne( String o, String fields, String orderBy){
        return findOne(o, fields, orderBy, getReadPreference());
    }
    
    public T findOne( String o, String fields, String orderBy, ReadPreference readPref ){
        return findOne((DBObject) JSON.parse(o), (DBObject) JSON.parse(fields), (DBObject) JSON.parse(orderBy), readPref);
    }
    
    
    public T findAndModify(String query, DBObject fields, String sort, boolean remove, String update, boolean returnNew, boolean upsert){
        return findAndModify((DBObject) JSON.parse(query), fields, (DBObject) JSON.parse(sort), remove, (DBObject) JSON.parse(update), returnNew, upsert);
    }
    
    public T findAndModify( String query , String sort , String update) {
        return findAndModify(query, modelFields, sort, false, update, false, false);
    }
    
    public T findAndModify( String query , String update ){
        return findAndModify(query, modelFields, null, false, update, false, false);
    }
    
    public T findAndRemove( String query ) {
        return findAndModify(query, modelFields, null, true, null, false, false);
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
        return getCount(query, modelFields, 0, 0, getReadPreference());
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
    
    
    
    public WriteResult insertModel(T[] models, WriteConcern concern){
        return insertModel(Arrays.asList(models) , concern);
    }
    
    public WriteResult insertModel(T model){
        return insertModel(Arrays.asList(model) , getWriteConcern());
    }
    
    @SuppressWarnings("unchecked")
    public WriteResult insertModel(T ... models){
        return insertModel(models, getWriteConcern());
    }
    
    @SuppressWarnings("unchecked")
    public WriteResult insertModel(WriteConcern concern, T ... models){
        return insertModel(models, concern);
    }
    
    public WriteResult insertModel(List<T> models){
        return insertModel(models, getWriteConcern());
    }
    
    public WriteResult insertModel(List<T> models, WriteConcern concern){
        List<DBObject> dbObjects = new ArrayList<DBObject>();
        for(T model : models){
            dbObjects.add((DBObject) getConverter().convert(model, Object.class));
        }
        return insert(dbObjects, concern);
    }
    
    
    public WriteResult updateModel( DBObject q , T model , boolean upsert , boolean multi , WriteConcern concern ){
        return update(q, (DBObject) getConverter().convert(model, Object.class), upsert, multi, concern);
    }
    
    public WriteResult updateModel( DBObject q , T model , boolean upsert , boolean multi ){
        return updateModel(q, model, upsert, multi, getWriteConcern());
    }
    
    public WriteResult updateModel( DBObject q , T model ){
        return updateModel(q, model, false, false, getWriteConcern());
    }
    
    public WriteResult updateMultiModel( DBObject q , T model ){
        return updateModel(q, model, false, true, getWriteConcern());
    }
    
    
    public WriteResult removeModel( T model , WriteConcern concern ){
        return remove((DBObject) getConverter().convert(model, Object.class), concern);
    }

    public WriteResult removeModel( T model ){
        return removeModel(model, getWriteConcern());
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
    
    @SuppressWarnings("unchecked")
    public T findOne( Object obj ){
        return (T) getConverter().revert(dbCollection.findOne(obj, modelFields), modelClass);
    }
    
    @SuppressWarnings("unchecked")
    public T findOne( Object obj, DBObject fields ){
        return (T) getConverter().revert(dbCollection.findOne(obj, fields), modelClass);
    }
    
    @SuppressWarnings("unchecked")
    public T findAndModify(DBObject query, DBObject fields, DBObject sort, boolean remove, DBObject update, boolean returnNew, boolean upsert){
        return (T) getConverter().revert(dbCollection.findAndModify(query, fields, sort, remove, update, returnNew, upsert), modelClass);
    }
    
    @SuppressWarnings("unchecked")
    public T findAndModify( DBObject query , DBObject sort , DBObject update) {
        return (T) getConverter().revert(dbCollection.findAndModify(query, modelFields, sort, false, update, false, false), modelClass);
    }
    
    @SuppressWarnings("unchecked")
    public T findAndModify( DBObject query , DBObject update ){
        return (T) getConverter().revert(dbCollection.findAndModify(query, modelFields, null, false, update, false, false), modelClass);
    }
    
    @SuppressWarnings("unchecked")
    public T findAndRemove( DBObject query ) {
        return (T) getConverter().revert(dbCollection.findAndModify( query, modelFields, null, true, null, false, false ), modelClass);
    }
    
    public void createIndex( final DBObject keys ){
        dbCollection.createIndex(keys);
    }
    
    public void createIndex( DBObject keys , DBObject options ){
        dbCollection.createIndex(keys, options);
    }
    
    public void createIndex( DBObject keys , DBObject options, DBEncoder encoder ){
        dbCollection.createIndex(keys, options, encoder);
    }
    
    public void ensureIndex( final String name ){
        dbCollection.ensureIndex(name);
    }
    
    public void ensureIndex( final DBObject keys ){
        dbCollection.ensureIndex(keys);
    }
    
    public void ensureIndex( DBObject keys , String name ){
        dbCollection.ensureIndex(keys, name);
    }
    
    public void ensureIndex( DBObject keys , String name , boolean unique ){
        dbCollection.ensureIndex(keys, name, unique);
    }
    
    public void ensureIndex( final DBObject keys , final DBObject optionsIN ){
        dbCollection.ensureIndex(keys, optionsIN);
    }
    
    public void resetIndexCache(){
        dbCollection.resetIndexCache();
    }
    
    public static String genIndexName( DBObject keys ){
        return DBCollection.genIndexName(keys);
    }
    
    public void setHintFields( List<DBObject> lst ){
        dbCollection.setHintFields(lst);
    }
    
    public ModelDBCursor<T> find( DBObject ref ){
        return find(ref, null);
    }
    
    public ModelDBCursor<T> find( DBObject ref , DBObject keys ){
        return new ModelDBCursor<T>(new DBCursor(dbCollection, ref, keys, getReadPreference()), modelClass, getConverter());
    }
    
    public ModelDBCursor<T> find(){
        return find(new BasicDBObject());
    }
    
    @SuppressWarnings("unchecked")
    public T findOne(){
        return (T) getConverter().revert(dbCollection.findOne(new BasicDBObject(), modelFields, getReadPreference()), modelClass);
    }
    
    @SuppressWarnings("unchecked")
    public T findOne( DBObject o ){
        return (T) getConverter().revert(dbCollection.findOne(o, modelFields, getReadPreference()), modelClass);
    }

    @SuppressWarnings("unchecked")
    public T findOne( DBObject o, DBObject fields ) {
        return (T) getConverter().revert(dbCollection.findOne(o, fields, getReadPreference()), modelClass);
    }
    
    @SuppressWarnings("unchecked")
    public T findOne( DBObject o, DBObject fields, DBObject orderBy){
        return (T) getConverter().revert(dbCollection.findOne(o, fields, orderBy, getReadPreference()), modelClass);
    }
    
    @SuppressWarnings("unchecked")
    public T findOne( DBObject o, DBObject fields, ReadPreference readPref ){
        return (T) getConverter().revert(dbCollection.findOne(o, fields, readPref), modelClass);
    }
    
    @SuppressWarnings("unchecked")
    public T findOne( DBObject o, DBObject fields, DBObject orderBy, ReadPreference readPref ){
        return (T) getConverter().revert(dbCollection.findOne(o, fields, orderBy, readPref), modelClass);
    }
    
    public Object apply( DBObject o ){
        return dbCollection.apply(o);
    }
    
    public Object apply( DBObject jo , boolean ensureID ){
        return dbCollection.apply(jo, ensureID);
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
        return dbCollection.getCount(query, modelFields, getReadPreference());
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
    
    public ModelDBCollection<T> rename( String newName ){
        return new ModelDBCollection<T> (dbCollection.rename(newName), getWriteConcern(), getReadPreference(), modelClass, converter);
    }
    
    public ModelDBCollection<T> rename( String newName, boolean dropTarget ){
        return new ModelDBCollection<T> (dbCollection.rename(newName, dropTarget), getWriteConcern(), getReadPreference(), modelClass, converter);
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
    
    public MapReduceOutput mapReduce( DBObject command ){
        return dbCollection.mapReduce(command);
    }
    
    public AggregationOutput aggregate( DBObject firstOp, DBObject ... additionalOps){
        return dbCollection.aggregate(firstOp, additionalOps);
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

    public DBObject getModelFields() {
        return modelFields;
    }

    public DBCollection getDbCollection() {
        return dbCollection;
    }

    public void setDbCollection(DBCollection dbCollection) {
        this.dbCollection = dbCollection;
    }

    public Converter getConverter() {
        return converter;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }
    
}