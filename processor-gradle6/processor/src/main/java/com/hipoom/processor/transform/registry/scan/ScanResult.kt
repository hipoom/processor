package com.hipoom.processor.transform.registry.scan

import com.android.build.api.transform.JarInput
import com.hipoom.processor.transform.registry.InputResult
import java.util.HashMap

/**
 * @author ZhengHaiPeng
 * @since 2024/7/28 10:23 上午
 */
class ScanResult {

    val annotation2Classes = HashMap<String, MutableSet<String>>()

    val interface2Classes = HashMap<String, MutableSet<String>>()

    val jar2Res = HashMap<JarInput, InputResult>()

}


/**
 * 合并两个 map 的 value.
 */
fun HashMap<String, MutableSet<String>>.mergeValueSet(another: HashMap<String, HashSet<String>>) {
    another.forEach { (key, values) ->
        val thisValues = get(key) ?: HashSet()
        thisValues.addAll(values)
        put(key, thisValues)
    }
}