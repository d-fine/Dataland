package org.dataland.datalandinternalstorage.repositories

import org.dataland.datalandinternalstorage.entities.DataPointItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * A JPA repository for accessing the data point items
 */
interface DataPointItemRepository : JpaRepository<DataPointItem, String> {
    @Query(value = "SELECT * FROM data_point_item WHERE id = ANY(:ids)", nativeQuery = true)
    fun findAllByIdArray(
        @Param("ids") ids: Array<String>,
    ): List<DataPointItem>
}
