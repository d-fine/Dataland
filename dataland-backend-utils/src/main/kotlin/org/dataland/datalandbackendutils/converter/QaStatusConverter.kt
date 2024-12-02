package org.dataland.datalandbackendutils.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.dataland.datalandbackendutils.model.QaStatus

/**
 * Converts QaStatus entries in Entities to database values and vice versa
 */
@Converter
class QaStatusConverter : AttributeConverter<QaStatus?, String> {
    override fun convertToDatabaseColumn(qaStatus: QaStatus?): String = qaStatus.toString()

    override fun convertToEntityAttribute(qaStatusAsString: String): QaStatus = QaStatus.valueOf(qaStatusAsString)
}
