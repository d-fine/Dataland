import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole

@Converter(autoApply = true)
class CompanyRoleConverter : AttributeConverter<CompanyRole, String> {
    override fun convertToDatabaseColumn(attribute: CompanyRole) = attribute.name

    override fun convertToEntityAttribute(dbData: String) = CompanyRole.valueOf(dbData)
}
