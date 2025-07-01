package org.dataland.frameworktoolbox.intermediate.components

/**
 * Stores constants which do not seem fit to be defined at point of use
 */
object JsonExamples {
    const val EXAMPLE_EXTENDED_DOCUMENT_SUPPORT = """
        "quality" : "Reported",
        "comment" : "The value is reported by the company."
        "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
    }"""

    const val EXAMPLE_PLAIN_DATE_COMPONENT = """{
    "value" : "2007-03-05" """

    const val EXAMPLE_PLAIN_DECIMAL_COMPONENT = """{
        "value" : 100.5"""

    const val EXAMPLE_PLAIN_YES_NO_COMPONENT = """{
    "value" : "Yes" """

    const val EXAMPLE_PLAIN_INTEGER_COMPONENT = """{
    "value" : 100 """

    const val EXAMPLE_PLAIN_PERCENTAGE_COMPONENT = """{
        "value": 13.52 """

    const val EXAMPLE_PLAIN_LIST_OF_STRING_BASE_DATA_POINT_COMPONENT = """[
						{
							"value": "lifetime value",
							"dataSource": {
								"fileName": "Certification",
								"fileReference": "1902e40099c913ecf3715388cb2d9f7f84e6f02a19563db6930adb7b6cf22868"
							}
						},
						{
							"value": "technologies",
							"dataSource": {
								"fileName": "Policy",
								"fileReference": "04c4e6cd07eeae270635dd909f58b09b2104ea5e92ec22a80b6e7ba1d0b75dd0"
							}
						}
					]"""

    const val EXAMPLE_PLAIN_SINGLE_SELECT_COMPONENT = """{
    "value" : "Option 1" """
}
