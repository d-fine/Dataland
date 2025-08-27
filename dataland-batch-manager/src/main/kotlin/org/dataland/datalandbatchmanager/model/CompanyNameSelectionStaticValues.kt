package org.dataland.datalandbatchmanager.model

object CompanyNameSelectionStaticValues {
    const val ENGLISH_LANGUAGE_STRING_GLEIF = "en"
    val noCompanyNameReplacementLanguageWhiteList = listOf(ENGLISH_LANGUAGE_STRING_GLEIF, "de", "fr", "it", "es")

    enum class AltenativeNameType {
        PREFERRED_ASCII_TRANSLITERATED_LEGAL_NAME,
        TRADING_OR_OPERATING_NAME,
        ALTERNATIVE_LANGUAGE_LEGAL_NAME,
        AUTO_ASCII_TRANSLITERATED_LEGAL_NAME,
        PREVIOUS_LEGAL_NAME,
    }
}
