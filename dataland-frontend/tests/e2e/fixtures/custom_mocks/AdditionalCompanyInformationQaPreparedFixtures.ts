import { type AdditionalCompanyInformationData } from '@clients/backend';
import {
  type AdditionalCompanyInformationData as AdditionalCompanyInformationQaData,
  QaReportDataPointVerdict,
  type CurrencyDataPoint,
} from '@clients/qaservice';
import { AdditionalCompanyInformationGenerator } from '@e2e/fixtures/frameworks/additional-company-information/AdditionalCompanyInformationGenerator.ts';

/**
 * Generate a pair of qa report and data for the AdditionalCompanyInformation with various
 * verdicts and data.
 * @returns a pair of qa report and data for the AdditionalCompanyInformation
 */
export function generateAdditionalCompanyInformationLinkedQaReports(): {
  data: AdditionalCompanyInformationData;
  qaReport: AdditionalCompanyInformationQaData;
} {
  const dataGenerator = new AdditionalCompanyInformationGenerator(0);
  const data: AdditionalCompanyInformationData = {
    general: {
      general: {
        referencedReports: dataGenerator.reports,
      },
      financialInformation: {
        equity: dataGenerator.randomCurrencyDataPoint(),
        debt: dataGenerator.randomCurrencyDataPoint(),
        evic: dataGenerator.randomCurrencyDataPoint(),
      },
    },
  };

  const qaReport: AdditionalCompanyInformationQaData = {
    general: {
      financialInformation: {
        equity: {
          verdict: QaReportDataPointVerdict.QaAccepted,
          comment: 'Equity is fine',
          correctedData: {},
        },
        debt: {
          verdict: QaReportDataPointVerdict.QaNotAttempted,
          comment: 'Qa for debt was inconclusive',
          correctedData: {},
        },
        evic: {
          verdict: QaReportDataPointVerdict.QaRejected,
          comment: 'EVIC is not correct',
          correctedData: dataGenerator.randomCurrencyDataPoint() as CurrencyDataPoint,
        },
      },
    },
  };
  return { data, qaReport };
}
