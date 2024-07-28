package com.hipoom.processor

import com.android.build.gradle.AppExtension
import org.gradle.api.Project
import java.io.File


/**
 * Plugin 的名字。
 */
const val PLUGIN_NAME = "hipoom"

/**
 * 工程信息
 */
lateinit var project: Project

/**
 * application 扩展。
 */
lateinit var appExtension: AppExtension

/**
 * 工程的构建目录
 */
val projectBuildDirectory: File
    get() = project.layout.buildDirectory.get().asFile

/**
 * Plugin 的构建目录
 */
val pluginBuildDirectory: File
    get() = File(projectBuildDirectory, PLUGIN_NAME)