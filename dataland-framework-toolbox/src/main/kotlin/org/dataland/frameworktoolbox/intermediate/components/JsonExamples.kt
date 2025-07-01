package org.dataland.frameworktoolbox.intermediate.components

/**
 * Stores constants which do not seem fit to be defined at point of use
 */
object JsonExamples {
    /**
     * Obtain an example with extended document support
     * @param examplePlainData an example for the component without extended document support
     */
    fun exampleExtendedDocumentSupport(plainTextExample: String) =
        """
            {
            "value" : $plainTextExample, 
            "quality" : "Reported",
            "comment" : "The value is reported by the company."
            "dataSource" : {
            "page" : "5-7",
            "tagName" : "monetaryAmount",
            "fileName" : "AnnualReport2020.pdf",
            "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
            }
    }"""

    const val EXAMPLE_EXTENDED_CURRENCY_COMPONENT = """
        {
        "value" : 100.5,
        "currency" : "USD",
        "quality" : "Reported",
        "comment" : "The value is reported by the company."
        "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
        }
    """

    const val EXAMPLE_PLAIN_DATE_COMPONENT = """ "2007-03-05" """

    const val EXAMPLE_PLAIN_DECIMAL_COMPONENT = "100.5"

    const val EXAMPLE_PLAIN_YES_NO_COMPONENT = """ "Yes" """

    const val EXAMPLE_PLAIN_INTEGER_COMPONENT = "100"

    const val EXAMPLE_PLAIN_PERCENTAGE_COMPONENT = "13.52"

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

    const val EXAMPLE_PLAIN_SINGLE_SELECT_COMPONENT = """ "Option 1" """

    const val EXAMPLE_PLAIN_FREE_TEXT_COMPONENT = """ "This is some free text" """

    const val EXAMPLE_PLAIN_STRING_COMPONENT = """ "This is a string" """

    const val EXAMPLE_PLAIN_ISO_COUNTRY_CODES_MULTI_SELECT_COMPONENT = """ ["TR","VN"] """

    const val EXAMPLE_PLAIN_MULTI_SELECT_COMPONENT = """ ["Option 1","Option 2"] """

    const val EXAMPLE_PLAIN_NACE_CODES_COMPONENT = """ ["47.23", "47.78"] """
}
