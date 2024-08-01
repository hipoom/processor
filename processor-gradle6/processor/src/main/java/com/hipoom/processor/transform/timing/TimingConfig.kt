package com.hipoom.processor.transform.timing

/**
 * @author ZhengHaiPeng
 * @since 2024/7/29 21:33
 *
 */
open class TimingConfig {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    /**
     * 是否需要启动方法耗时插桩。 默认是不启动。
     */
    var enable: Boolean = false

    /**
     * 类的全类名的前缀，如果满足 [blackList] 中的任意一个，就不会插桩。
     */
    var blackList: Set<String>? = null
}