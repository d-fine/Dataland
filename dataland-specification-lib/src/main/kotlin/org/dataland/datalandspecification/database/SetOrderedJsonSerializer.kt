package org.dataland.datalandspecification.database

import com.fasterxml.jackson.databind.util.StdConverter

/**
 * JSON serializer that sorts the keys of a set before serializing it.
 */
class SetOrderedJsonSerializer : StdConverter<Set<String>, Set<String>>() {
    /**
     * Convert a set to a sorted set for serialization.
     */
    override fun convert(input: Set<String>?): Set<String>? = input?.toSortedSet()
}
