package com.hipoom.processor.transform.timing.editor

import com.hipoom.processor.transform.timing.TimingConfig
import javassist.ClassPool
import javassist.CtClass
import org.objectweb.asm.ClassReader
import java.io.InputStream

/**
 * @author ZhengHaiPeng
 * @since 2024/7/29 21:43
 */
class JarEntryClassHandler : AbsEditor() {

    /* ======================================================= */
    /* Override/Implements Methods                             */
    /* ======================================================= */
    override fun getLoggerName() = "editor/jar"



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    /**
     * 给这个类的所有函数都加上时间记录的代码。
     *
     * @param inputStream 待处理的文件。
     *
     * @return 如果类被修改了，返回修改后类的二进制数据流。 否则，返回 null.
     */
    fun handleClass(
        configs: TimingConfig?,
        inputStream: InputStream
    ): ByteArray? {
        // 读取类信息
        val reader = ClassReader(inputStream)
        val className = reader.className.replace('/', '.')
        log("", "")
        log("handleClass", "className: $className")
        increaseIndent()

        // 忽略系统的类
        if (needIgnoreClassWithName(configs, className)) {
            log("handleClass", "命中黑名单，忽略这个类.")
            decreaseIndent()
            return null
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
            return null
        }

        if (tempCls.isInterface) {
            log("handleClass", "忽略接口.")
            decreaseIndent()
            return null
        }

        log("handleClass", "即将处理类：$callableClassName")
        var hasModified = false
        tempCls.declaredMethods?.forEach {
            hasModified = onVisitMethod(tempCls, it) || hasModified
        }

        // 如果有修改，写入到文件中
        if (hasModified) {
            val bytes = tempCls.toBytecode()

            // detach 非常重要，避免出现多次插桩的情况。
            tempCls.detach()

            decreaseIndent()
            logger.flush()
            return bytes
        }

        decreaseIndent()
        logger.flush()
        return null
    }

}