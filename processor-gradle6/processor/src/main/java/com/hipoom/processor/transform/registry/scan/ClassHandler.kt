package com.hipoom.processor.transform.registry.scan

import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.getInterfacesSafely
import com.hipoom.processor.common.of
import com.hipoom.processor.transform.registry.InputResult
import com.hipoom.processor.transform.registry.RegistryTransformConfig
import com.hipoom.processor.transform.registry.TRANSFORM_REGISTRY
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

    private val logger = Logger.of(TRANSFORM_REGISTRY, "ClassHandler")



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    /**
     * 将 [inputStream] 转化为 Class 信息，然后判断这个 Class 是否添加了想要的注解，或者实现了想要的接口。
     * 如果是，则将 Class 添加到 [outSave] 中。
     *
     * @param inputStream class 对应的输入流。
     * @param outSave 保存结果的地方。
     */
    fun handleClass(configs: RegistryTransformConfig, inputStream: InputStream, outSave: InputResult) {
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

        configs.annotations?.forEach { target ->
            if (tempCls.hasAnnotation(target)) {
                logger.info("「$className」 包含注解「$target」")

                // 添加到结果集中
                val classes = outSave.annotation[target] ?: HashSet()
                outSave.annotation[target] = classes
                classes.add(callableClassName)
            }
        }

        configs.interfaces?.forEach { target ->
            val interfaces = tempCls.getInterfacesSafely(configs.needTrackSuperClassForInterface)
            interfaces.forEach {
                logger.info("「$className」 实现了接口「${it.name}」")
            }
            val found = interfaces.find { it.name == target } != null

            // 当前类没有实现 target 接口
            if (!found) {
                return@forEach
            }

            logger.info("「$className」 实现了接口「$target」")

            // 添加到结果集中
            val classes = outSave.interfaces[target] ?: HashSet()
            classes.add(callableClassName)
            outSave.interfaces[target] = classes
        }
    }

}