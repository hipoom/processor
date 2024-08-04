package com.hipoom.processor.transform.timing

import com.hipoom.processor.transform.timing.config.BlackListConfig
import com.hipoom.processor.transform.timing.config.WhiteListConfig

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
     * 白名单。
     * 必须是白名单中的类，才会参与插桩。
     */
    var whitelist = WhiteListConfig()

    fun whitelist(block: org.gradle.api.Action<WhiteListConfig>) {
        block.execute(whitelist)
    }

    /**
     * 类的全类名的前缀，如果满足 [blacklist] 中的任意一个，就不会插桩。
     */
    var blacklist = BlackListConfig()

    fun blacklist(block: org.gradle.api.Action<BlackListConfig>) {
        block.execute(blacklist)
    }
}