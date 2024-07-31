package com.hipoom.processor.transform.timing.scan

import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.of
import com.hipoom.processor.transform.timing.TRANSFORM_TIMING
import com.hipoom.processor.transform.timing.TimingConfig
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.Modifier
import org.objectweb.asm.ClassReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * @author ZhengHaiPeng
 * @since 2024/7/28 10:41 上午
 */
object ClassHandler {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    private val logger = Logger.of(TRANSFORM_TIMING, "ClassHandler")



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    /**
     * 给这个类的所有函数都加上时间记录的代码。
     * 这一步，修改完之后，会写入到文件中。
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
    ): Boolean {
        // 读取类信息
        val reader = ClassReader(fileInputStream)
        val className = reader.className.replace('/', '.')
        log("", "")
        log("handleClass", "className: $className")
        log("handleClass", "outputDirectory: " + outputDirectory.absolutePath)
        increaseIndent()

        // 跳过 .R$id、.R$layout 等等...
        if (className.contains(".R\$")) {
            log("handleClass", "忽略名字中有 .R$ 的类.")
            decreaseIndent()
            return false
        }

        // 忽略系统的类
        if (needIgnoreClassWithName(className)) {
            log("handleClass", "忽略以 'kotlin.'、 'com.android.' 等开头的类.")
            decreaseIndent()
            return false
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
            return false
        }

        if (tempCls.isInterface) {
            log("handleClass", "忽略接口.")
            decreaseIndent()
            return false
        }

        log("handleClass", "即将处理类：$callableClassName")
        var hasModified = false
        tempCls.declaredMethods?.forEach {
            hasModified = hasModified || onVisitMethod(tempCls, it)
        }

        // 如果有修改，写入到文件中
        if (hasModified) {
            log("handleClass", "将修改后的 class 写入到文件中： ${outputDirectory.absolutePath}")
            tempCls.writeFile(outputDirectory.absolutePath)
        }

        decreaseIndent()
        logger.flush()
        return hasModified
    }



    /* ======================================================= */
    /* Private Methods                                         */
    /* ======================================================= */

    private fun needIgnoreClassWithName(className: String): Boolean {
        return listOf("kotlin.", "kotlinx.", "com.android.", "androidx.").any {
            className.startsWith(it)
        }
    }

    /**
     * @return 是否修改了 [ctMethod] .
     */
    private fun onVisitMethod(ctClass: CtClass, ctMethod: CtMethod): Boolean {
        val name = ctMethod.name
        log("onVisitMethod", "处理: $name")
        increaseIndent()

        if (ctMethod.isEmpty) {
            log("onVisitMethod", "忽略空方法")
            decreaseIndent()
            return false
        }

        if (Modifier.isNative(ctMethod.modifiers)) {
            log("onVisitMethod", "忽略 native 方法")
            decreaseIndent()
            return false
        }

        if (ctClass.isFrozen) {
            ctClass.defrost()
        }

        try {
            ctMethod.insertBefore("android.util.Log.i(\"插桩\", \"$name\" + \" 开始-->\");")
            ctMethod.insertAfter("android.util.Log.i(\"插桩\", \"$name\" + \" 结束<--\");")
        } catch (e: Exception) {
            log("onVisitMethod", "异常：" + e.message)
        }

        log("onVisitMethod", "插入完毕.")

        decreaseIndent()
        return true
    }


    private var indent = ""

    private fun increaseIndent() {
        indent = "$indent    "
    }

    private fun decreaseIndent() {
        indent = indent.substring(0, indent.length - 4)
    }

    private fun log(methodName: String, msg: String) {
        logger.info("$indent |-- [$methodName] $msg")
    }

}