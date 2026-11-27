import { type PcafData, type SfdrData } from '@clients/backend';

/**
 * PCAF datasets cannot be uploaded and there are no PCAF fake fixtures. As SFDR and PCAF share their data points,
 * PCAF data is obtained from SFDR data in tests instead. This function returns the PCAF view of an SFDR dataset,
 * i.e. the subset of SFDR data points that PCAF consists of, arranged in the PCAF data structure.
 * @param sfdrData the SFDR dataset
 * @returns the PCAF dataset consisting of the data points shared with SFDR
 */
export function convertSfdrDataToPcafData(sfdrData: SfdrData): PcafData {
  return {
    general: {
      general: {
        fiscalYearEnd: sfdrData.general?.company?.fiscalYearEnd,
      },
      company: {
        mainPcafSector: sfdrData.general?.company?.mainPcafSector,
        companyExchangeStatus: sfdrData.general?.company?.companyExchangeStatus,
      },
    },
    financial: {
      financial: {
        pppAdjustedGrossDomesticProductGdp: sfdrData.financial?.financial?.pppAdjustedGrossDomesticProductGdp,
        bookValueOfDebtInEUR: sfdrData.financial?.financial?.bookValueOfDebtInEUR,
      },
      listedCompany: {
        marketCapitalizationInEUR: sfdrData.financial?.listedCompany?.marketCapitalizationInEUR,
        minoritiesInterestInEUR: sfdrData.financial?.listedCompany?.minoritiesInterestInEUR,
        enterpriseValueInEUR: sfdrData.financial?.listedCompany?.enterpriseValueInEUR,
      },
      unlistedCompany: {
        totalEquityInEUR: sfdrData.financial?.unlistedCompany?.totalEquityInEUR,
        totalEquityAndDebtInEUR: sfdrData.financial?.unlistedCompany?.totalEquityAndDebtInEUR,
      },
    },
    environmental: {
      greenhouseGasEmissions: {
        scope1GhgEmissionsInTonnes: sfdrData.environmental?.greenhouseGasEmissions?.scope1GhgEmissionsInTonnes,
        scope2GhgEmissionsInTonnes: sfdrData.environmental?.greenhouseGasEmissions?.scope2GhgEmissionsInTonnes,
        scope2GhgEmissionsLocationBasedInTonnes:
          sfdrData.environmental?.greenhouseGasEmissions?.scope2GhgEmissionsLocationBasedInTonnes,
        scope2GhgEmissionsMarketBasedInTonnes:
          sfdrData.environmental?.greenhouseGasEmissions?.scope2GhgEmissionsMarketBasedInTonnes,
        scope3GhgEmissionsInTonnes: sfdrData.environmental?.greenhouseGasEmissions?.scope3GhgEmissionsInTonnes,
      },
    },
  };
}
