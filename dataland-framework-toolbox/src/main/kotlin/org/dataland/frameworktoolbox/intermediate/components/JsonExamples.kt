package org.dataland.frameworktoolbox.intermediate.components

/**
 * Stores constants which do not seem fit to be defined at point of use
 */
object JsonExamples {
    /**
     * Obtain an example with extended document support
     * @param plainTextExample an example for the component without extended document support
     */
    fun exampleExtendedDocumentSupport(plainTextExample: String) =
        """{
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

    const val EXAMPLE_EXTENDED_CURRENCY_COMPONENT = """{
      "value" : 100.5,
      "currency" : "USD",
      "quality" : "Reported",
      "comment" : "The value is reported by the company.",
      "dataSource" : {
      "page" : "5-7",
      "tagName" : "monetaryAmount",
      "fileName" : "AnnualReport2020.pdf",
      "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
    }"""

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

    const val EXAMPLE_PLAIN_EU_TAXONOMY_ALIGNED_ACTIVITIES_COMPONENT = """[{
      "activityName" : "CollectionAndTransportOfNonHazardousWasteInSourceSegregatedFractions",
      "naceCodes" : [ "E.38.11" ],
      "share" : {
        "relativeShareInPercent" : 25,
        "absoluteShare" : {
          "amount" : 100.5,
          "currency" : "EUR"
        }
      },
      "substantialContributionToClimateChangeMitigationInPercent" : 10,
      "substantialContributionToClimateChangeAdaptationInPercent" : 5,
      "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent" : 35,
      "substantialContributionToTransitionToACircularEconomyInPercent" : 70,
      "substantialContributionToPollutionPreventionAndControlInPercent" : 2,
      "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent" : 1,
      "dnshToClimateChangeMitigation" : "Yes",
      "dnshToClimateChangeAdaptation" : "Yes",
      "dnshToSustainableUseAndProtectionOfWaterAndMarineResources" : "Yes",
      "dnshToTransitionToACircularEconomy" : "Yes",
      "dnshToPollutionPreventionAndControl" : "Yes",
      "dnshToProtectionAndRestorationOfBiodiversityAndEcosystems" : "Yes",
      "minimumSafeguards" : "Yes",
      "enablingActivity" : "No",
      "transitionalActivity" : "Yes"
    }, {
      "activityName" : "InfrastructureForRailTransport",
      "naceCodes" : [ "C27.9", "C30.20" ],
      "share" : {
        "relativeShareInPercent" : 25,
        "absoluteShare" : {
          "amount" : 100.5,
          "currency" : "EUR"
        }
      },
      "substantialContributionToClimateChangeMitigationInPercent" : 7,
      "substantialContributionToClimateChangeAdaptationInPercent" : 5,
      "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent" : 35,
      "substantialContributionToTransitionToACircularEconomyInPercent" : 70,
      "substantialContributionToPollutionPreventionAndControlInPercent" : 2,
      "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent" : 1,
      "dnshToClimateChangeMitigation" : "Yes",
      "dnshToClimateChangeAdaptation" : "Yes",
      "dnshToSustainableUseAndProtectionOfWaterAndMarineResources" : "Yes",
      "dnshToTransitionToACircularEconomy" : "Yes",
      "dnshToPollutionPreventionAndControl" : "Yes",
      "dnshToProtectionAndRestorationOfBiodiversityAndEcosystems" : "No",
      "minimumSafeguards" : "Yes",
      "enablingActivity" : "No",
      "transitionalActivity" : "Yes"
    }]"""

    const val EXAMPLE_PLAIN_EU_TAXONOMY_NON_ALIGNED_ACTIVITIES_COMPONENT = """[{
      "activityName" : "CollectionAndTransportOfNonHazardousWasteInSourceSegregatedFractions",
      "naceCodes" : [ "E.38.11" ],
      "share" : {
        "relativeShareInPercent" : 25,
        "absoluteShare" : {
          "amount" : 100.5,
          "currency" : "EUR"
        }
      }
    }, {
      "activityName" : "InfrastructureForRailTransport",
      "naceCodes" : [ "C27.9", "C30.20" ],
      "share" : {
        "relativeShareInPercent" : 25,
        "absoluteShare" : {
          "amount" : 100.5,
          "currency" : "EUR"
        }
      }
    }, {
      "activityName" : "LowCarbonAirportInfrastructure",
      "naceCodes" : [ "F41.20", "F42.99" ],
      "share" : {
        "relativeShareInPercent" : 25,
        "absoluteShare" : {
          "amount" : 100.5,
          "currency" : "EUR"
        }
      }
    }]"""

    const val EXAMPLE_PLAIN_EU_TAXONOMY_REPORTING_ASSURANCE_COMPONENT = """ "LimitedAssurance" """

    const val EXAMPLE_PLAIN_NUCLEAR_AND_GAS_ALIGNED_DENOMINATOR_COMPONENT = """{
      "taxonomyAlignedShareDenominatorNAndG426" : {
        "mitigationAndAdaptation" : 0.2,
        "mitigation" : 0.3,
        "adaptation" : 0.1
      },
      "taxonomyAlignedShareDenominatorNAndG427" : {
        "mitigationAndAdaptation" : 1.2,
        "mitigation" : 0.4,
        "adaptation" : 0.2
      },
      "taxonomyAlignedShareDenominatorNAndG428" : {
        "mitigationAndAdaptation" : 1,
        "mitigation" : null,
        "adaptation" : null
      },
      "taxonomyAlignedShareDenominatorNAndG429" : {
        "mitigationAndAdaptation" : null,
        "mitigation" : null,
        "adaptation" : null
      },
      "taxonomyAlignedShareDenominatorNAndG430" : {
        "mitigationAndAdaptation" : 0.3,
        "mitigation" : 2.4,
        "adaptation" : 0.5
      },
      "taxonomyAlignedShareDenominatorNAndG431" : {
        "mitigationAndAdaptation" : 0.2,
        "mitigation" : 0.5,
        "adaptation" : 0.6
      },
      "taxonomyAlignedShareDenominatorOtherActivities" : {
        "mitigationAndAdaptation" : 1.1,
        "mitigation" : 1.4,
        "adaptation" : 1.1
      },
      "taxonomyAlignedShareDenominator" : {
        "mitigationAndAdaptation" : 4,
        "mitigation" : 5,
        "adaptation" : 2.5
      }
    }"""

    const val EXAMPLE_PLAIN_NUCLEAR_AND_GAS_ALIGNED_NUMERATOR_COMPONENT = """{
      "taxonomyAlignedShareNumeratorNAndG426" : {
        "mitigationAndAdaptation" : 5,
        "mitigation" : 6,
        "adaptation" : 4
      },
      "taxonomyAlignedShareNumeratorNAndG427" : {
        "mitigationAndAdaptation" : 30,
        "mitigation" : 8,
        "adaptation" : 8
      },
      "taxonomyAlignedShareNumeratorNAndG428" : {
        "mitigationAndAdaptation" : 25,
        "mitigation" : null,
        "adaptation" : null
      },
      "taxonomyAlignedShareNumeratorNAndG429" : {
        "mitigationAndAdaptation" : null,
        "mitigation" : null,
        "adaptation" : null
      },
      "taxonomyAlignedShareNumeratorNAndG430" : {
        "mitigationAndAdaptation" : 7.5,
        "mitigation" : 48,
        "adaptation" : 20
      },
      "taxonomyAlignedShareNumeratorNAndG431" : {
        "mitigationAndAdaptation" : 5,
        "mitigation" : 10,
        "adaptation" : 24
      },
      "taxonomyAlignedShareNumeratorOtherActivities" : {
        "mitigationAndAdaptation" : 27.5,
        "mitigation" : 28,
        "adaptation" : 44
      },
      "taxonomyAlignedShareNumerator" : {
        "mitigationAndAdaptation" : 100,
        "mitigation" : 100,
        "adaptation" : 100
      }
    }"""

    const val EXAMPLE_PLAIN_NUCLEAR_AND_GAS_ELIGIBLE_BUT_NOT_ALIGNED_COMPONENT = """{
      "taxonomyEligibleButNotAlignedShareNAndG426" : {
        "mitigationAndAdaptation" : 1.2,
        "mitigation" : 1.3,
        "adaptation" : 1.1
      },
      "taxonomyEligibleButNotAlignedShareNAndG427" : {
        "mitigationAndAdaptation" : 2.2,
        "mitigation" : 1.4,
        "adaptation" : 1.2
      },
      "taxonomyEligibleButNotAlignedShareNAndG428" : {
        "mitigationAndAdaptation" : 2,
        "mitigation" : 1,
        "adaptation" : 1
      },
      "taxonomyEligibleButNotAlignedShareNAndG429" : {
        "mitigationAndAdaptation" : null,
        "mitigation" : null,
        "adaptation" : null
      },
      "taxonomyEligibleButNotAlignedShareNAndG430" : {
        "mitigationAndAdaptation" : 1.3,
        "mitigation" : 3.4,
        "adaptation" : 1.5
      },
      "taxonomyEligibleButNotAlignedShareNAndG431" : {
        "mitigationAndAdaptation" : 1.2,
        "mitigation" : 1.5,
        "adaptation" : 1.6
      },
      "taxonomyEligibleButNotAlignedShareOtherActivities" : {
        "mitigationAndAdaptation" : 2.2,
        "mitigation" : 2.1,
        "adaptation" : 2
      },
      "taxonomyEligibleButNotAlignedShare" : {
        "mitigationAndAdaptation" : 10.1,
        "mitigation" : 10.7,
        "adaptation" : 8.4
      }
    }"""

    const val EXAMPLE_PLAIN_NUCLEAR_AND_GAS_NON_ELIGIBLE_COMPONENT = """{
      "taxonomyNonEligibleShareNAndG426" : 1.1,
      "taxonomyNonEligibleShareNAndG427" : 1.2,
      "taxonomyNonEligibleShareNAndG428" : 0.6,
      "taxonomyNonEligibleShareNAndG429" : 0.5,
      "taxonomyNonEligibleShareNAndG430" : 2.8,
      "taxonomyNonEligibleShareNAndG431" : 1.2,
      "taxonomyNonEligibleShareOtherActivities" : 1.9,
      "taxonomyNonEligibleShare" : 9.3
    }"""
}
