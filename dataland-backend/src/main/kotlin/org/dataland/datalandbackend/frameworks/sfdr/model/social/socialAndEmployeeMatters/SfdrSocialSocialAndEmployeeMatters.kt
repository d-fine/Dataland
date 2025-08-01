// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
package org.dataland.datalandbackend.frameworks.sfdr.model.social.socialAndEmployeeMatters

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import org.dataland.datalandbackend.model.datapoints.CurrencyDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.enums.commons.YesNo
import org.dataland.datalandbackend.validator.MaximumValue
import org.dataland.datalandbackend.validator.MinimumValue
import java.math.BigDecimal
import java.math.BigInteger

/**
 * The data-model for the SocialAndEmployeeMatters section
 */
@Suppress("LargeClass", "MaxLineLength")
data class SfdrSocialSocialAndEmployeeMatters(
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Has the company been involved in Human Rights related legal proceedings?""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val humanRightsLegalProceedings: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company abide by the ILO Core Labour Standards?""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val iloCoreLabourStandards: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company have an environmental policy? If yes, please share the policy with us.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val environmentalPolicy: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Has the company been involved in corruption-related legal proceedings?""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val corruptionLegalProceedings: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company have a transparency policy? If yes, please share the policy with us. According to the OECD Guidelines for Multinational Enterprises, multinational companies should inform the public not only about their financial performance, but also about all of the important aspects of their business activities, such as how they are meeting social and environmental standards and what risks they foresee linked to their business activities.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val transparencyDisclosurePolicy: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company have policies in place to support/respect human rights and carry out due diligence to ensure that the business activities do not have a negative human rights impact? If yes, please share the relevant documents with us.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val humanRightsDueDiligencePolicy: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company have policies in place to abolish all forms of child labour? If yes, please share the policy with us.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val policyAgainstChildLabour: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company have policies in place to abolish all forms of forced labour? If yes, please share the policy with us.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val policyAgainstForcedLabour: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company have policies in place to eliminate discrimination in the workplace? If yes, please share the policy with us.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val policyAgainstDiscriminationInTheWorkplace: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Is the company ISO 14001 certified (Environmental Management)? If yes, please share the certificate with us.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val iso14001Certificate: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company have a policy on anti-corruption and anti-bribery consistent with the United Nations Convention against Corruption? (See Regulation (EU) 2022/1288, Annex I, table 3, indicator nr. 15.) If yes, please share the policy with us.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val policyAgainstBriberyAndCorruption: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company have policies and procedures in place to apply fair business, marketing and advertising practices and to guarantee the safety and quality of the goods and services? If yes, please share the relevant documents with us.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val fairBusinessMarketingAdvertisingPolicy: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company have policies and procedures in place to permit the transfer and rapid dissemination of technologies and expertise? If yes, please share the relevant documents with us.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val technologiesExpertiseTransferPolicy: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company have policies and procedures in place related to fair competition and anti-competitive cartels? If yes, please share the relevant documents with us.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val fairCompetitionPolicy: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Is the company involved in a violation of OECD Guidelines for Multinational Enterprises for Taxation? In the field of taxation, multinational enterprises should make their contribution to public finances within the framework of applicable law and regulations, in accordance with the tax rules and regulations of the host countries, and should cooperate with the tax authorities.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val violationOfTaxRulesAndRegulation: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company have a policy to monitor compliance with the UNGC principles or OECD Guidelines for Multinational Enterprises? (See Regulation (EU) 2022/1288, Annex I, top (22) and table 1, indicator nr. 11.) If yes, please share the relevant documents with us.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val unGlobalCompactPrinciplesCompliancePolicy: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company have grievance/complaints handling mechanisms to address violations of the UNGC principles or OECD Guidelines for Multinational Enterprises? (See Regulation (EU) 2022/1288, Annex I, top (22) and table 1, indicator nr. 11.) If yes, please share the policy with us.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val oecdGuidelinesForMultinationalEnterprisesGrievanceHandling: ExtendedDataPoint<YesNo?>? = null,
    @field:MinimumValue(minimumValue = 0)
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Average gross hourly earnings of male employees""",
        example = """{
      "value" : 100.5,
      "currency" : "USD",
      "quality" : "Reported",
      "comment" : "The value is reported by the company.",
      "dataSource" : {
      "page" : "5-7",
      "tagName" : "monetaryAmount",
      "fileName" : "AnnualReport2020.pdf",
      "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
    } """,
    )
    val averageGrossHourlyEarningsMaleEmployees: CurrencyDataPoint? = null,
    @field:MinimumValue(minimumValue = 0)
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Average gross hourly earnings of female employees""",
        example = """{
      "value" : 100.5,
      "currency" : "USD",
      "quality" : "Reported",
      "comment" : "The value is reported by the company.",
      "dataSource" : {
      "page" : "5-7",
      "tagName" : "monetaryAmount",
      "fileName" : "AnnualReport2020.pdf",
      "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
    } """,
    )
    val averageGrossHourlyEarningsFemaleEmployees: CurrencyDataPoint? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """(average gross hourly earnings of male paid employees - average gross hourly earnings of female paid employees)/ average gross hourly earnings of male paid employees (in Percent). See Regulation (EU) 2022/1288, Annex I, top (23).""",
        example = """{
      "value" : 100.5, 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val unadjustedGenderPayGapInPercent: ExtendedDataPoint<BigDecimal?>? = null,
    @field:MinimumValue(minimumValue = 0)
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Number of females on the supervisory board, i.e. means the administrative, management or supervisory body of a company""",
        example = """{
      "value" : 100, 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val femaleBoardMembersSupervisoryBoard: ExtendedDataPoint<BigInteger?>? = null,
    @field:MinimumValue(minimumValue = 0)
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Number of females on the board of directors of the company""",
        example = """{
      "value" : 100, 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val femaleBoardMembersBoardOfDirectors: ExtendedDataPoint<BigInteger?>? = null,
    @field:MinimumValue(minimumValue = 0)
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Number of males on the supervisory board, i.e. means the administrative, management or supervisory body of a company""",
        example = """{
      "value" : 100, 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val maleBoardMembersSupervisoryBoard: ExtendedDataPoint<BigInteger?>? = null,
    @field:MinimumValue(minimumValue = 0)
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Number of males on the board of directors of the company""",
        example = """{
      "value" : 100, 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val maleBoardMembersBoardOfDirectors: ExtendedDataPoint<BigInteger?>? = null,
    @field:MinimumValue(minimumValue = 0)
    @field:MaximumValue(maximumValue = 100)
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Percentage of female board members among all supervisory board members. See Regulation (EU) 2022/1288, Annex I, table 1, indicator nr. 13.""",
        example = """{
      "value" : 100.5, 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val boardGenderDiversitySupervisoryBoardInPercent: ExtendedDataPoint<BigDecimal?>? = null,
    @field:MinimumValue(minimumValue = 0)
    @field:MaximumValue(maximumValue = 100)
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Percentage of female board members among all board of directors members. See Regulation (EU) 2022/1288, Annex I, table 1, indicator nr. 13.""",
        example = """{
      "value" : 100.5, 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val boardGenderDiversityBoardOfDirectorsInPercent: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Is the company involved in the manufacture or selling of controversial weapons such as anti-personnel mines, cluster munitions, chemical weapons and biological weapons? See Regulation (EU) 2022/1288, Annex I, table 1, indicator nr. 14.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val controversialWeaponsExposure: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company have a workplace accident prevention policy? (See Regulation (EU) 2022/1288, Annex I, table 3, indicator nr. 1.) If yes, please share the policy with us.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val workplaceAccidentPreventionPolicy: ExtendedDataPoint<YesNo?>? = null,
    @field:MinimumValue(minimumValue = 0)
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Rate of recordable work-related injuries as defined in GRI, i.e. (Number of recordable work-related injuries) /  (number of hours worked ) x 200,000. Linked to Regulation (EU) 2022/1288, Annex I, table 3, indicator nr. 2.""",
        example = """{
      "value" : 100.5, 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val rateOfAccidents: ExtendedDataPoint<BigDecimal?>? = null,
    @field:MinimumValue(minimumValue = 0)
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Number of workdays lost to injuries, accidents, fatalities or illness in total""",
        example = """{
      "value" : 100.5, 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val workdaysLostInDays: ExtendedDataPoint<BigDecimal?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company have a supplier code of conduct addressing unsafe working conditions, precarious work, child labor, and forced labor? (See Regulation (EU) 2022/1288, Annex I, table 3, indicator nr. 4.) If yes, please share the document with us.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val supplierCodeOfConduct: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company have a grievance/complaints handling mechanism related to employee matters? See Regulation (EU) 2022/1288, Annex I, table 3, indicator nr. 5.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val grievanceHandlingMechanism: ExtendedDataPoint<YesNo?>? = null,
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Does the company have a policy on the protection of whistleblowers? (See Regulation (EU) 2022/1288, Annex I, table 3, indicator nr. 6.) If yes, please share the policy with us.""",
        example = """{
      "value" :  "Yes" , 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val whistleblowerProtectionPolicy: ExtendedDataPoint<YesNo?>? = null,
    @field:MinimumValue(minimumValue = 0)
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Number of reported discrimination-related incidents. See Regulation (EU) 2022/1288, Annex I, table 3, indicator nr. 7.1 .""",
        example = """{
      "value" : 100, 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val reportedIncidentsOfDiscrimination: ExtendedDataPoint<BigInteger?>? = null,
    @field:MinimumValue(minimumValue = 0)
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Number of discrimination related incidents reported that lead to any kind of penalty and/or fine. See Regulation (EU) 2022/1288, Annex I, table 3, indicator nr. 7.2 .""",
        example = """{
      "value" : 100, 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val sanctionedIncidentsOfDiscrimination: ExtendedDataPoint<BigInteger?>? = null,
    @field:MinimumValue(minimumValue = 0)
    @Suppress("ktlint:standard:max-line-length")
    @field:Schema(
        description = """Annual total compensation for the highest compensated individual divided by the median annual total compensation for all employees (excluding the highest-compensated individual). See Regulation (EU) 2022/1288, Annex I, table 3, indicator nr. 8.""",
        example = """{
      "value" : 100.5, 
      "quality" : "Reported",
      "comment" : "The value is reported by the company."
      "dataSource" : {
        "page" : "5-7",
        "tagName" : "monetaryAmount",
        "fileName" : "AnnualReport2020.pdf",
        "fileReference" : "207c80dd75e923a88ff283d8bf97e346c735d2859e27bd702cf033feaef6de47"
      }
    } """,
    )
    @field:Valid()
    val excessiveCeoPayRatio: ExtendedDataPoint<BigDecimal?>? = null,
)
