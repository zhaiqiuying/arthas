package com.taobao.arthas.agent.attach;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * 
 * @author hengyunabc 2020-06-22
 *
 */
public class AttachArthasClassloader extends URLClassLoader {
    public AttachArthasClassloader(URL[] urls) {
        //super(urls, ClassLoader.getSystemClassLoader().getParent());
        super(urls, ClassLoader.getSystemClassLoader());
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        final Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        // 优先从parent（SystemClassLoader）里加载系统类，避免抛出ClassNotFoundException
        if (name != null && (name.startsWith("sun.")
                || name.startsWith("java."))) {
            return super.loadClass(name, resolve);
        } else if (name != null && (name.startsWith("com.googlecode") || name.startsWith("com.google"))) {
            return super.loadClass(name, resolve);
        }
        try {
            Class<?> aClass = findClass(name);
            if (resolve) {
                resolveClass(aClass);
            }
            return aClass;
        } catch (Exception e) {
            // ignore
        }
        return super.loadClass(name, resolve);
    }
}
