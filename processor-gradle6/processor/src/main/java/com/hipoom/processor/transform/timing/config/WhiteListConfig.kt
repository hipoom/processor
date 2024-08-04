package com.hipoom.processor.transform.timing.config

/**
 * @author ZhengHaiPeng
 * @since 2024/7/29 21:43
 */
open class WhiteListConfig {

    /* ======================================================= */
    /* Fields                                                  */
    /* ======================================================= */

    /**
     * 前缀。
     * 全类名必须满足以 [prefix] 开头，才会参与插桩。
     */
    var prefix: Set<String>? = null

    /**
     * 全类名中，必须包含 [contains] 关键字，才会参与插桩。
     */
    var contains: Set<String>? = null
}