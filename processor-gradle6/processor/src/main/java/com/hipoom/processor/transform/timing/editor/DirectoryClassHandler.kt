package com.hipoom.processor.transform.timing.editor

import com.hipoom.processor.transform.timing.TimingConfig
import javassist.ClassPool
import javassist.CtClass
import org.objectweb.asm.ClassReader
import java.io.File
import java.io.FileInputStream

/**
 * @author ZhengHaiPeng
 * @since 2024/7/28 10:41 上午
 */
object DirectoryClassHandler : AbsEditor() {

    /* ======================================================= */
    /* Override/Implements Methods                             */
    /* ======================================================= */

    override fun getLoggerName() = "editor/directory"



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    /**
     * 给这个类的所有函数都加上时间记录的代码。
     *
     * @param fileInputStream 待处理的文件。
     * @param outputDirectory 输出文件夹。
     *
     * @return 是否已经将结果复制到输出目录了。
     */
    fun handleClass(
        configs: TimingConfig?,
        fileInputStream: FileInputStream,
        outputDirectory: File
    ) {
        // 读取类信息
        val reader = ClassReader(fileInputStream)
        val className = reader.className.replace('/', '.')
        log("", "")
        log("handleClass", "className: $className")
        log("handleClass", "outputDirectory: " + outputDirectory.absolutePath)
        increaseIndent()

        // 忽略系统的类
        if (needIgnoreClassWithName(configs, className)) {
            log("handleClass", "命中了黑名单，忽略这个类.")
            decreaseIndent()
            return
        }

        // 这里是解决内部类的类名包含$的问题，如果原始类名也包含$，则会出错(不考虑)
        val callableClassName = className.replace("$", ".")

        // 从 classPool 中找到类名对应的类信息
        val classPool = ClassPool.getDefault()
        val tempCls: CtClass? = try {
            classPool.get(className)
        } catch (e: Exception) {
            null
        }
        if (tempCls == null) {
            log("handleClass", "从 classPool 中找不到 $className 对应的类")
            decreaseIndent()
            return
        }

        if (tempCls.isInterface) {
            log("handleClass", "忽略接口.")
            decreaseIndent()
            return
        }

        log("handleClass", "即将处理类：$callableClassName")
        var hasModified = false
        tempCls.declaredMethods?.forEach {
            log("handleClass", "处理函数：${it.longName}")
            hasModified = onVisitMethod(tempCls, it) || hasModified
        }

        // 如果有修改，写入到文件中
        if (hasModified) {
            log("handleClass", "将修改后的 class 写入到文件中： ${outputDirectory.absolutePath}")
            tempCls.writeFile(outputDirectory.absolutePath)

            // detach 非常重要，避免出现多次插桩的情况。
            tempCls.detach()
        }

        decreaseIndent()
        logger.flush()
        return
    }

}