package com.hipoom.processor.transform.timing.scan;

import java.util.jar.JarEntry;

import com.android.annotations.NonNull;

/**
 * @author ZhengHaiPeng
 * @since 2024/7/30 00:20
 */
public interface EntryFilter {

    /* ======================================================= */
    /* Public Methods                                          */
    /* ======================================================= */

    /**
     * 是否需要忽略遍历时遇到的 jar 包。
     */
    boolean needIgnore(@NonNull JarEntry entry);
}
