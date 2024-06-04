package org.dataland.datalandbatchmanager.model

import com.fasterxml.jackson.annotation.JsonProperty

enum class GleifRelationshipTypes {
    @JsonProperty("IS_FUND-MANAGED_BY")
    IS_FUNDMANAGED_BY,
    @JsonProperty("IS_SUBFUND_OF")
    IS_SUBFUND_OF,
    @JsonProperty("IS_DIRECTLY_CONSOLIDATED_BY")
    IS_DIRECTLY_CONSOLIDATED_BY,
    @JsonProperty("IS_ULTIMATELY_CONSOLIDATED_BY")
    IS_ULTIMATELY_CONSOLIDATED_BY,
    @JsonProperty("IS_INTERNATIONAL_BRANCH_OF")
    IS_INTERNATIONAL_BRANCH_OF,
    @JsonProperty("IS_FEEDER_TO")
    IS_FEEDER_TO
}
