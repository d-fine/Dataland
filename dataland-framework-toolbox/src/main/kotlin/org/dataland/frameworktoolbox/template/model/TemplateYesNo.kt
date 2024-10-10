package org.dataland.frameworktoolbox.template.model

import com.fasterxml.jackson.annotation.JsonCreator
import java.lang.IllegalArgumentException

/**
 * Yes or No: That is the question!
 */
enum class TemplateYesNo {
    Yes,
    No,
    ;

    companion object {
        /**
         * Parse a string to a Yes/No enum. A blank string is treated as "No".
         */
        @JsonCreator
        @JvmStatic
        fun fromString(input: String): TemplateYesNo =
            if (input == "Yes") {
                Yes
            } else if (input.isBlank() || input == "No") {
                No
            } else {
                throw IllegalArgumentException(
                    "Cannot convert '$input' to a YesNo value. Please Specify one of 'Yes' or 'No'",
                )
            }
    }
}
