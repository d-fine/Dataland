package org.dataland.frameworktoolbox.specific.uploadconfig.functional

/**
 * A TypeScript lambda of the format (dataset: FrameworkDataType): AvailableMLDTDisplayObjectTypes => BODY
 */
class FrameworkDisplayValueLambda(lambdaBody: String, imports: Set<String> = emptySet()) : FrameworkLambda(
    lambdaBody = lambdaBody,
    returnParameter = "AvailableMLDTDisplayObjectTypes",
    imports = imports,
)
