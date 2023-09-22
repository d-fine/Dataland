import { assertDefined } from "@/utils/TypeScriptUtils";
import {
  type CompanyAssociatedDataEuTaxonomyDataForFinancials,
  type CompanyInformation,
  type DataPointOneValueBigDecimal,
  DataTypeEnum,
  type EligibilityKpis,
  type EuTaxonomyDataForFinancials,
} from "@clients/backend";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { TEST_PDF_FILE_NAME, TEST_PDF_FILE_PATH } from "@sharedUtils/ConstantsForPdfs";
import { admin_name, admin_pw } from "@e2e/utils/Cypress";
import { type FixtureData } from "@sharedUtils/Fixtures";
import { dateFormElement } from "@sharedUtils/components/DateFormElement";
import { type CyHttpMessages } from "cypress/types/net-stubbing";
import { goToEditFormOfMostRecentDatasetForCompanyAndFramework } from "./GeneralUtils";
import Chainable = Cypress.Chainable;

/**
 * Fills the eutaxonomy-financials upload form with the given dataset
 * @param data the data to fill the form with
 */
export function fillAndValidateEuTaxonomyForFinancialsUploadForm(data: EuTaxonomyDataForFinancials): void {
  dateFormElement.selectDayOfNextMonth("fiscalYearEnd", 12);
  dateFormElement.validateDay("fiscalYearEnd", 12);

  cy.get('[data-test="MultiSelectfinancialServicesTypes"]')
    .click()
    .get("div.p-multiselect-panel")
    .find("li.p-multiselect-item")
    .first()
    .click();
  cy.get('[data-test="addKpisButton"]').click({ force: true });
  cy.get('[data-test="removeSectionButton"]').click({ force: true });

  cy.get('[data-test="MultiSelectfinancialServicesTypes"]')
    .click()
    .get("div.p-multiselect-panel")
    .find("li.p-multiselect-item")
    .each(($el) => {
      cy.wrap($el).click({ force: true });
    });
  cy.get('[data-test="addKpisButton"]').click({ force: true });
  cy.get('[data-test="dataPointToggle"]')
    .eq(1)
    .should("exist")
    .should("contain.text", "Data point is available")
    .find('[data-test="dataPointToggleButton"]')
    .click();
  cy.get('[data-test="dataPointToggle"]')
    .eq(1)
    .find('[data-test="dataPointToggleTitle"]')
    .should("contain.text", "Data point is not available");
  cy.get('[data-test="dataPointToggle"]').eq(1).find('[data-test="dataPointToggleButton"]').click();
  cy.get('[data-test="dataPointToggle"]')
    .eq(1)
    .find('[data-test="dataPointToggleTitle"]')
    .should("contain.text", "Data point is available");
  if (data.nfrdMandatory !== undefined) {
    cy.get(`input[name="nfrdMandatory"][value=${data.nfrdMandatory.toString()}]`).check();
  }
  cy.get(
    `input[name="fiscalYearDeviation"][value=${
      data.fiscalYearDeviation ? data.fiscalYearDeviation.toString() : "Deviation"
    }]`,
  ).check();
  cy.get('input[name="scopeOfEntities"][value="No"]').check();
  cy.get('div[data-test="submitSideBar"] li:last a').click();
  cy.window().then((win) => {
    const scrollPosition = win.scrollY;
    expect(scrollPosition).to.be.greaterThan(0);
  });
  cy.get('button[data-test="submitButton"]').click();
  cy.window().then((win) => {
    const scrollPosition = win.scrollY;
    expect(scrollPosition).to.be.greaterThan(0);
  });
  cy.get(
    `input[name="euTaxonomyActivityLevelReporting"][value=${
      data.euTaxonomyActivityLevelReporting ? data.euTaxonomyActivityLevelReporting.toString() : "No"
    }]`,
  ).check();
  cy.get('input[name="numberOfEmployees"]').type("-13");
  cy.get('em[title="Number Of Employees"]').click();
  cy.get(`[data-message-type="validation"]`).should("exist").should("contain", "at least 0");
  cy.get('input[name="numberOfEmployees"]')
    .clear()
    .type(`${data.numberOfEmployees ? data.numberOfEmployees.toString() : "13"}`);
  cy.get('button[data-test="removeSectionButton"]').should("exist").should("have.class", "ml-auto");

  cy.get('[data-test="assuranceSection"] select[name="assurance"]').select(2);
  cy.get('[data-test="assuranceSection"] input[name="provider"]').type("Assurance Provider", { force: true });
  cy.get('[data-test="assuranceSection"] select[name="report"]').select(1);
  cy.get('[data-test="assuranceSection"] input[name="page"]').type("-13");
  cy.get('em[title="Assurance"]').click();
  cy.get(`[data-message-type="validation"]`).should("exist").should("contain", "at least 0");
  cy.get('[data-test="assuranceSection"] input[name="page"]').clear().type("1");

  fillEligibilityKpis("creditInstitutionKpis", data.eligibilityKpis?.CreditInstitution);
  fillEligibilityKpis("insuranceKpis", data.eligibilityKpis?.InsuranceOrReinsurance);
  fillEligibilityKpis("investmentFirmKpis", data.eligibilityKpis?.InvestmentFirm);
  fillEligibilityKpis("assetManagementKpis", data.eligibilityKpis?.AssetManagement);
  fillField(
    "insuranceKpis",
    "taxonomyEligibleNonLifeInsuranceActivitiesInPercent",
    data.insuranceKpis?.taxonomyEligibleNonLifeInsuranceActivitiesInPercent,
  );
  fillField("investmentFirmKpis", "greenAssetRatioInPercent", data.investmentFirmKpis?.greenAssetRatioInPercent);
  fillField(
    "creditInstitutionKpis",
    "tradingPortfolioAndInterbankLoansInPercent",
    data.creditInstitutionKpis?.tradingPortfolioAndInterbankLoansInPercent,
  );
  fillField(
    "creditInstitutionKpis",
    "tradingPortfolioInPercent",
    data.creditInstitutionKpis?.tradingPortfolioInPercent,
  );
  fillField("creditInstitutionKpis", "interbankLoansInPercent", data.creditInstitutionKpis?.interbankLoansInPercent);
  fillField("creditInstitutionKpis", "greenAssetRatioInPercent", data.creditInstitutionKpis?.greenAssetRatioInPercent);
}

/**
 * Fills a set with eligibility-kpis for different company types
 * @param divTag value of the parent div data-test attribute to fill in
 * @param data the kpi data to use to fill the form
 */
export function fillEligibilityKpis(divTag: string, data: EligibilityKpis | undefined): void {
  fillField(divTag, "taxonomyEligibleActivityInPercent", data?.taxonomyEligibleActivityInPercent);
  fillField(divTag, "taxonomyNonEligibleActivityInPercent", data?.taxonomyNonEligibleActivityInPercent);
  fillField(divTag, "derivativesInPercent", data?.derivativesInPercent);
  fillField(divTag, "banksAndIssuersInPercent", data?.banksAndIssuersInPercent);
  fillField(divTag, "investmentNonNfrdInPercent", data?.investmentNonNfrdInPercent);
}

/**
 * Enters a single decimal inputs field value in the upload eutaxonomy-financials form
 * @param divTag value of the parent div data-test attribute to fill in
 * @param inputsTag value of the parent div data-test attribute to fill in
 * @param value the value to fill in
 */
export function fillField(divTag: string, inputsTag: string, value?: DataPointOneValueBigDecimal): void {
  if (value?.value) {
    const valueAsString = value.value.toString();
    if (divTag === "") {
      cy.get(`[data-test="${inputsTag}"]`).find('input[name="value"]').type(valueAsString);
      cy.get(`[data-test="${inputsTag}"]`).find('input[name="page"]').type("13");
      cy.get(`[data-test="${inputsTag}"]`).find('select[name="report"]').select(1);
      cy.get(`[data-test="${inputsTag}"]`).find('select[name="quality"]').select(1);
      cy.get(`[data-test="${inputsTag}"]`)
        .find('textarea[name="comment"]')
        .type(`${value.comment ?? "comment"}`);
    } else {
      cy.get(`[data-test="${divTag}"]`)
        .find(`[data-test="${inputsTag}"]`)
        .find('input[name="value"]')
        .type(valueAsString);
      cy.get(`[data-test="${divTag}"]`)
        .find(`[data-test="${inputsTag}"]`)
        .find('input[name="page"]')
        .type(`${value.dataSource?.page ?? "13"}`);
      cy.get(`[data-test="${divTag}"]`).find(`[data-test="${inputsTag}"]`).find('select[name="report"]').select(1);
      cy.get(`[data-test="${divTag}"]`).find(`[data-test="${inputsTag}"]`).find('select[name="quality"]').select(1);
      cy.get(`[data-test="${divTag}"]`)
        .find(`[data-test="${inputsTag}"]`)
        .find('textarea[name="comment"]')
        .type(`${value.comment ?? "comment"}`);
    }
  }
}

/**
 * Extracts the first eutaxonomy-financials dataset from the fake fixtures
 * @returns the first eutaxonomy-financials dataset from the fake fixtures
 */
export function getFirstEuTaxonomyFinancialsFixtureDataFromFixtures(): Chainable<
  FixtureData<EuTaxonomyDataForFinancials>
> {
  return cy.fixture("CompanyInformationWithEuTaxonomyDataForFinancials").then(function (jsonContent) {
    const companiesWithEuTaxonomyDataForFinancials = jsonContent as Array<FixtureData<EuTaxonomyDataForFinancials>>;
    return companiesWithEuTaxonomyDataForFinancials[0];
  });
}

/**
 * Visits the edit page for the eu taxonomy dataset for financial companies via navigation.
 * @param companyId the id of the company for which to edit a dataset
 * @param expectIncludedFile specifies if the test file is expected to be in the server response
 */
export function gotoEditForm(companyId: string, expectIncludedFile: boolean): void {
  goToEditFormOfMostRecentDatasetForCompanyAndFramework(companyId, DataTypeEnum.EutaxonomyFinancials).then(
    (interception) => {
      const referencedReports = assertDefined(
        (interception?.response?.body as CompanyAssociatedDataEuTaxonomyDataForFinancials)?.data?.referencedReports,
      );
      expect(TEST_PDF_FILE_NAME in referencedReports).to.equal(expectIncludedFile);
      expect(`${TEST_PDF_FILE_NAME}2` in referencedReports).to.equal(true);
    },
  );
}

/**
 * Uploads a company via POST-request, then an EU Taxonomy dataset for financial companies for the uploaded company
 * via the form in the frontend, and then visits the view page where that dataset is displayed
 * @param companyInformation Company information to be used for the company upload
 * @param testData EU Taxonomy dataset for financial companies to be uploaded
 * @param beforeFormFill is performed before filling the fields of the upload form
 * @param formFillSteps Steps involved to fill data of the upload form
 * @param afterFormFill is performed after filling the fields of the upload form
 * @param submissionDataIntercept performs checks on the request itself
 * @param afterDatasetSubmission is performed after the data has been submitted
 */
export function uploadCompanyViaApiAndEuTaxonomyDataForFinancialsViaForm(
  companyInformation: CompanyInformation,
  testData: EuTaxonomyDataForFinancials,
  beforeFormFill: () => void,
  formFillSteps: (data: EuTaxonomyDataForFinancials) => void,
  afterFormFill: () => void,
  submissionDataIntercept: (request: CyHttpMessages.IncomingHttpRequest) => void,
  afterDatasetSubmission: (companyId: string) => void,
): void {
  getKeycloakToken(admin_name, admin_pw).then((token: string) => {
    return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
      (storedCompany): void => {
        cy.ensureLoggedIn(admin_name, admin_pw);
        cy.visitAndCheckAppMount(
          `/companies/${storedCompany.companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/upload`,
        );
        beforeFormFill();
        formFillSteps(testData);
        afterFormFill();
        submitFilledInEuTaxonomyForm(submissionDataIntercept);
        afterDatasetSubmission(storedCompany.companyId);
      },
    );
  });
}

/**
 * Fills the eutaxonomy-financials upload form with the given dataset
 * @param data the data to fill the form with
 */
export function fillAndValidateEuTaxonomyCreditInstitutionForm(data: EuTaxonomyDataForFinancials): void {
  dateFormElement.selectDayOfNextMonth("fiscalYearEnd", 12);
  dateFormElement.validateDay("fiscalYearEnd", 12);

  if (data.nfrdMandatory !== undefined) {
    cy.get(`input[name="nfrdMandatory"][value=${data.nfrdMandatory.toString()}]`).check();
  }

  cy.get(
    `input[name="fiscalYearDeviation"][value=${
      data.fiscalYearDeviation ? data.fiscalYearDeviation.toString() : "Deviation"
    }]`,
  ).check();

  cy.get('input[name="numberOfEmployees"]').type(
    `${data.numberOfEmployees ? data.numberOfEmployees.toString() : "13"}`,
  );

  cy.get('[data-test="assuranceSection"] select[name="assurance"]').select(2);
  cy.get('[data-test="assuranceSection"] input[name="provider"]').type("Assurance Provider", { force: true });
  cy.get('[data-test="assuranceSection"] select[name="report"]').select(1);

  cy.get('[data-test="MultiSelectfinancialServicesTypes"]')
    .click()
    .get("div.p-multiselect-panel")
    .find("li.p-multiselect-item")
    .each(($el) => {
      cy.wrap($el).click({ force: true });
    });

  cy.get('[data-test="addKpisButton"]').click({ force: true });

  cy.get('[data-test="removeSectionButton"]').each(($el, index) => {
    if (index > 0) {
      cy.wrap($el).click({ force: true });
    }
  });

  cy.get('button[data-test="removeSectionButton"]').should("exist").should("have.class", "ml-auto");

  fillEligibilityKpis("creditInstitutionKpis", data.eligibilityKpis?.CreditInstitution);
  fillField(
    "creditInstitutionKpis",
    "tradingPortfolioAndInterbankLoansInPercent",
    data.creditInstitutionKpis?.tradingPortfolioAndInterbankLoansInPercent,
  );
  fillField("creditInstitutionKpis", "tradingPortfolioInPercent", data.creditInstitutionKpis?.tradingPortfolioInPercent);
  fillField("creditInstitutionKpis", "interbankLoansInPercent", data.creditInstitutionKpis?.interbankLoansInPercent);
  fillField("creditInstitutionKpis", "greenAssetRatioInPercent", data.creditInstitutionKpis?.greenAssetRatioInPercent);
}

/**
 * This method verifies that uploaded reports are downloadable
 * @param companyId the ID of the company whose data to view
 */
export function checkIfLinkedReportsAreDownloadable(companyId: string): void {
  cy.visitAndCheckAppMount(`/companies/${companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}`);
  const expectedPathToDownloadedReport = Cypress.config("downloadsFolder") + `/${TEST_PDF_FILE_NAME}.pdf`;
  const downloadLinkSelector = `span[data-test="Report-Download-${TEST_PDF_FILE_NAME}"]`;
  cy.readFile(expectedPathToDownloadedReport).should("not.exist");
  cy.intercept("**/documents/*").as("documentDownload");
  cy.get(downloadLinkSelector).click();
  cy.wait("@documentDownload");
  cy.readFile(`../${TEST_PDF_FILE_PATH}`, "binary", {
    timeout: Cypress.env("medium_timeout_in_ms") as number,
  }).then((expectedPdfBinary) => {
    cy.task("calculateHash", expectedPdfBinary).then((expectedPdfHash) => {
      cy.readFile(expectedPathToDownloadedReport, "binary", {
        timeout: Cypress.env("medium_timeout_in_ms") as number,
      }).then((receivedPdfHash) => {
        cy.task("calculateHash", receivedPdfHash).should("eq", expectedPdfHash);
      });
      cy.task("deleteFolder", Cypress.config("downloadsFolder"));
    });
  });
}

/**
 * After a Eu Taxonomy financial or non financial form has been filled in this function submits the form and checks
 * if a 200 response is returned by the backend
 * @param submissionDataIntercept function that asserts content of an intercepted request
 */
export function submitFilledInEuTaxonomyForm(
  submissionDataIntercept: (request: CyHttpMessages.IncomingHttpRequest) => void,
): void {
  const postRequestAlias = "postDataAlias";
  cy.intercept(
    {
      method: "POST",
      url: `**/api/data/**`,
      times: 1,
    },
    submissionDataIntercept,
  ).as(postRequestAlias);
  cy.get('button[data-test="submitButton"]').click();
  cy.wait(`@${postRequestAlias}`, { timeout: Cypress.env("long_timeout_in_ms") as number }).then((interception) => {
    expect(interception.response?.statusCode).to.eq(200);
  });
  cy.contains("td", "EU Taxonomy");
}
