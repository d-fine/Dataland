package org.dataland.frameworktoolbox.frameworks.nuclearandgas
import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.frameworks.nuclearandgas.custom.CustomComponentFactory
import org.dataland.frameworktoolbox.specific.datamodel.FrameworkDataModelBuilder
import org.dataland.frameworktoolbox.template.TemplateDiagnostic
import org.dataland.frameworktoolbox.template.components.TemplateComponentFactory
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.io.File

/**
 * The EU Taxonomy Nuclear And Gas Framework
 */
@Component
class NuclearAndGasFramework : PavedRoadFramework(
    identifier = "nuclear-and-gas",
    label = "EU Taxonomy Nuclear and Gas Framework",
    explanation = "EU Taxonomy Nuclear and Gas Framework according to the Commission Delegated Regulation (EU)" +
        " 2021/2178, Annex XII ",
    File("./dataland-framework-toolbox/inputs/nuclear-and-gas/nuclear-and-gas.xlsx"),
    order = 9,
    enabledFeatures = FrameworkGenerationFeatures.ENTRY_SET,
) {

    private lateinit var customComponentFactories: List<CustomComponentFactory>

    override fun getComponentFactoriesForIntermediateRepresentation(
        context: ApplicationContext,
    ): List<TemplateComponentFactory> {
        val superFactories = super.getComponentFactoriesForIntermediateRepresentation(context)
        val templateDiagnostic = context.getBean<TemplateDiagnostic>()
        customComponentFactories = CustomComponentFactory.fromExcel(frameworkTemplateCsvFile, templateDiagnostic)
        customComponentFactories.forEach { it.build(superFactories) }
        return customComponentFactories + superFactories
    }

    /**
     * TODOs:
     * Clean up the code, better naming for custom components, also generated file names contain nuclear and gas twice
     * 3. We need to generate a mapping file. That creates a map from the data points in the component
     *    To the tooltips, that is then stored as ts file somewhere
     *    export const Mapping { fieldName: "Super nice explanation of the field" }
     *    Maybe we want to use a ftl template, maybe not
     */


    override fun customizeDataModel(dataModel: FrameworkDataModelBuilder) {
        val customPackage = dataModel.rootPackageBuilder.addPackage(CustomComponentFactory.PACKAGE_NAME)
        customComponentFactories.forEach {
            it.addClassToPackageBuilder(customPackage)
        }
    }

}
