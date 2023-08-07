import {
  CompanyInformation,
  Configuration,
  DataMetaInformation,
  SfdrData,
  SfdrDataControllerApi,
} from "@clients/backend";
import { UploadIds } from "./GeneralApiUtils";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "./CompanyUpload";
import { uploadDocuments } from "@sharedUtils/components/UploadDocuments";
import { TEST_PDF_FILE_NAME } from "@e2e/utils/Constants";

/**
 * Uploads a single SFDR data entry for a company
 * @param token The API bearer token to use
 * @param companyId The Id of the company to upload the dataset for
 * @param reportingPeriod The reporting period to use for the upload
 * @param data The Dataset to upload
 * @returns a promise on the created data meta information
 */
export async function uploadOneSfdrDataset(
  token: string,
  companyId: string,
  reportingPeriod: string,
  data: SfdrData,
): Promise<DataMetaInformation> {
  const response = await new SfdrDataControllerApi(
    new Configuration({ accessToken: token }),
  ).postCompanyAssociatedSfdrData(
    {
      companyId,
      reportingPeriod,
      data,
    },
    true,
  );
  return response.data;
}

/**
 * Uploads a company and single SFDR data entry for a company
 * @param token The API bearer token to use
 * @param companyInformation The company information to use for the company upload
 * @param testData The Dataset to upload
 * @param reportingPeriod The reporting period to use for the upload
 * @returns an object which contains the companyId from the company upload and the dataId from the data upload
 */
export function uploadCompanyAndSfdrDataViaApi(
  token: string,
  companyInformation: CompanyInformation,
  testData: SfdrData,
  reportingPeriod: string,
): Promise<UploadIds> {
  return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
    (storedCompany) => {
      return uploadOneSfdrDataset(token, storedCompany.companyId, reportingPeriod, testData).then(
        (dataMetaInformation) => {
          return { companyId: storedCompany.companyId, dataId: dataMetaInformation.dataId };
        },
      );
    },
  );
}
/**
 * Adds reports to the dataset via the Sfdr upload form for the given dataset
 */
export function selectsReportsForUploadInSfdrForm(): void {
  uploadDocuments.selectFile(TEST_PDF_FILE_NAME, "referencedReports");
  uploadDocuments.validateReportToUploadIsListed(TEST_PDF_FILE_NAME);
  uploadDocuments.fillAllReportsToUploadForms(1);
}
