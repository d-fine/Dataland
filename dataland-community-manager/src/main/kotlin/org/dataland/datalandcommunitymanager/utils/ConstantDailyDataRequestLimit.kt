package org.dataland.datalandcommunitymanager.utils

private const val MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER_DEFAULT = 10

private val getEnv = System.getenv("MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER")
val MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER = if (getEnv.isNullOrEmpty()) {
    MAX_NUMBER_OF_DATA_REQUESTS_PER_DAY_FOR_ROLE_USER_DEFAULT
} else {
    getEnv.toInt()
}
