package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.TemporarilyCachedDataApi
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.PrivateDataManager
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayInputStream

/**
 * Implementation of the controller for delivering and removing temporarily stored data
 * @param publicDataManager service to manage data storage
 */
@RestController
class TemporarilyCachedDataController(
    @Autowired var publicDataManager: DataManager,
    @Autowired var privateDataManager: PrivateDataManager,
) : TemporarilyCachedDataApi {

    override fun getReceivedPublicData(dataId: String): ResponseEntity<String> {
        return ResponseEntity.ok(publicDataManager.selectPublicDataSetFromTemporaryStorage(dataId))
    }

    override fun getReceivedPrivateData(dataId: String): ResponseEntity<String> {
        return ResponseEntity.ok(privateDataManager.selectPrivateDataSetFromTemporaryStorage(dataId))
    }
    override fun getReceivedPrivateDocuments(hash: String): ResponseEntity<InputStreamResource> {
        val blob = privateDataManager.retrieveDocumentsromMemoryStore(hash)
            ?: throw ResourceNotFoundApiException(
                "Documents for hash \"$hash\" not found in temporary storage",
                "Dataland does not know the files associated to \"$hash\"",
            )
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(InputStreamResource(ByteArrayInputStream(blob)))
    }
}
