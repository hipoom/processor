package com.hipoom.processor.transform.registry


typealias AnnotationName = String
typealias InterfaceName = String
typealias ClassName = String


/**
 * 一个 .jar 或者 .class 的分析结果。
 *
 * @author ZhengHaiPeng
 * @since 2024/7/28 02:25
 */
class InputResult {
    val annotation = HashMap<AnnotationName, HashSet<ClassName>>()
    val interfaces = HashMap<InterfaceName, HashSet<ClassName>>()
}