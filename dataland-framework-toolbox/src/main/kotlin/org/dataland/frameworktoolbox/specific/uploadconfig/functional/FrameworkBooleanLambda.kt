package org.dataland.frameworktoolbox.specific.uploadconfig.functional

/**
 * A TypeScript lambda of the format (dataset: FrameworkDataType): boolean => BODY
 */
class FrameworkBooleanLambda(lambdaBody: String, imports: Set<String> = emptySet()) : FrameworkLambda(
    lambdaBody = lambdaBody,
    returnParameter = "boolean",
    imports = imports,
) {
    companion object {
        val TRUE = FrameworkBooleanLambda("true")
        val FALSE = FrameworkBooleanLambda("false")
    }
}
