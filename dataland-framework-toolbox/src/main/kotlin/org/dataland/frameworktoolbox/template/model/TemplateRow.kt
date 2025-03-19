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
    var combinedTooltip: String?,
    @JsonProperty("Tooltip - upload page")
    val uploadPageTooltip: String? = null,
    @JsonProperty("Tooltip - view page (if different from upload page)")
    val viewPageTooltip: String? = null,
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
    @JsonProperty("Data Point Type Name Overwrite")
    var dataPointTypeNameOverwrite: String? = null,
    @JsonProperty("Include Category in Type Name")
    var includeCategoryInTypeName: TemplateYesNo,
    @JsonProperty("Include Sub-Category in Type Name")
    var includeSubCategoryInTypeName: TemplateYesNo,
) {
    /**
     * Calculate the first 8 characters of the SHA2-256 checksum of this DataClasses toString representation
     */
    fun shortSha(): String = toString().shortSha()
}
