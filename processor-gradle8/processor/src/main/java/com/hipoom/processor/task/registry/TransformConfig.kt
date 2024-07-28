@file:Suppress("MemberVisibilityCanBePrivate")

package com.hipoom.processor.task.registry

/**
 * 在 build.gradle 中的配置。
 *
 * @author ZhengHaiPeng
 * @since 2024/7/27 20:58
 */
class TransformConfig {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    /**
     * 需要关注的注解全类名。
     */
    var annotations: Set<String>? = null

    /**
     * 需要关注的接口全类名
     */
    var interfaces: Set<String>? = null



    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    /**
     * 用户是否配置了 annotations 或者 interfaces.
     */
    fun hasUserConfigured(): Boolean {
        return !(annotations.isNullOrEmpty() && interfaces.isNullOrEmpty())
    }

}