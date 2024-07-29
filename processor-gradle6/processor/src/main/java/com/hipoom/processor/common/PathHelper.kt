package com.hipoom.processor.common

import com.android.build.api.transform.TransformInvocation
import com.hipoom.processor.appExtension
import javassist.ClassPool
import java.io.File

/**
 * @author ZhengHaiPeng
 * @since 2024/7/28 13:44 下午
 */
object PathHelper {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    private val logger = Logger.of("main", "PathHelper")



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    /**
     * 这里现在可能会被调用多次，不确定是否会有异常。
     */
    fun appendPaths(transformInvocation: TransformInvocation?) {
        val classPool = ClassPool.getDefault()

        // 将系统的类加入到搜索路径中
        classPool.appendSystemPath()

        val bootClasses = appExtension.bootClasspath
        bootClasses?.forEach { file ->
            classPool.appendClassPath(file.absolutePath)
            logger.info("添加 boot classes: ${file.absolutePath}")
        }

        // 把所有需要打包到 apk 中的类都加入到搜索路径中
        transformInvocation?.inputs?.forEach { input ->
            input.jarInputs.forEach {
                logger.info("添加 Jar 包路径：${it.file.absolutePath}")
                classPool.appendClassPath(it.file.absolutePath)
            }
            input.directoryInputs.forEach {
                appendClassRecursively(classPool, it.file)
            }
        }
    }



    /* ======================================================= */
    /* Private Methods                                         */
    /* ======================================================= */

    /**
     * 递归添加一个文件夹作为搜索路径
     */
    private fun appendClassRecursively(classPool: ClassPool, dir: File) {
        logger.info("添加文件夹路径：${dir.absolutePath}")
        classPool.appendClassPath(dir.absolutePath)
        if (!dir.isDirectory) {
            return
        }
        dir.listFiles()?.forEach {
            if (it.isDirectory) {
                appendClassRecursively(classPool, it)
            }
        }
    }
}