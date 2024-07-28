package com.hipoom.processor.transform.registry.incremental

import com.hipoom.processor.transform.registry.RegistryTransformConfig
import java.util.LinkedList

/**
 * @author ZhengHaiPeng
 * @since 2024/7/28 13:44 下午
 */
class CacheData {
    var date: String? = null
    var config: RegistryTransformConfig? = null
    var jarPathContainsRegistry: String? = null
    var jarPath2Results = LinkedList<InputCache>()
}
