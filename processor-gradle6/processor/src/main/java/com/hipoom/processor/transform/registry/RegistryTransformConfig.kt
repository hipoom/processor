@file:Suppress("MemberVisibilityCanBePrivate")

package com.hipoom.processor.transform.registry

/**
 * 在 build.gradle 中的配置。
 *
 * @author ZhengHaiPeng
 * @since 2024/7/27 20:58
 */
class RegistryTransformConfig {

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

    /**
     * 扫描范围。例如配置了 "com/hipoom", 则只会处理 com.hipoom 包名下的类。
     * 如果不配置，则默认是扫描所有类。
     * 注意： 包名需要使用 '/' 分割，而不是 '.'
     */
    var scanScope: Set<String>? = null

    /**
     * 在处理 interfaces 时，是否需要回溯每一个类的所有父类。
     *
     * 如果 class Child 继承自 class Base, 且 class Base implements InterfaceC,
     * 那么当设置 needTrackSuperClassForInterface == true 时， class Child 会被加入到 Registry 中，
     * 而 needTrackSuperClassForInterface == false 时，则不会被加入到 Registry 中。
     */
    var needTrackSuperClassForInterface = false



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