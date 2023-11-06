package org.dataland.frameworktoolbox

import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.DateComponent
import org.dataland.frameworktoolbox.intermediate.components.YesNoComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.intermediate.group.delete
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.template.ExcelTemplate
import org.dataland.frameworktoolbox.template.TemplateComponentBuilder
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.template.components.TemplateComponentFactory
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.io.File
import java.nio.file.Path

fun parseFrameworkExcelFile(): ExcelTemplate {
    // STEP 1: Parse EXCEL
    val frameworkTemplateFile =
        File("/home/dfine/dataland_project/dataland_developer_tools_ng/dataland-framework-toolbox/csv-input/dataDictionary-New PtP.csv")
    val excelTemplate = ExcelTemplate.fromCsv(frameworkTemplateFile)

    // STEP 2: Customize parsed EXCEL using framework-specific stuff (if required)
    // FYI: This is just a useless example that demonstrates would you could do here
    excelTemplate.rows.find { it.fieldIdentifier == "2" }?.fieldName = "Sectors"

    return excelTemplate
}

fun convertExcelToHighLevelComponentRepresentation(context: ApplicationContext, template: ExcelTemplate): Framework {
    // Retrieve converts dynamically from Spring Context (can, therefore, be customized using Spring beans)
    val componentFactories = context.getBeansOfType<TemplateComponentFactory>().values.toList()
    val componentGenerationUtils = context.getBean<ComponentGenerationUtils>()

    // STEP 3: Convert EXCEL --> Intermediate Framework Representation
    val p2pFramework = Framework("newp2p")

    // Register custom converter that in this case just ignores all unknown fields.
    // But you could also register framework-specific converters for fields that only occur in that framework
    // (e.g., LKSG Production-Sites or EU-Taxo Acitivities)
    val noopComponentFactory = object : TemplateComponentFactory {
        override fun canGenerateComponent(row: TemplateRow): Boolean = true

        override fun generateComponent(
            row: TemplateRow,
            utils: ComponentGenerationUtils,
            componentGroup: ComponentGroupApi,
        ) {
            println(row.component)
            // NOOP
        }
    }

    val intermediateBuilder = TemplateComponentBuilder(
        template = template,
        componentFactories = componentFactories + noopComponentFactory,
        generationUtils = componentGenerationUtils,
    )
    intermediateBuilder.build(into = p2pFramework.root)
    return p2pFramework
}

fun compileDataModelAndIntegrateItIntoTheDatalandBackend(framework: Framework) {
    val datalandProject = DatalandRepository(
        Path.of("./"),
    )

    // Build Lower-Level models and integrate them into Dataland.
    // In this step:
    // A) The data-classes are generated and copied into the respective directories of the backend
    // B) The backend is re-compiled to verify that the generation succeeded
    // C) The OpenApi Spec is re-generated
    // Afterward, you can launch the backend and the new framework is live (at least via the API) ;)
    val dataModel = framework.generateDataModel()
    println(dataModel.rootPackageBuilder)
    dataModel.build(into = datalandProject)
}

fun main() {
    // Spring Context setup
    val context = AnnotationConfigApplicationContext(SpringConfig::class.java)

    val excelTemplate = parseFrameworkExcelFile()
    val p2pFramework = convertExcelToHighLevelComponentRepresentation(context, excelTemplate)

    // Customize intermediate representation
    // In this example, override the kotlin data-type of general.general.dataType to be a String instead of a Date
    // Additionally, the field general.governance.organizationalResponsibilityForParisCompatibility is deleted to
    // showcase the flexibility you have using this API
    p2pFramework.root.edit<ComponentGroup>("general") {
        edit<ComponentGroup>("general") {
            edit<DateComponent>("dataDate") {
                dataModelGenerator = {
                    it.addProperty(
                        this.identifier,
                        TypeReference("String", isNullable),
                    )
                }
            }
            create<DateComponent>("testDate") {
            }
        }
        edit<ComponentGroup>("governance") {
            delete<YesNoComponent>("organisationalResponsibilityForParisCompatibility")
        }
    }

    compileDataModelAndIntegrateItIntoTheDatalandBackend(p2pFramework)
}
