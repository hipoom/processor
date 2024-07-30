package com.hipoom.processor.common.scan.filter


operator fun FileFilter.plus(another: FileFilter): FileFilter {
    return FileFilter {
        if (needIgnore(it)) {
            return@FileFilter true
        }

        if (another.needIgnore(it)) {
            return@FileFilter true
        }

        return@FileFilter false
    }
}

/**
 * 只需要 .class 文件。
 */
val onlyNeedClassFile = FileFilter {
    return@FileFilter !it.path.endsWith(".class")
}

/**
 * 忽略 R.class 文件。
 */
val ignoreRClassFile = FileFilter {
    return@FileFilter it.path.endsWith("/R.class")
}

/**
 * 默认的文件过滤器。
 * 只要 .class 文件，且不要 R.class 文件。
 */
val defaultFileFilter = onlyNeedClassFile + ignoreRClassFile



operator fun JarEntryFilter.plus(another: JarEntryFilter): JarEntryFilter {
    return JarEntryFilter {
        if (needIgnore(it)) {
            return@JarEntryFilter true
        }

        if (another.needIgnore(it)) {
            return@JarEntryFilter true
        }

        return@JarEntryFilter false
    }
}


/**
 * 只需要 .class 文件。
 */
val onlyNeedClassEntry = JarEntryFilter {
    return@JarEntryFilter !it.name.endsWith(".class")
}

/**
 * 不需要 directory.
 */
val ignoreDirectoryEntry = JarEntryFilter {
    return@JarEntryFilter it.isDirectory
}
