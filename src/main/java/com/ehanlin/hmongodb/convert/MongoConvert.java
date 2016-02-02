package com.ehanlin.hmongodb.convert;

import com.ehanlin.hconvert.ConverterBase;
import com.ehanlin.hconvert.ModelFinderBase;
import com.ehanlin.hmongodb.convert.model.MongoDefaultModel;

public class MongoConvert extends ConverterBase {
    
    public static final MongoConvert instance = new MongoConvert();

    public MongoConvert(){
        setModelFinder(new ModelFinderBase.FindByInheritanceChain());
        setDefaultModel(new MongoDefaultModel<Object>());
    }
}