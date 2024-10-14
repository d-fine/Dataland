package org.dataland.frameworktoolbox.frameworks.nuclearandgas

import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.springframework.stereotype.Component
import java.io.File

/**
 * The EU Taxonomy Nuclear And Gas Framework
 */
@Component
class NuclearAndGasFramework :
    PavedRoadFramework(
        identifier = "nuclear-and-gas",
        label = "EU Taxonomy Nuclear and Gas Framework",
        explanation =
            "EU Taxonomy Nuclear and Gas Framework according to the Commission Delegated Regulation (EU)" +
                " 2021/2178, Annex XII ",
        File("./dataland-framework-toolbox/inputs/nuclear-and-gas/nuclear-and-gas.xlsx"),
        order = 9,
        enabledFeatures = FrameworkGenerationFeatures.ENTRY_SET,
    ) {
//    private lateinit var nuclearAndGasTemplateComponentFactories: List<NuclearAndGasTemplateComponentFactory>
//
//    override fun getComponentFactoriesForIntermediateRepresentation(context: ApplicationContext): List<TemplateComponentFactory> {
//        val allComponentFactories = super.getComponentFactoriesForIntermediateRepresentation(context)
//
//        val templateDiagnostic = context.getBean<TemplateDiagnostic>()
//
//        nuclearAndGasTemplateComponentFactories =
//            NuclearAndGasTemplateComponentFactory.fromExcel(
//                frameworkTemplateCsvFile,
//                templateDiagnostic,
//                "Nuclear and Gas",
//            )
// //        nuclearAndGasTemplateComponentFactories.forEach { it.buildInternalFramework(superFactories) }
// //        nuclearAndGasTemplateComponentFactories.forEach { it.printTooltips() }
//        return nuclearAndGasTemplateComponentFactories + allComponentFactories
//    }
//
//    override fun customizeDataModel(dataModel: FrameworkDataModelBuilder) {
//        val customPackage = dataModel.rootPackageBuilder.addPackage(NuclearAndGasTemplateComponentFactory.PACKAGE_NAME)
//        nuclearAndGasTemplateComponentFactories.forEach {
//            it.addClassToPackageBuilder(customPackage)
//        }
//    }
//
//    override fun customizeFixtureGenerator(fixtureGenerator: FrameworkFixtureGeneratorBuilder) {
//        nuclearAndGasTemplateComponentFactories.forEach {
//            it.addGeneratorToFixtureGeneratorBuilder(fixtureGenerator)
//        }
//    }
}
