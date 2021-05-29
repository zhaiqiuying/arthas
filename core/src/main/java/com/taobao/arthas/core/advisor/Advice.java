package com.taobao.arthas.core.advisor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.taobao.arthas.core.util.ObjectUtils;

/**
 * 通知点 Created by vlinux on 15/5/20.
 */
public class Advice {

    private final ClassLoader loader;
    private final Class<?> clazz;
    private final ArthasMethod method;
    private final Object target;
    private final Object[] params;
    private final Object returnObj;
    private final Throwable throwExp;
    private final boolean isBefore;
    private final boolean isThrow;
    private final boolean isReturn;

    public boolean isBefore() {
        return isBefore;
    }

    public boolean isAfterReturning() {
        return isReturn;
    }

    public boolean isAfterThrowing() {
        return isThrow;
    }

    public ClassLoader getLoader() {
        return loader;
    }

    public Object getTarget() {
        return target;
    }

    public Object[] getParams() {
        return params;
    }

    public Object getReturnObj() {
        return returnObj;
    }

    public Throwable getThrowExp() {
        return throwExp;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public ArthasMethod getMethod() {
        return method;
    }

    /**
     * for finish
     *
     * @param loader    类加载器
     * @param clazz     类
     * @param method    方法
     * @param target    目标类
     * @param params    调用参数
     * @param returnObj 返回值
     * @param throwExp  抛出异常
     * @param access    进入场景
     */
    private Advice(
            ClassLoader loader,
            Class<?> clazz,
            ArthasMethod method,
            Object target,
            Object[] params,
            Object returnObj,
            Throwable throwExp,
            int access) {

        this.params = params;
        if (!ObjectUtils.isEmpty(params)) {
            for (int i = 0; i < params.length; i++) {
                this.params[i] = fromPbToJson(loader, this.params[i]);
            }
        }

        if (returnObj != null && returnObj.getClass().getSuperclass().getName().equals("com.google.protobuf.GeneratedMessageV3")) {
            this.returnObj = fromPbToJson(loader, returnObj);
        } else {
            this.returnObj = returnObj;
        }

        this.loader = loader;
        this.clazz = clazz;
        this.method = method;
        this.target = target;
        this.throwExp = throwExp;
        isBefore = (access & AccessPoint.ACCESS_BEFORE.getValue()) == AccessPoint.ACCESS_BEFORE.getValue();
        isThrow = (access & AccessPoint.ACCESS_AFTER_THROWING.getValue()) == AccessPoint.ACCESS_AFTER_THROWING.getValue();
        isReturn = (access & AccessPoint.ACCESS_AFTER_RETUNING.getValue()) == AccessPoint.ACCESS_AFTER_RETUNING.getValue();
    }

    public static Object fromPbToJson(ClassLoader loader, Object o) {
        if (o != null && o.getClass().getSuperclass().getName().equals("com.google.protobuf.GeneratedMessageV3")) {
            ByteArrayOutputStream baos = null;
            ObjectOutputStream oos = null;
            try {
                baos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(baos);
                oos.writeObject(o);
                oos.flush();
                InputStream is = new ByteArrayInputStream(baos.toByteArray());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (oos != null) {
                        oos.close();
                    }
                    if (baos != null) {
                        baos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                return new JsonFormat().printToString((Message) o);
//                return loader.loadClass("com.googlecode.protobuf.format.JsonFormat")
//                        .getMethod("printToString", loader.loadClass("com.google.protobuf.Message"))
//                        .invoke(loader.loadClass("com.googlecode.protobuf.format.JsonFormat").newInstance(), o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return o;
    }

    public static Advice newForBefore(ClassLoader loader,
                                      Class<?> clazz,
                                      ArthasMethod method,
                                      Object target,
                                      Object[] params) {
        return new Advice(
                loader,
                clazz,
                method,
                target,
                params,
                null, //returnObj
                null, //throwExp
                AccessPoint.ACCESS_BEFORE.getValue()
        );
    }

    public static Advice newForAfterRetuning(ClassLoader loader,
                                             Class<?> clazz,
                                             ArthasMethod method,
                                             Object target,
                                             Object[] params,
                                             Object returnObj) {
        return new Advice(
                loader,
                clazz,
                method,
                target,
                params,
                returnObj,
                null, //throwExp
                AccessPoint.ACCESS_AFTER_RETUNING.getValue()
        );
    }

    public static Advice newForAfterThrowing(ClassLoader loader,
                                             Class<?> clazz,
                                             ArthasMethod method,
                                             Object target,
                                             Object[] params,
                                             Throwable throwExp) {
        return new Advice(
                loader,
                clazz,
                method,
                target,
                params,
                null, //returnObj
                throwExp,
                AccessPoint.ACCESS_AFTER_THROWING.getValue()
        );

    }

}
