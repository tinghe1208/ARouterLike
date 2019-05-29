package com.arouter.like.plugin.utils

import com.arouter.like.plugin.RegisterTransform
import javassist.CtClass
import javassist.NotFoundException;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class ScanUtil {


    static boolean shouldProcessPreDexJar(String path) {
        return !path.contains("com.android.support") && !path.contains("/android/m2repository")
    }

    static boolean shouldProcessClass(String entryName) {
        return entryName != null && entryName.startsWith(Constants.ROUTER_CLASS_PACKAGE_NAME)
    }

    /**
     * scan jar file
     * @param jarFile All jar files that are compiled into apk
     * @param destFile dest file after this transform
     */
    static void scanJar(File jarFile, File destFile) {
        if (jarFile) {
            RegisterTransform.sClassPool.appendClassPath(jarFile.absolutePath)
            def file = new JarFile(jarFile)
            Enumeration enumeration = file.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String className = getJarEntryClassName(jarEntry)
                Logger.i(className)
                if(className) {
                    CtClass ctClass = RegisterTransform.sClassPool.get(className)
                    if (Constants.ROUTER_INIT_CLASS == className) {
                        //找到包含Router的jar文件
                        RegisterTransform.sRouterInitFile = destFile
                        RegisterTransform.sRouterInit = ctClass;
                    } else {
                        try {
                            CtClass[] interfaces = ctClass.getInterfaces()
                            boolean findRouter = false
                            if (interfaces) {
                                def iterator = interfaces.iterator()
                                while (iterator.hasNext()) {
                                    CtClass value = iterator.next()
                                    if (value.name == Constants.IROUTER_CLASS) {
                                        //找到实现了IRouter的Class
                                        RegisterTransform.sRouters.add(ctClass)
                                        findRouter = true
                                        break
                                    }
                                }
                            }
                            if (!findRouter) {
                                ctClass.detach()
                            }
                        } catch (NotFoundException e) {

                        }
                    }
                }
            }
            file.close()
        }
    }

    static String getJarEntryClassName(JarEntry jarEntry) {
        String entryName = jarEntry.getName()
        if(entryName.endsWith(".class")) {
            return entryName.replace("/", ".").substring(0, entryName.length() - 6)
        }else{
            return null
        }
    }
}
