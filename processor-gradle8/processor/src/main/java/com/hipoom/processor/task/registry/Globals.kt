package com.hipoom.processor.task.registry

import com.hipoom.processor.common.Logger
import com.hipoom.processor.common.of

lateinit var config: TransformConfig

/**
 * 需要关注的注解全类名。
 */
val annotations: Set<String>
    get() = config.annotations ?: emptySet()

/**
 * 需要关注的接口全类名
 */
val interfaces: Set<String>
    get() = config.interfaces ?: emptySet()

const val TRANSFORM_NAME = "transform"

object loggers {
    val main = Logger.of(TRANSFORM_NAME, "main")
}