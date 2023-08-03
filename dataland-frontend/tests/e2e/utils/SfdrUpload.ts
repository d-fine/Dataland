import {
  CompanyInformation,
  Configuration,
  DataMetaInformation,
  DataTypeEnum,
  SfdrData,
  SfdrDataControllerApi,
} from "@clients/backend";
import { UploadIds } from "./GeneralApiUtils";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "./CompanyUpload";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { uploadDocuments } from "@sharedUtils/components/UploadDocuments";
import { TEST_PDF_FILE_NAME } from "@e2e/utils/Constants";
import { recursivelySelectYesOnAllFields } from "@e2e/utils/LksgUpload";

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
 * Fills the Sfdr upload form with the given dataset
 * @param companyId comany id stored in database
 * @param valueFieldNotFilled optional parameter to define if value field is populated
 */
export function uploadSfdrDataViaForm(companyId: string): void {
  cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/${DataTypeEnum.Sfdr}/upload`);
  submitButton.buttonIsAddDataButton();
  submitButton.buttonAppearsDisabled();
  uploadDocuments.selectFile(TEST_PDF_FILE_NAME, "referencedReports");
  uploadDocuments.validateReportToUploadIsListed(TEST_PDF_FILE_NAME);
  uploadDocuments.fillAllReportsToUploadForms(1);

  fillAndValidateSfdrUploadForm();
  submitButton.clickButton();

  cy.get("div.p-message-success").should("be.visible");
}

/**
 * Fills all the fields of the Sfdr upload form for non-financial companies
 */
function fillAndValidateSfdrUploadForm(): void {
  Cypress.Keyboard.defaults({
    keystrokeDelay: 0,
  });

  submitButton.buttonIsAddDataButton();
  submitButton.buttonAppearsDisabled();
  selectDummyDates("dataDate");
  cy.get('input[name="fiscalYearDeviation"][value="Deviation"]').check();
  selectDummyDates("fiscalYearEnd");

  recursivelySelectYesOnAllFields(15);
  recursivelySelectReportedQualityFields();
}

/**
 * Selects a dummy year in the Sfdr upload form date picker.
 * @param fieldName name of the test element labeled with dataTest and the fieldName
 */
function selectDummyDates(fieldName = "dataDate"): void {
  cy.get(`[data-test="${fieldName}"]`).find("button.p-datepicker-trigger").click();
  cy.get("div.p-datepicker").find('button[aria-label="Previous Month"]').click();
  cy.get("div.p-datepicker").find('span:contains("13")').click();
  cy.get(`input[name="${fieldName}"]`).should(($input) => {
    const val = $input.val();
    expect(val).to.include("-13");
  });
}

/**
 * Opens the Reported Quality Fields and selects the first option
 */
function recursivelySelectReportedQualityFields(): void {
  cy.get(`select[name="quality"]`).each(($element) => {
    cy.wrap($element).select(3);
  });
}
