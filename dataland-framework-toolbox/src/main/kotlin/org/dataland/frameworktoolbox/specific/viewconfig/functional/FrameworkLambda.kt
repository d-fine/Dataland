package org.dataland.frameworktoolbox.specific.viewconfig.functional

import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport

/**
 * A base-class for TypeScript Lambdas of the format (dataset: FrameworkDataType): T => BODY
 * @param lambdaBody the body of the lambda function
 * @param returnParameter the return-type of the function in TypeScript
 * @param imports a set of TS imports required for the operation of the lambda function
 */
open class FrameworkLambda(
    var lambdaBody: String,
    var returnParameter: String,
    var imports: Set<TypeScriptImport>,
) {
    val usesDataset: Boolean
        get() = lambdaBody.contains("dataset")
}
