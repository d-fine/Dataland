package org.dataland.frameworktoolbox.specific.uploadconfig.functional

/**
 * A helper class to pass typescript code to some property of a field in an Upload Config as a lambda body,
 * as well as import statements at the top of the Upload Config.
 */
class FrameworkUploadOptions(body: String, imports: Set<String>? = emptySet()) : FrameworkUploadBase(
    body = body,
    imports = imports,
)
