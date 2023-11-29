import { type CompanyInformation, type DataTypeEnum } from "@clients/backend";
import { type CyHttpMessages, type Interception } from "cypress/types/net-stubbing";
import Chainable = Cypress.Chainable;
import { getKeycloakToken } from "./Auth";
import { admin_name, admin_pw, uploader_name, uploader_pw } from "./Cypress";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "./CompanyUpload";
import { submitFilledInEuTaxonomyForm } from "./EuTaxonomyFinancialsUpload";

/**
 * Visits the edit page for a framework via UI navigation.
 * @param companyId the id of the company for which to edit a dataset
 * @param dataType the framework
 * @returns a cypress chainable to the interception of the data request on the edit page
 */
export function goToEditFormOfMostRecentDatasetForCompanyAndFramework(
  companyId: string,
  dataType: DataTypeEnum,
): Chainable<Interception> {
  const getRequestAlias = "fetchDataForPrefill";
  cy.intercept({
    method: "GET",
    url: "**/api/data/**",
    times: 2,
  }).as(getRequestAlias);
  cy.visit(`/companies/${companyId}/frameworks/${dataType}`);
  cy.wait(`@${getRequestAlias}`, { timeout: Cypress.env("medium_timeout_in_ms") as number });
  cy.get('[data-test="editDatasetButton"]').click();
  return cy.wait(`@${getRequestAlias}`, { timeout: Cypress.env("medium_timeout_in_ms") as number });
}

/**
 * Uploads a company via POST-request, then an EU Taxonomy dataset for the uploaded company via the form in the
 * frontend, and then visits the view page where that dataset is displayed
 * @param frameworkDataType The EU Taxanomy framework being tested
 * @param companyInformation Company information to be used for the company upload
 * @param testData EU Taxonomy dataset to be uploaded
 * @param formFill Steps involved to fill data of the upload form
 * @param submissionDataIntercept performs checks on the request itself
 * @param afterDatasetSubmission is performed after the data has been submitted
 */
export function uploadCompanyViaApiAndEuTaxonomyDataViaForm<T>(
  frameworkDataType: DataTypeEnum,
  companyInformation: CompanyInformation,
  testData: T,
  formFill: (data: T) => void,
  submissionDataIntercept: (request: CyHttpMessages.IncomingHttpRequest) => void,
  afterDatasetSubmission: (companyId: string) => void,
): void {
  getKeycloakToken(uploader_name, uploader_pw).then((token: string) => {
    return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
      (storedCompany): void => {
        cy.ensureLoggedIn(admin_name, admin_pw);
        cy.visitAndCheckAppMount(`/companies/${storedCompany.companyId}/frameworks/${frameworkDataType}/upload`);
        formFill(testData);
        submitFilledInEuTaxonomyForm(submissionDataIntercept);
        afterDatasetSubmission(storedCompany.companyId);
      },
    );
  });
}

/**
 * This method can be used in tests to check if two objects are equal. Equal means in this case, that the field names
 * in both objects match and also the values match.
 * @param objA the first object of the comparison
 * @param objB the second object of the comparison
 * @param path is the path of the current key of object A that is being compared to object B
 */
export function compareObjectKeysAndValuesDeep(
  objA: Record<string, object>,
  objB: Record<string, object>,
  path = "",
): void {
  const keysA = Object.keys(objA);
  const keysB = Object.keys(objB);

  for (const key of keysA) {
    const newPath = path ? `${path}.${key}` : key;

    if (!keysB.includes(key)) {
      throw new Error(`A field with the key ${newPath} exists in A but not in B`);
    }

    const valueA = objA[key] as Record<string, object>;
    const valueB = objB[key] as Record<string, object>;
    checkIfContentIsIdentical(valueA, valueB, newPath);
  }

  for (const key of keysB) {
    if (!keysA.includes(key)) {
      throw new Error(`A field with the key ${path}.${key} exists in B but not in A`);
    }
  }
}
/**
 * This method compares if two values are the same, if not it will throw an error
 * @param valueA the first value of the comparison
 * @param valueB the second value of the comparison
 * @param newPath is the path of the current key of value A that is being compared to value B
 */
function checkIfContentIsIdentical(
  valueA: Record<string, object>,
  valueB: Record<string, object>,
  newPath: string,
): void {
  const throwErrorBecauseOfFieldValue = (fieldPath: string): void => {
    throw new Error(`Field ${fieldPath} is not equal.`);
  };
  if (typeof valueA === "object" && typeof valueB === "object") {
    if (valueA === null || valueB === null) {
      if (valueA !== valueB) {
        throwErrorBecauseOfFieldValue(newPath);
      }
    } else {
      compareObjectKeysAndValuesDeep(valueA, valueB, newPath);
    }
  } else if (valueA !== valueB) {
    throwErrorBecauseOfFieldValue(newPath);
  }
}

/**
 * This function opens a given dataset with its Multi Layer Data Table and checks wether a specific field can be hidden using the input switch.
 * @param datasetIdentifier Identifies a dataset on the "my dataset" Page. The function will open the corresponding dataset.
 * @param toggledFieldName Name of a field which is toggled by the input switch
 */
export function checkToggleEmptyFieldsSwitch(datasetIdentifier: string, toggledFieldName: string): void {
  cy.wait(300);
  cy.get("span").contains(toggledFieldName).should("not.exist");
  cy.get('span[data-test="hideEmptyDataToggle"]').should("exist");
  cy.get('div[data-test="dataPointToggleButton"]').should("have.class", "p-inputswitch-checked").click();
  cy.get('div[data-test="dataPointToggleButton"]').should("not.have.class", "p-inputswitch-checked");
  cy.get("span").contains(toggledFieldName).should("exist");
  cy.get('div[data-test="dataPointToggleButton"]').click();
  cy.get('div[data-test="dataPointToggleButton"]').should("have.class", "p-inputswitch-checked");
  cy.get("span").contains(toggledFieldName).should("not.exist");
}
