package com.hipoom.processor.transform.registry.edit

import com.android.build.api.transform.Format
import com.android.build.api.transform.TransformOutputProvider
import com.hipoom.processor.common.JarEntryReplaceUtils
import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.of
import com.hipoom.processor.transform.registry.TRANSFORM_REGISTRY
import com.hipoom.processor.transform.registry.incremental.IncrementalCache
import com.hipoom.processor.transform.registry.scan.ScanResult
import javassist.ClassPool
import java.io.File

/**
 * 修改 Registry 代码的类。
 *
 * @author ZhengHaiPeng
 * @since 2024/7/28 10:59 上午
 */
object CodeEditor {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    private val logger = Logger.of(TRANSFORM_REGISTRY, "editor")



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    fun edit(scanResult: ScanResult, outputProvider: TransformOutputProvider) {

        // 获取 Registry 所在的 Jar 包
        val jarContainsRegistry = IncrementalCache.jarContainsRegistry
        if (jarContainsRegistry == null) {
            logger.warn("无法找到 Registry 所在的 jar 包")
            return
        }

        val classRegistry = ClassPool.getDefault().getCtClass("com.hipoom.registry.Registry")
        val isFrozen = classRegistry.isFrozen
        if (isFrozen) {
            classRegistry.defrost()
        }

        val methodInit = classRegistry.getDeclaredMethod("init")

        val sb = StringBuilder()
        sb.append("{")
        scanResult.annotation2Classes.forEach { (annotation, annotated) ->
            logger.info("=========================")
            logger.info("注解: $annotation")
            logger.info("-------------------------")
            annotated.forEach {
                sb.append("addAnnotation(${annotation}.class, ${it}.class);\n")
                logger.info(it)
            }
            logger.info("=========================\n")
        }

        scanResult.interface2Classes.forEach { (`interface`, implemented) ->
            logger.info("=========================")
            logger.info("接口: $`interface`")
            logger.info("-------------------------")
            implemented.forEach {
                sb.append("addInterface(${`interface`}.class, ${it}.class);\n")
                logger.info(it)
            }
            logger.info("=========================\n")
        }

        sb.append("}")

        val code = sb.toString()
        methodInit.setBody(code)
        logger.info("=========================")
        logger.info("Registry#init() 方法的代码")
        logger.info("-------------------------")
        logger.info(code)
        logger.info("=========================")

        // 获取输出路径
        val output: File = outputProvider.getContentLocation(
            IncrementalCache.jarContainsRegistry?.file?.absolutePath,
            IncrementalCache.jarContainsRegistry?.contentTypes,
            IncrementalCache.jarContainsRegistry?.scopes,
            Format.JAR
        )

        logger.info("包含 Registry 类的 jar 包的输入路径为：${IncrementalCache.jarContainsRegistry?.file?.absolutePath}")
        logger.info("包含 Registry 类的 jar 包的输出路径为：${output}")

        JarEntryReplaceUtils.replace(
            jar = output,
            entryName = "com/hipoom/registry/Registry.class",
            entryBytes = classRegistry.toBytecode()
        )
    }

}