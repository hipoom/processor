package com.hipoom.processor.transform.timing.scan

import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.of
import com.hipoom.processor.transform.timing.TRANSFORM_TIMING
import com.hipoom.processor.transform.timing.TimingConfig
import javassist.ClassPool
import javassist.CtClass
import org.objectweb.asm.ClassReader
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
     *
     * @param inputStream class 对应的输入流。
     */
    fun handleClass(configs: TimingConfig?, inputStream: InputStream) {
        // 读取类信息
        val reader = ClassReader(inputStream)
        val className = reader.className.replace('/', '.')

        logger.info("[handleClass] $className")

        // 跳过 .R$id、.R$layout 等等...
        if (className.contains(".R\$")) {
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
            logger.warn("[handleClass] 从 classPool 中找不到 $className 对应的类")
            return
        }

        // TODO:
    }

}