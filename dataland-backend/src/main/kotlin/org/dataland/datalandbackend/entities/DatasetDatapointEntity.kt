package org.dataland.datalandbackend.entities

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapKeyColumn
import jakarta.persistence.Table

/**
 * The database entity for connecting a dataset and the datapoints the dataset is composed of.
 */
@Entity
@Table(name = "dataset_datapoint")
data class DatasetDatapointEntity(
    @Id
    @Column(name = "dataset_id")
    val datasetId: String,
    @ElementCollection
    @CollectionTable(name = "data_point_uuid_map", joinColumns = [JoinColumn(name = "dataset_id")])
    @MapKeyColumn(name = "data_point_identifier")
    @Column(name = "data_point_uuid")
    val dataPoints: Map<String, String>
)
