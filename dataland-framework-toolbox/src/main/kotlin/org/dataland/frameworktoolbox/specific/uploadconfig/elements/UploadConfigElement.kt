package org.dataland.frameworktoolbox.specific.uploadconfig.elements

import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport
import java.util.function.Consumer

/**
 * A single element of a MLDT View-Configuration (either Cell or Section)
 */
sealed interface UploadConfigElement {
    val parentSection: UploadCategoryBuilder?
    val imports: Set<TypeScriptImport>

    /**
     * When this function passes without errors, it is guaranteed that this config element
     * is compliant with the limitations of the legacy upload page and code generation can proceed
     */
    fun assertComplianceWithLegacyUploadPage()

    /**
     * Traverses the component tree in pre-order fashion and execute the provided function
     * for every element
     */
    fun traverse(lambda: Consumer<UploadConfigElement>)
}
