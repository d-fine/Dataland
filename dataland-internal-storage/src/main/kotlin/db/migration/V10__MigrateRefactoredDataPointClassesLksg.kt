package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.getOrJsonNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * Performs the migration of the refactored data point classes for the lksg framework
 */
class V10__MigrateRefactoredDataPointClassesLksg {
    /**
     * Performs the migration of the refactored data point classes for lksg data
     */
    fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "lksg",
            this::migrateRefactoredDataPointClasses,
        )
    }

    private val oldToNewFieldNamesForDataSource = mapOf(
        "name" to "fileName",
        "reference" to "fileReference",
    )

    /**
     * Migrates the refactored Data Point Classes for the Lksg framework
     */
    fun migrateRefactoredDataPointClasses(dataTableEntity: DataTableEntity) {
        val dataObject = JSONObject(dataTableEntity.companyAssociatedData.getString("data"))
        migrateDataSourceObjectsInCategoryGovernance(dataObject)
        migrateDataSourceObjectsInCategorySocial(dataObject)
        dataTableEntity.companyAssociatedData.put("data", dataObject.toString())
    }

    /**
     * Performs the migration the category "social"
     */
    private fun migrateDataSourceObjectsInCategoryGovernance(dataObject: JSONObject) {
        val governanceCategoryObject = dataObject.getOrJavaNull("governance") ?: return
        governanceCategoryObject as JSONObject
        val certificationsPoliciesAndResponsibilitiesObject = governanceCategoryObject
            .getOrJavaNull("certificationsPoliciesAndResponsibilities") ?: return
        certificationsPoliciesAndResponsibilitiesObject as JSONObject
        listOf(
            "smetaSocialAuditConcept",
            "riskManagementSystemCertification",
            "fairLaborAssociationCertification",
        ).forEach { certificateCategory ->
            val certificateCategoryObject = certificationsPoliciesAndResponsibilitiesObject
                .getOrJavaNull(certificateCategory) ?: return@forEach
            migrateSingleDataSourceObjectFormParentObject(certificateCategoryObject as JSONObject)
        }
    }

    /**
     * Performs the migration the category social
     */
    private fun migrateDataSourceObjectsInCategorySocial(dataObject: JSONObject) {
        val socialCategoryObject = dataObject.getOrJavaNull("social") ?: return
        socialCategoryObject as JSONObject
        enterSubSubcategoriesAndTriggerMigration(
            socialCategoryObject,
            "childLabor", "childLaborPreventionPolicy",
        )
        enterSubSubcategoriesAndTriggerMigration(
            socialCategoryObject,
            "unequalTreatmentOfEmployment", "fairAndEthicalRecruitmentPolicy",
        )
    }

    private fun enterSubSubcategoriesAndTriggerMigration(
        categoryObject: JSONObject,
        subcategoryKey: String,
        subSubcategoryKey: String,
    ) {
        val subcategoryObject = categoryObject.getOrJavaNull(subcategoryKey) ?: return
        subcategoryObject as JSONObject
        val subSubcategoryObject = subcategoryObject.getOrJavaNull(subSubcategoryKey) ?: return
        migrateSingleDataSourceObjectFormParentObject(subSubcategoryObject as JSONObject)
    }

    /**
     * Migrates one single dataSource Object in the corresponding parent folder
     */
    private fun migrateSingleDataSourceObjectFormParentObject(dataSourceParentObject: JSONObject) {
        val dataSourceObject = dataSourceParentObject.getOrJsonNull("dataSource")
        dataSourceObject as JSONObject
        oldToNewFieldNamesForDataSource.forEach {
            dataSourceObject.put(it.value, dataSourceObject.get(it.key))
            dataSourceObject.remove(it.key)
        }
    }
}
