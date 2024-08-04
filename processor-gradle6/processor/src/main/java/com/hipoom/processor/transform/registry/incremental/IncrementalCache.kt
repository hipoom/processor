package com.hipoom.processor.transform.registry.incremental

import com.android.build.api.transform.JarInput
import com.google.gson.Gson
import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.flushAll
import com.hipoom.processor.common.of
import com.hipoom.processor.common.toTraceString
import com.hipoom.processor.pluginBuildDirectory
import com.hipoom.processor.transform.registry.InputResult
import com.hipoom.processor.transform.registry.RegistryTransformConfig
import com.hipoom.processor.transform.registry.TRANSFORM_REGISTRY
import java.io.File
import java.text.SimpleDateFormat

/**
 * @author ZhengHaiPeng
 * @since 2024/7/27 21:25
 *
 */
object IncrementalCache {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    private val logger = Logger.of(TRANSFORM_REGISTRY, "incremental")

    /**
     * 旧缓存
     */
    var old: CacheData? = null

    /**
     * 新缓存
     */
    private var new = CacheData()

    /**
     * Registry 所在的 Jar 包
     */
    var jarContainsRegistry: JarInput? = null

    /**
     * Registry 所在的 Jar 包的路径
     */
    var jarPathContainsRegistry: String? = null



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    fun load() {
        logger.info("开始加载缓存")

        try {
            val file = getCacheFile()
            val json = file.readText()
            if (json.isEmpty()) {
                logger.info("没有缓存信息")
                return
            }

            // 存缓存的 json 中读取为对象
            old = Gson().fromJson(json, CacheData::class.java)

            // 获取 jar 对应的文件路径
            jarPathContainsRegistry = old?.jarPathContainsRegistry

            logger.info("缓存加载完毕！")
        } catch (e: Exception) {
            e.printStackTrace()
            logger.warn(e.toTraceString())
        }
    }

    /**
     * 添加一个 Jar 的扫描结果。
     */
    fun addCache(inputAndResult: InputCache) {
        logger.info("添加缓存，input: " + inputAndResult.inputPath + ", res: " + inputAndResult.result)
        new.jarPath2Results.add(inputAndResult)
    }

    /**
     * 从上次的缓存中读取一个 jar 包对应的扫描结果
     */
    fun get(jar: JarInput): InputResult? {
        return old?.jarPath2Results?.find { it.inputPath == jar.file.absolutePath }?.result
    }

    fun clear() {
        // 删除旧缓存
        getCacheFile().delete()
        // 内存清理
        old = null
    }

    @Suppress("SimpleDateFormat")
    fun store(config: RegistryTransformConfig) {
        new.date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())
        new.config = config
        new.jarPathContainsRegistry = jarContainsRegistry?.file?.absolutePath ?: jarPathContainsRegistry
        val file = getCacheFile()
        file.writeText(Gson().toJson(new))
        Logger.flushAll()
    }



    /* ======================================================= */
    /* Private Methods                                         */
    /* ======================================================= */

    private fun getCacheFile(): File {
        val file = File(pluginBuildDirectory, "$TRANSFORM_REGISTRY/incremental/cache.json")
        if (file.exists()) {
            return file
        }

        file.parentFile.also {
            if (!it.exists()) {
                it.mkdirs()
            }
        }

        file.createNewFile()
        return file
    }

}