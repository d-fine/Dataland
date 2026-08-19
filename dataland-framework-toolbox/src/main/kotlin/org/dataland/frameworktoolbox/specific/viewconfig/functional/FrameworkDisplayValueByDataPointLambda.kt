package org.dataland.frameworktoolbox.specific.viewconfig.functional

import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * A TypeScript lambda of the format (datapoint: String): AvailableMLDTDisplayObjectTypes => BODY
 */
class FrameworkDisplayValueByDataPointLambda(
    lambdaBody: String,
    imports: Set<TypeScriptImport> = emptySet(),
) : FrameworkLambda(
        lambdaBody = lambdaBody,
        returnParameter = "AvailableMLDTDisplayObjectTypes",
        imports = imports,
    )
