package com.hipoom.processor.transform.registry

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.gson.Gson
import com.hipoom.processor.PluginConfigs
import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.PathHelper
import com.hipoom.processor.common.copyInputsToOutputs
import com.hipoom.processor.common.flushAll
import com.hipoom.processor.common.of
import com.hipoom.processor.project
import com.hipoom.processor.transform.registry.edit.CodeEditor
import com.hipoom.processor.transform.registry.incremental.IncrementalCache
import com.hipoom.processor.transform.registry.scan.InputScanner

/**
 * @author ZhengHaiPeng
 * @since 2024/7/27 21:05
 *
 */
class RegistryTransform : Transform() {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    companion object {
        private val logger = Logger.of(TRANSFORM_REGISTRY, "main")
    }



    /* ======================================================= */
    /* Override/Implements Methods                             */
    /* ======================================================= */

    override fun getName() = TRANSFORM_REGISTRY

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS

    override fun getScopes(): MutableSet<QualifiedContent.ScopeType> = TransformManager.SCOPE_FULL_PROJECT

    override fun isIncremental() = true

    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)

        logger.info("RegistryTransform 开始执行了.")

        // 加载缓存
        IncrementalCache.load()

        // 读取用户的配置
        val configs = readConfigs()
        logger.info("=========================")
        logger.info("读取到的配置是")
        logger.info("-------------------------")
        val json = Gson().toJson(configs)
        logger.info("\n$json")
        logger.info("=========================")

        // 如果配置是空的，不做处理，直接将 input 拷贝到 output
        if (configs == null) {
            transformInvocation?.copyInputsToOutputs()
            Logger.flushAll()
            return
        }

        // 开始执行 transform
        val begin = System.currentTimeMillis()
        onTransform(transformInvocation, configs)
        val end = System.currentTimeMillis()
        val cost = end - begin
        logger.info("耗时： $cost 毫秒")

        // 保存配置
        IncrementalCache.store(configs)

        // 所有日志写入文件。
        Logger.flushAll()
    }



    /* ======================================================= */
    /* Private Methods                                         */
    /* ======================================================= */

    private fun readConfigs(): RegistryTransformConfig? {
        logger.info("[readConfigs] 开始读取配置")

        // 读取用户配置
        val configs = project.extensions.findByName("hipoom") as? PluginConfigs
        val transformConfig = configs?.registry

        // 如果没有配置，清理增量编译的缓存
        if (transformConfig == null || !transformConfig.hasUserConfigured()) {
            IncrementalCache.clear()
            showConfigHint()
            return null
        }

        // 如果没有旧的数据，将新数据缓存
        if (IncrementalCache.old == null) {
            IncrementalCache.clear()
            return transformConfig
        }

        // 转化为 json，判断是否相等
        val gson = Gson()
        val oldJson = gson.toJson(IncrementalCache.old?.config)
        val newJson = gson.toJson(transformConfig)
        logger.info("新配置：$newJson")
        logger.info("旧配置：$oldJson")

        if (oldJson != newJson) {
            logger.info("配置发生了变更，清除增量编译的缓存！")
            IncrementalCache.clear()
        } else {
            logger.info("配置相同，使用增量编译.")
        }

        return transformConfig
    }

    private fun showConfigHint() {
        logger.info(
        """
        请在项目 app module 级别的 build.gradle 中添加 hipoom 配置，例如：
        apply plugin: 'hipoom'
        hipoom {
            registry {
                annotations = ["注解1的全类名", "注解2的全类名"]
                interfaces = ["接口1的全类名", "接口2的全类名"]
            }
        }
        """.trimIndent()
        )
    }

    private fun onTransform(
        transformInvocation: TransformInvocation?,
        config: RegistryTransformConfig
    ) {
        // 如果配置的注解、接口列表为空，不再处理
        if (!config.hasUserConfigured()) {
            logger.info("配置是空的，不做处理，直接将 input 拷贝到 output")
            transformInvocation?.copyInputsToOutputs()
            return
        }

        // 添加类的搜索路径到 Global.classPool 中
        PathHelper.appendPaths(transformInvocation)

        // 扫描所有输入
        val scanRes = InputScanner.scan(config, transformInvocation)

        // 将扫描得到的类信息插入到代码中
        CodeEditor.edit(scanRes, transformInvocation?.outputProvider!!)
    }
}