package com.hipoom.processor.transform.timing

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.gson.Gson
import com.hipoom.processor.PluginConfigs
import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.PathHelper
import com.hipoom.processor.common.flushAll
import com.hipoom.processor.common.measureTime
import com.hipoom.processor.common.of
import com.hipoom.processor.project
import com.hipoom.processor.transform.timing.scan.TimingScanner

/**
 * @author ZhengHaiPeng
 * @since 2024/7/29 21:30
 *
 */
class TimingTransform : Transform() {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    companion object {
        private val logger = Logger.of(TRANSFORM_TIMING, "main")
    }



    /* ======================================================= */
    /* Override/Implements Methods                             */
    /* ======================================================= */

    override fun getName() = TRANSFORM_TIMING

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS

    override fun getScopes(): MutableSet<QualifiedContent.ScopeType> = TransformManager.SCOPE_FULL_PROJECT

    override fun isIncremental() = true

    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
        logger.info("TimingTransform 开始执行了.")

        // 读取用户的配置
        val configs = readConfigs()
        logger.info("=========================")
        logger.info("读取到的配置是")
        logger.info("-------------------------")
        val json = Gson().toJson(configs)
        logger.info("\n$json")
        logger.info("=========================")

        // 开始执行 transform
        val cost = measureTime {
            onTransform(transformInvocation, configs)
        }
        logger.info("TimingTransform 耗时： $cost 毫秒")

        // 所有日志写入文件。
        Logger.flushAll()
    }



    /* ======================================================= */
    /* Private Methods                                         */
    /* ======================================================= */

    private fun readConfigs(): TimingConfig? {
        logger.info("[readConfigs] 开始读取配置")

        // 读取用户配置
        val configs = project.extensions.findByName("hipoom") as? PluginConfigs
        return configs?.timing
    }

    private fun onTransform(
        transformInvocation: TransformInvocation?,
        config: TimingConfig?
    ) {
        // 添加类的搜索路径到 Global.classPool 中
        PathHelper.appendPaths(transformInvocation)

        // 扫描所有输入
        TimingScanner(config).scanAndCopyToOutput(transformInvocation)
    }
}