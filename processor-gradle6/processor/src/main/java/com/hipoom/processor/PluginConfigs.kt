@file:Suppress("MemberVisibilityCanBePrivate")

package com.hipoom.processor

import com.hipoom.processor.transform.registry.RegistryTransformConfig
import com.hipoom.processor.transform.timing.TimingConfig

/**
 * plugin 可以让用户配置的配置项。
 * 这个类必须要 open，因为 Gradle 在编译时会动态生成这个类的子类。
 * 如果不是 final，则会出现：
 * ```
 * > Could not create an instance of type com.hipoom.processor.PluginConfigs.
 *      > Class PluginConfigs is final.
 * ```
 * 这样的报错。
 *
 * @author ZhengHaiPeng
 * @since 2024/7/27 22:03
 */
open class PluginConfigs {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    var registry = RegistryTransformConfig()

    fun registry(block: org.gradle.api.Action<RegistryTransformConfig>) {
        block.execute(registry)
    }


    var timing = TimingConfig()

    fun timing(block: org.gradle.api.Action<TimingConfig>) {
        block.execute(timing)
    }

}