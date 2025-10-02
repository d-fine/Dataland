package org.dataland.datalandbackend.utils

import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.springframework.test.context.bean.override.mockito.MockitoBean

/**
 * Annotation for creating default mock beans for external services used by the backend
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MockitoBean(types = [DocumentControllerApi::class, SpecificationControllerApi::class])
annotation class DefaultMocks
