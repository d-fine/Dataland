package org.dataland.frameworktoolbox.template.components

import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.dataland.frameworktoolbox.utils.diagnostic.DiagnosticManager
import org.dataland.frameworktoolbox.utils.shortSha
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * A component factory with the lowest precedence that issues warnings alerting users to
 * the absence of a suitable component factory
 */
@Order(0)
@Component
class WarningComponentFactory(
    @Autowired val diagnostic: DiagnosticManager,
) : TemplateComponentFactory {
    override fun canGenerateComponent(row: TemplateRow): Boolean = true

    override fun generateComponent(
        row: TemplateRow,
        utils: ComponentGenerationUtils,
        componentGroup: ComponentGroupApi,
    ): ComponentBase? {
        diagnostic.warning(
            "IgnoredRow-${row.fieldIdentifier}-${row.toString().shortSha()}",
            "No-one wants to generate components for ${row.component} (Row $row)",
        )
        return null
    }

    override fun updateDependency(
        row: TemplateRow,
        utils: ComponentGenerationUtils,
        componentIdentifierMap: Map<String, ComponentBase>,
    ) {
        // NOOP
    }
}
