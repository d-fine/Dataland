package org.dataland.frameworktoolbox.specific.viewconfig.functional

import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * A TypeScript lambda of the format (dataset: FrameworkDataType): boolean => BODY
 */
class FrameworkBooleanLambda(
    lambdaBody: String,
    imports: Set<TypeScriptImport> = emptySet(),
) : FrameworkLambda(
        lambdaBody = lambdaBody,
        returnParameter = "boolean",
        imports = imports,
    ) {
    companion object {
        val TRUE = FrameworkBooleanLambda("true")
        val FALSE = FrameworkBooleanLambda("false")
    }
}
