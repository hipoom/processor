package com.hipoom.processor.transform.registry.scan

import com.android.build.api.transform.JarInput
import com.android.build.api.transform.Status
import com.android.build.api.transform.TransformOutputProvider
import com.google.gson.Gson
import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.copyToOutput
import com.hipoom.processor.common.of
import com.hipoom.processor.transform.registry.InputResult
import com.hipoom.processor.transform.registry.RegistryTransformConfig
import com.hipoom.processor.transform.registry.TRANSFORM_NAME
import com.hipoom.processor.transform.registry.incremental.IncrementalCache
import com.hipoom.processor.transform.registry.incremental.InputCache
import java.util.jar.JarFile

/**
 * @author ZhengHaiPeng
 * @since 2024/7/28 10:28 上午
 *
 */
object JarScanner {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    private val logger = Logger.of(TRANSFORM_NAME, "JarScan")



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    fun scanAndCopy2Output(configs: RegistryTransformConfig, jar: JarInput, outputProvider: TransformOutputProvider): InputResult {
        log("处理 Jar 包：${jar.file.absolutePath}")

        // 找到了包含 Registry 所在的 jar 包
        if (jar.file.absolutePath == IncrementalCache.jarPathContainsRegistry) {
            IncrementalCache.jarContainsRegistry = jar
        }

        jar.copyToOutput(outputProvider)
        return scanJar(configs, jar)
    }



    /* ======================================================= */
    /* Private Methods                                         */
    /* ======================================================= */

    private fun scanJar(configs: RegistryTransformConfig, jar: JarInput): InputResult {
        when (jar.status) {
            // 未改变的 Jar 包，从缓存中读取其信息
            Status.NOTCHANGED -> {
                log("这是一个未改变的Jar包")
                val cache = IncrementalCache.get(jar)
                if (cache != null) {
                    log("包含: ${Gson().toJson(cache)}")
                    IncrementalCache.addCache(InputCache(jar.file.absolutePath, cache))
                    return cache
                }

                log("找不到Jar包的缓存数据，重新扫描")
                val res = handleJar(configs, jar)
                val data = InputCache(jar.file.absolutePath, res)
                IncrementalCache.addCache(data)
                return res
            }

            // 新增的 Jar 包，直接处理，并添加到缓存
            Status.ADDED -> {
                log("这是一个新增的Jar包")
                val res = handleJar(configs, jar)
                val data = InputCache(jar.file.absolutePath, res)
                IncrementalCache.addCache(data)
                return res
            }

            // 移除的 Jar 包
            Status.REMOVED -> {
                log("这是一个被移除的Jar包")
                return InputResult()
            }

            // 有改变的 Jar 包，从缓存移除，重新分析
            Status.CHANGED -> {
                log("这是一个有改变的Jar包")
                val res = handleJar(configs, jar)
                val data = InputCache(jar.file.absolutePath, res)
                IncrementalCache.addCache(data)
                return res
            }

            else -> {}
        }

        return InputResult()
    }


    private fun handleJar(configs: RegistryTransformConfig, jar: JarInput): InputResult {
        val res = InputResult()

        // 遍历这个 jar 包中的所有类，并逐一处理
        val jarFile = JarFile(jar.file)
        val entries = jarFile.entries()
        while (entries.hasMoreElements()) {
            val entry = entries.nextElement()
            val entryName = entry.name
            if (entry.isDirectory) {
                continue
            }

            if (!entryName.endsWith(".class")) {
                continue
            }

            // 确保 entryName 在扫描范围内，提高速度
            if (!isEntryNameInScope(configs, entryName)) {
                continue
            }

            // 如果目标类是 Registry，记录下所在的 jar
            if (entryName == "com/hipoom/registry/Registry.class") {
                logger.info("jarContainsRegistry = $jar, path = ${jar.file}")
                IncrementalCache.jarContainsRegistry = jar
            }

            val inputStream = jarFile.getInputStream(entry)
            ClassHandler.handleClass(configs, inputStream, res)
        }

        return res
    }

    private fun log(msg: String) {
        logger.info(msg)
    }

    private fun isEntryNameInScope(configs: RegistryTransformConfig, entryName: String): Boolean {
        // 如果没有配置 scanScope， 则默认扫描所有类。
        if (configs.scanScope.isNullOrEmpty()) {
            return true
        }

        configs.scanScope?.forEach { prefix ->
            if (entryName.startsWith(prefix)) {
                return true
            }
        }

        // 匹配完，没有一个命中，则说明 entryName 不在 scope 范围内。
        return false
    }

}