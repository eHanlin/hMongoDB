package com.ehanlin.hmongodb;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ehanlin.hconvert.Converter;
import com.ehanlin.hmongodb.convert.MongoConvert;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBDecoderFactory;
import com.mongodb.DBObject;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;

public class ModelDBCursor<T> implements Iterator<T> , Iterable<T>, Closeable{
    
    private Class<T> modelClass = null;
    
    private DBCursor dbCursor = null;
    private Converter converter = null;
    
    public ModelDBCursor(DBCursor dbCursor, Class<T> modelClass, Converter converter){
        this.dbCursor = dbCursor;
        this.modelClass = modelClass;
        if(converter == null){
            this.converter = MongoConvert.instance;
        }else{
            this.converter = converter;
        }
    }
    
    public ModelDBCursor<T> copy(){
        return new ModelDBCursor<T>(dbCursor.copy(), modelClass, converter);
    }

    @Override
    public Iterator<T> iterator() {
        return copy();
    }
    
    public ModelDBCursor<T> sort( DBObject orderBy ){
        dbCursor.sort(orderBy);
        return this;
    }
    
    public ModelDBCursor<T> addSpecial( String name , Object o ){
        dbCursor.addSpecial(name, o);
        return this;
    }
    
    public ModelDBCursor<T> hint( DBObject indexKeys ){
        dbCursor.hint(indexKeys);
        return this;
    }
    
    public ModelDBCursor<T> hint( String indexName ){
        dbCursor.hint(indexName);
        return this;
    }
    
    public ModelDBCursor<T> snapshot(){
        dbCursor.snapshot();
        return this;
    }
    
    public DBObject explain(){
        return dbCursor.explain();
    }
    
    public ModelDBCursor<T> limit( int n ){
        dbCursor.limit(n);
        return this;
    }
    
    public ModelDBCursor<T> batchSize( int n ){
        dbCursor.batchSize(n);
        return this;
    }
    
    public ModelDBCursor<T> skip( int n ){
        dbCursor.skip(n);
        return this;
    }
    
    public long getCursorId() {
        return dbCursor.getCursorId();
    }
    
    @Override
    public void close() throws IOException {
        dbCursor.close();
    }
    
    public ModelDBCursor<T> addOption( int option ){
        dbCursor.addOption(option);
        return this;
    }
    
    public ModelDBCursor<T> setOptions( int options ){
        dbCursor.setOptions(options);
        return this;
    }
    
    public ModelDBCursor<T> resetOptions(){
        dbCursor.resetOptions();
        return this;
    }
    
    public int getOptions(){
        return dbCursor.getOptions();
    }
    
    public int numGetMores(){
        return dbCursor.numGetMores();
    }
    
    public List<Integer> getSizes(){
        return dbCursor.getSizes();
    }
    
    public int numSeen(){
        return dbCursor.numSeen();
    }

    @Override
    public boolean hasNext() {
        return dbCursor.hasNext();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T next() {
        return (T) getConverter().revert(dbCursor.next(), modelClass);
    }
    
    @SuppressWarnings("unchecked")
    public T curr(){
        return (T) getConverter().revert(dbCursor.curr(), modelClass); 
    }

    @Override
    public void remove() {
        dbCursor.remove();
    }
    
    public int length() {
        return dbCursor.length();
    }
    
    public List<T> toArray(){
        return toArray( Integer.MAX_VALUE ); 
    }
    
    @SuppressWarnings("unchecked")
    public List<T> toArray( int max ) {
        List<DBObject> list = dbCursor.toArray(max);
        List<T> result = new ArrayList<T>();
        for(DBObject dbObject : list){
            result.add((T) getConverter().revert(dbObject, modelClass));
        }
        return result;
    }
    
    public int itcount(){
        return dbCursor.itcount();
    }
    
    public int count() {
        return dbCursor.count();
    }
    
    public int size() {
        return dbCursor.size();
    }
    
    public DBObject getKeysWanted(){
        return dbCursor.getKeysWanted();
    }
    
    public DBObject getQuery(){
        return dbCursor.getQuery();
    }
    
    public DBCollection getCollection(){
        return dbCursor.getCollection();
    }
    
    public ServerAddress getServerAddress() {
        return dbCursor.getServerAddress();
    }
    
    public ModelDBCursor<T> setReadPreference( ReadPreference preference ){
        dbCursor.setReadPreference(preference);
        return this;
    }
    
    public ReadPreference getReadPreference(){
        return dbCursor.getReadPreference();
    }
    
    public ModelDBCursor<T> setDecoderFactory(DBDecoderFactory fact){
        dbCursor.setDecoderFactory(fact);
        return this;
    }
    
    public DBDecoderFactory getDecoderFactory(){
        return dbCursor.getDecoderFactory();
    }
    
    @Override
    public String toString(){
        return "ModelDBCursor : "+dbCursor.toString();
    }

    public DBCursor getDbCursor() {
        return dbCursor;
    }

    public void setDbCursor(DBCursor dbCursor) {
        this.dbCursor = dbCursor;
    }

    public Converter getConverter() {
        return converter;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }
}
