package org.dataland.frameworktoolbox.frameworks.additionalcompanyinformation

import java.io.File
import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.springframework.stereotype.Component

/** The additional company information framework */
@Component
class AdditionalCompanyInformationFramework :
  InDevelopmentPavedRoadFramework(
    identifier = "additional-company-information",
    label = "Additional Company Information",
    explanation = "Additional Company Information",
    File(
      "./dataland-framework-toolbox/inputs/additional-company-information/additional-company-information.xlsx"
    ),
    order = 10,
    enabledFeatures = FrameworkGenerationFeatures.ENTRY_SET,
  )
