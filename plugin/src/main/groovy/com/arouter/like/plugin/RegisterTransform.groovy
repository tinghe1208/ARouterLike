package com.arouter.like.plugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager
import com.arouter.like.plugin.utils.Constants;
import com.arouter.like.plugin.utils.Logger
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import com.arouter.like.plugin.utils.ScanUtil
import org.apache.commons.io.IOUtils

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry


class RegisterTransform extends Transform {

    public static ClassPool sClassPool = ClassPool.getDefault()
    //Router所在的jar文件
    static File sRouterInitFile
    //实现了IRouter的CtClass集合
    static Set<CtClass> sRouters = new HashSet<>()
    //Router
    static CtClass sRouterInit

    @Override
    String getName() {
        return Constants.TRANSFORM_NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);

        Logger.i("Start scan register info in jar file.");
        def inputs = transformInvocation.inputs
        def outputProvider = transformInvocation.outputProvider
        inputs.forEach { TransformInput input ->
            // scan all jars
            input.jarInputs.each { JarInput jarInput ->
                String destName = jarInput.name
                // rename jar files
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4)
                }
                // input file
                File src = jarInput.file
                // output file
                File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                //scan jar file to find classes
                if (ScanUtil.shouldProcessPreDexJar(src.absolutePath)) {
                    ScanUtil.scanJar(src, dest)

                }
                FileUtils.copyFile(src, dest)

            }
            // scan class files
            input.directoryInputs.each { DirectoryInput directoryInput ->
                File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                // copy to dest
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }

        insertInitCodeIntoJarFile(sRouterInitFile)
    }

    private byte[] generateCode() {
        CtClass router = sClassPool.get(Constants.ROUTER_INIT_CLASS)
        router.defrost()
        CtMethod ctMethod = sRouterInit.getDeclaredMethod(Constants.ROUTER_METHOD_INIT);

        int i = 0;
        StringBuffer stringBuffer = new StringBuffer()
        sRouters.forEach {
            stringBuffer.append(String.format("%s var%d = new %s();\n", it.name, i, it.name))
            stringBuffer.append(String.format("var%d.register(mRouterMap);\n", i))
            i++
        }
        ctMethod.insertAfter(stringBuffer.toString())
        return router.toBytecode()
    }

    private File insertInitCodeIntoJarFile(File jarFile) {
        if (jarFile) {
            def optJar = new File(jarFile.getParent(), jarFile.name + ".opt")
            if (optJar.exists())
                optJar.delete()
            def file = new JarFile(jarFile)
            Enumeration enumeration = file.entries()
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar))

            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                ZipEntry zipEntry = new ZipEntry(entryName)
                InputStream inputStream = file.getInputStream(jarEntry)
                jarOutputStream.putNextEntry(zipEntry)
                if (Constants.ROUTER_INIT_FILE_PATH == entryName) {
                    Logger.i('Insert init code to class >> ' + entryName)

                    def bytes = generateCode()
                    jarOutputStream.write(bytes)
                }else{
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }
                inputStream.close()
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            file.close()

            if (jarFile.exists()) {
                jarFile.delete()
            }
            optJar.renameTo(jarFile)
        }
        return jarFile
    }

}
