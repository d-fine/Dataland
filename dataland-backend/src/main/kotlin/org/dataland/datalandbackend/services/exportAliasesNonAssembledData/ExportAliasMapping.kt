package org.dataland.datalandbackend.services.exportAliasesNonAssembledData

/**
 *  An interface for the hardcoded mapping fieldname to export alias
 */
interface ExportAliasMapping {
    val fieldNameToReadableName: Map<String, String>
}
