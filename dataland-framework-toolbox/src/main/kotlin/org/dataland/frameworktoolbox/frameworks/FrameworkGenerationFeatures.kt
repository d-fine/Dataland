package org.dataland.frameworktoolbox.frameworks

import java.util.Collections

/**
 * An enum that contains different features available during code generation.
 */
enum class FrameworkGenerationFeatures {
    BackendDataModel, QaModel, BackendApiController, ViewPage, UploadPage, FakeFixtures;

    companion object {
        val ENTRY_SET: Set<FrameworkGenerationFeatures> = Collections.unmodifiableSet(entries.toSet())

        /**
         * Returns a set of all features except the ones specified.
         * @param without the features to excludes
         */
        fun allExcept(vararg without: FrameworkGenerationFeatures): Set<FrameworkGenerationFeatures> {
            return ENTRY_SET - without.toSet()
        }
    }
}
