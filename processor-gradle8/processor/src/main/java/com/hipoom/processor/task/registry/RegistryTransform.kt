package com.hipoom.processor.task.registry

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.flushAll
import org.gradle.api.Project

/**
 * @author ZhengHaiPeng
 * @since 2024/7/27 21:05
 *
 */
class RegistryTransform(
    private val project: Project,
    private val appExtension: AppExtension
) : Transform() {

    /* ======================================================= */
    /* Override/Implements Methods                             */
    /* ======================================================= */

    override fun getName() = "registry"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS

    override fun getScopes(): MutableSet<QualifiedContent.ScopeType> = TransformManager.SCOPE_FULL_PROJECT

    override fun isIncremental() = true

    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)

        loggers.main.info("RegistryTransform 开始执行了.")

        // 加载缓存
        IncrementalCache.load()

        // 所有日志写入文件。
        Logger.flushAll()
    }

}