package org.dataland.datalandbackend

const val RATIO_PRECISION = 4

const val TEASER_COMPANY_INDEX_IN_FIXTURES: Int = 2

val REALDATA: Boolean = System.getenv("REALDATA").toBoolean()
val REAL_TEASER_COMPANY_PERM_ID: String = System.getenv("REAL_TEASER_COMPANY_PERM_ID")
