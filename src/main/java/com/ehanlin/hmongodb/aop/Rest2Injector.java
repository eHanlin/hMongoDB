package com.ehanlin.hmongodb.aop;

import com.ehanlin.hmongodb.ConnectTool;
import com.ehanlin.hmongodb.annoation.Rest2Inject;
import com.ehanlin.hmongodb.util.Rest2DBToolGET;
import com.mongodb.DBObject;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Order(1)
@Service
public class Rest2Injector {

    @Autowired
    ConnectTool.ConnectToolDBInstance dbInstance = null;

    @SuppressWarnings("unchecked")
    @Around(value="@annotation(rest2Inject)")
    public Object wrapper(ProceedingJoinPoint pjp, Rest2Inject rest2Inject) throws Throwable {
        Object result = pjp.proceed();

        Map<String, DBObject> findOneCache = new ConcurrentHashMap<String, DBObject>();
        Map<String, List<DBObject>> findCache = new ConcurrentHashMap<String, List<DBObject>>();

        if(result instanceof DBObject){
            Rest2DBToolGET.embedByString((DBObject) result, rest2Inject.embed(), rest2Inject.include(), rest2Inject.exclude(), dbInstance, findOneCache, findCache);
        }else if(result instanceof Collection){
            Rest2DBToolGET.embedByString(
                (Collection<DBObject>) result,
                rest2Inject.embed(),
                (rest2Inject.include().isEmpty()) ? null : rest2Inject.include(),
                (rest2Inject.exclude().isEmpty()) ? null : rest2Inject.exclude(),
                dbInstance, findOneCache, findCache);
        }

        return result;
    }

}
