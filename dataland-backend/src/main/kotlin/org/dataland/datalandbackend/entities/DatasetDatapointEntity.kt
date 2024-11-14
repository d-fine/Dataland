package org.dataland.datalandbackend.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * The database entity for connecting a dataset and the datapoints the dataset is composed of.
 */
@Entity
@Table(name = "dataset_datapoint")
data class DatasetDatapointEntity(
    @Id
    @Column(name = "data_id")
    val dataId: String,
    @Column(name = "data_points")
    val dataPoints: String,
)
