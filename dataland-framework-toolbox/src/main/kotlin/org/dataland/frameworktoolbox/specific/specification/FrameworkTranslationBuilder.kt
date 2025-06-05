package org.dataland.frameworktoolbox.specific.specification

import org.dataland.datalandspecification.specifications.FrameworkTranslation
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.utils.DatalandRepository

/**
 * A builder for a framework translation
 */
class FrameworkTranslationBuilder(
    framework: Framework,
    datalandRepository: DatalandRepository,
) : FrameworkBuilder(framework, datalandRepository) {
    private fun buildFrameworkTranslation() {
        database.translations.remove(framework.identifier)

        val frameworkTranslation =
            FrameworkTranslation(
                id = framework.identifier,
                schema = rootCategoryBuilder.toJsonNode(),
            )

        database.translations[this.framework.identifier] = frameworkTranslation
    }

    /**
     * Build the framework translation and save it to the repository
     */
    fun build() {
        buildFrameworkTranslation()
        database.saveToDisk()
    }
}
