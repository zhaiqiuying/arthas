package com.taobao.arthas.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;

/**
 * @author gongdewei 2020/5/15
 */
public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    private static Field serializeWriterBufLocalField;
    private static Field serializeWriterBytesBufLocal;
    private static Field serializeWriterBufferThreshold;

    /**
     * Set Fastjson SerializeWriter Buffer Threshold
     * @param value
     */
    public static void setSerializeWriterBufferThreshold(int value) {
        Class<SerializeWriter> clazz = SerializeWriter.class;
        try {
            if (serializeWriterBufferThreshold == null) {
                serializeWriterBufferThreshold = clazz.getDeclaredField("BUFFER_THRESHOLD");
            }
            serializeWriterBufferThreshold.setAccessible(true);
            serializeWriterBufferThreshold.set(null, value);
        } catch (Throwable e) {
            logger.error("update SerializeWriter.BUFFER_THRESHOLD value failed", e);
        }
    }

    /**
     * Set Fastjson SerializeWriter ThreadLocal value
     * @param bufSize
     */
    public static void setSerializeWriterBufThreadLocal(int bufSize) {
        Class<SerializeWriter> clazz = SerializeWriter.class;
        try {
            //set threadLocal value
            if (serializeWriterBufLocalField == null) {
                serializeWriterBufLocalField = clazz.getDeclaredField("bufLocal");
            }
            serializeWriterBufLocalField.setAccessible(true);
            ThreadLocal<char[]> bufLocal = (ThreadLocal<char[]>) serializeWriterBufLocalField.get(null);
            char[] charsLocal = bufLocal.get();
            if (charsLocal == null || charsLocal.length < bufSize) {
                bufLocal.set(new char[bufSize]);
            }

            if (serializeWriterBytesBufLocal == null) {
                serializeWriterBytesBufLocal = clazz.getDeclaredField("bytesBufLocal");
            }
            serializeWriterBytesBufLocal.setAccessible(true);
            ThreadLocal<byte[]> bytesBufLocal = (ThreadLocal<byte[]>) serializeWriterBytesBufLocal.get(null);
            byte[] bytesLocal = bytesBufLocal.get();
            if (bytesLocal == null || bytesLocal.length < bufSize) {
                bytesBufLocal.set(new byte[bufSize]);
            }
        } catch (Throwable e) {
            logger.error("update SerializeWriter.BUFFER_THRESHOLD value failed", e);
        }
    }

    /**
     * Set Fastjson SerializeWriter ThreadLocal value
     */
    public static void setSerializeWriterBufThreadLocal(char[] charsBuf, byte[] bytesBuf) {
        Class<SerializeWriter> clazz = SerializeWriter.class;
        try {
            //set threadLocal value
            if (serializeWriterBufLocalField == null) {
                serializeWriterBufLocalField = clazz.getDeclaredField("bufLocal");
            }
            serializeWriterBufLocalField.setAccessible(true);
            ThreadLocal<char[]> bufLocal = (ThreadLocal<char[]>) serializeWriterBufLocalField.get(null);
            bufLocal.set(charsBuf);

            if (serializeWriterBytesBufLocal == null) {
                serializeWriterBytesBufLocal = clazz.getDeclaredField("bytesBufLocal");
            }
            serializeWriterBytesBufLocal.setAccessible(true);
            ThreadLocal<byte[]> bytesBufLocal = (ThreadLocal<byte[]>) serializeWriterBytesBufLocal.get(null);
            bytesBufLocal.set(bytesBuf);
        } catch (Throwable e) {
            logger.error("update SerializeWriter.BUFFER_THRESHOLD value failed", e);
        }
    }

    public static Object fromPbToJson(ClassLoader loader, Object o) {

        try {
            Object obj = fromPbToJsonStr(loader, o);
            if (!obj.getClass().equals(String.class) || StringUtils.isEmpty(obj)) {
                return obj;
            }
            return JSON.parse((String) obj);
        } catch (Exception e) {
            logger.error("fromPbToJson fail", e);
        }
        return o;
    }

    public static Object fromPbToJsonStr(ClassLoader loader, Object o) {
        if (o != null && o.getClass().getSuperclass().getName().equals("com.google.protobuf.GeneratedMessageV3")) {
            ByteArrayOutputStream baos = null;
            ObjectOutputStream oos = null;
            try {
                baos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(baos);
                oos.writeObject(o);
                oos.flush();
            } catch (Exception e) {
                logger.error("fromPbToJsonStr fail", e);
                return o;
            } finally {
                try {
                    if (oos != null) {
                        oos.close();
                    }
                    if (baos != null) {
                        baos.close();
                    }
                } catch (IOException e) {
                    logger.error("fromPbToJsonStr fail", e);
                }
            }
            try {
                return new JsonFormat().printToString((Message) o);
            } catch (Exception e) {
                logger.error("fromPbToJsonStr fail", e);
            }
        }
        return o;
    }

}
