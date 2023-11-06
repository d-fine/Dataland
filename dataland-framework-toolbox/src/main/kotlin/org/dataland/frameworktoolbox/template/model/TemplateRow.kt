package org.dataland.frameworktoolbox.template.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.dataland.frameworktoolbox.utils.shortSha

/**
 * A TemplateRow represents a single row of the template Excel that is used
 * to describe frameworks
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class TemplateRow(
    @JsonProperty("Field Identifier")
    var fieldIdentifier: String,

    @JsonProperty("Category")
    var category: String,

    @JsonProperty("Sub-Category")
    var subCategory: String,

    @JsonProperty("Field Name")
    var fieldName: String,

    @JsonProperty("Tooltip")
    var tooltip: String,

    @JsonProperty("Component")
    var component: String,

    @JsonProperty("Options")
    var options: String,

    @JsonProperty("Unit")
    var unit: String,

    @JsonProperty("Document-Support")
    var documentSupport: TemplateDocumentSupport,

    @JsonProperty("Dependency")
    var dependency: String,

    @JsonProperty("Show when value is")
    var showWhenValueIs: String,

    @JsonProperty("Mandatory Field")
    var mandatoryField: TemplateYesNo,
) {
    /**
     * Calculate the first 8 characters of the SHA2-256 checksum of this DataClasses toString representation
     */
    fun shortSha(): String {
        return toString().shortSha()
    }
}
