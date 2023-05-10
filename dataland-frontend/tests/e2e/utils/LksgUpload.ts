import {
  CompanyInformation,
  Configuration,
  DataMetaInformation,
  LksgData,
  LksgDataControllerApi,
} from "@clients/backend";
import { UploadIds } from "./GeneralApiUtils";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "./CompanyUpload";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { lksgDataModel } from "@/components/resources/frameworkDataSearch/lksg/LksgDataModel";

/**
 * Uploads a single LKSG data entry for a company
 * @param token The API bearer token to use
 * @param companyId The Id of the company to upload the dataset for
 * @param reportingPeriod The reporting period to use for the upload
 * @param data The Dataset to upload
 * @returns a promise on the created data meta information
 */
export async function uploadOneLksgDatasetViaApi(
  token: string,
  companyId: string,
  reportingPeriod: string,
  data: LksgData
): Promise<DataMetaInformation> {
  const response = await new LksgDataControllerApi(
    new Configuration({ accessToken: token })
  ).postCompanyAssociatedLksgData({
    companyId,
    reportingPeriod,
    data,
  });
  return response.data;
}

/**
 * Uploads a company and single LkSG data entry for a company
 * @param token The API bearer token to use
 * @param companyInformation The company information to use for the company upload
 * @param testData The Dataset to upload
 * @param reportingPeriod The reporting period to use for the upload
 * @returns an object which contains the companyId from the company upload and the dataId from the data upload
 */
export function uploadCompanyAndLksgDataViaApi(
  token: string,
  companyInformation: CompanyInformation,
  testData: LksgData,
  reportingPeriod: string
): Promise<UploadIds> {
  return uploadCompanyViaApi(token, generateDummyCompanyInformation(companyInformation.companyName)).then(
    (storedCompany) => {
      return uploadOneLksgDatasetViaApi(token, storedCompany.companyId, reportingPeriod, testData).then(
        (dataMetaInformation) => {
          return { companyId: storedCompany.companyId, dataId: dataMetaInformation.dataId };
        }
      );
    }
  );
}

/**
 * Fills in dummy data for a single production site. Use this in a cy.within context of a production site container div
 */
function fillSingleProductionSite(): void {
  cy.get('input[name="nameOfProductionSite"]').type("CCddEE");
  cy.get('input[name="streetAndHouseNumber"]').type("Live-street 28");
  cy.get('select[name="country"]').select("Belgium");
  cy.get('input[name="city"]').type("Capitol City");
  cy.get('input[name="postalCode"]').type("WE-3133");
  cy.get('input[data-test="listOfGoodsOrServices"]').type("1,2,3");
  cy.get('button[aria-label="Add"]').click();
  cy.get("span.form-list-item").its("length").should("eq", 3);
}

/**
 * Clicking "Yes" Everywhere using cypress commands results in an out-of-memory error in electron
 * Therefore, we use native browser commands to consolidate all actions into a few cypress action
 * This action needs to be recursive as clicking yes on some fields will result in other fields getting visible
 * @param maxCounter the maximum recursion depth before an error is thrown
 */
function recursivelySelectYesOnAllFields(maxCounter: number): void {
  if (maxCounter <= 0) throw new Error("Recursion depth exceeded selecting yes on all input fields");

  cy.window().then((win) => {
    if (selectYesOnAllFieldsBrowser(win)) {
      cy.wait(250).then(() => recursivelySelectYesOnAllFields(maxCounter - 1));
    }
  });
}

/**
 * Uses the native browser window to sleect yes on all checkbox fields
 * @param win the native browser window to use
 * @returns whether a new checkbox has been checked
 */
function selectYesOnAllFieldsBrowser(win: Window): boolean {
  let changedAnything = false;
  win.document.querySelectorAll<HTMLInputElement>('input[type="radio"][value="Yes"]').forEach((element) => {
    if (!element.checked) {
      element.click();
      changedAnything = true;
    }
  });
  return changedAnything;
}

/**
 * Uses the native browser context to check if CheckBoxes from the Lksg data model have yes selected.
 */
function validateAllFieldsHaveYesSelected(): void {
  const yesNoInputs = lksgDataModel.flatMap((category) =>
    category.subcategories.flatMap((subcategory) =>
      subcategory.fields
        .filter((field) => field.component === "YesNoFormField" || field.component === "YesNoNaFormField")
        .map((field) => field.name)
    )
  );
  cy.window().then((win) => {
    yesNoInputs.forEach((name) => {
      const inputElement = win.document.querySelector<HTMLInputElement>(`input[name="${name}"][value="Yes"]`)!;

      if (!inputElement.checked) {
        throw new Error(`Checkbox ${name} should be selected, but is not`);
      }
    });
  });
}

/**
 * Given the data-test selector for a NACE form field,
 * this function will select the "A" code in that field
 * @param fieldName the identifier of the form field
 */
function selectANaceCode(fieldName: string): void {
  cy.get(`div[data-test='${fieldName}'] input`).click();

  cy.get(".p-treenode-label")
    .contains("A - Agriculture, hunting and forestry")
    .parents(".p-treenode-label")
    .last()
    .find("div.p-checkbox-box")
    .click();

  cy.get(`div[data-test='${fieldName}']`).click();
}

/**
 * Given the data-test selector for a Multi-Select Country-Code form field,
 * this function will select a single country
 * @param fieldName the identifier of the form field
 */
function selectACountryInMultiselect(fieldName: string): void {
  cy.get(`div[data-test='${fieldName}'] div.p-multiselect`).click();

  cy.get(".p-multiselect-item").contains("Afghanistan (AF)").siblings(".p-checkbox").last().click();
  cy.get(`div[data-test='${fieldName}']`).click();
}

/**
 * Selects a dummy date in the LKsG upload form date picker.
 */
function selectDummyDateInDataPicker(): void {
  cy.get('[data-test="dataDate"]').find("button.p-datepicker-trigger").click();
  cy.get("div.p-datepicker").find('button[aria-label="Previous Month"]').click();
  cy.get("div.p-datepicker").find('span:contains("13")').click();
  cy.get('input[name="dataDate"]').should(($input) => {
    const val = $input.val();
    expect(val).to.include("-13");
  });
}

/**
 * Tests if it is possible to add and remove production sites and fills out the details for one production sites
 */
function testProductionSiteAdditionAndRemovalAndFillOutOneProductionSite(): void {
  cy.get('div[data-test="productionSiteSection"]').should("be.visible");
  cy.get('button[data-test="ADD-NEW-Production-Site-button"]').should("be.visible").click();
  cy.get('div[data-test="productionSiteSection"]').should("have.length", 2);
  cy.get('[data-test="removeItemFromListOfProductionSites"]').eq(1).click();
  cy.get('div[data-test="productionSiteSection"]').should("have.length", 1);

  cy.get('div[data-test="productionSiteSection"]').within(() => fillSingleProductionSite());
}

/**
 * Fills out all required LKsG fields that are NOT Yes/No/(Na) fields
 */
function fillRequiredLksgFieldsWithDummyData(): void {
  selectACountryInMultiselect("subcontractingCompaniesCountries");
  selectACountryInMultiselect("highRiskCountries");
  selectACountryInMultiselect("highRiskCountriesRawMaterialsLocation");

  selectANaceCode("industry");
  selectANaceCode("subcontractingCompaniesIndustries");

  cy.get('div[data-test="shareOfTemporaryWorkers"]').find('input[value="Smaller10"]').click().should("be.checked");
  cy.get('div[data-test="market"]').find('input[value="International"]').click().should("be.checked");

  cy.get("input[name=humanRightsViolationActionMeasures]").type("Dummy answer");
  cy.get("input[name=humanRightsViolations]").type("Dummy answer");
  cy.get("input[name=numberOfEmployees]").type("7999");
  cy.get("input[name=totalRevenue]").type("10043000");
  cy.get("input[name=groupOfCompaniesName]").type("TestCompanyGroup");
  cy.get("input[name=capacity]").type("123");

  cy.get("select[name=totalRevenueCurrency]").select("EUR");
}

/**
 * Uploads a single LKSG data entry for a company via form
 */
export function uploadLksgDataViaForm(): void {
  Cypress.Keyboard.defaults({
    keystrokeDelay: 0,
  });

  submitButton.buttonIsAddDataButton();
  submitButton.buttonAppearsDisabled();
  selectDummyDateInDataPicker();

  recursivelySelectYesOnAllFields(15);
  validateAllFieldsHaveYesSelected();

  fillRequiredLksgFieldsWithDummyData();
  testProductionSiteAdditionAndRemovalAndFillOutOneProductionSite();

  submitButton.buttonAppearsEnabled();
  submitButton.clickButton();
  cy.get("div.p-message-success").should("be.visible");
}

/**
 * Scrolls to the top and bottom of the LKSG input form and checks if the sidebar correctly switches from sticky to non-sticky while doing so.
 */
export function checkStickynessOfSubmitSideBar(): void {
  cy.scrollTo("bottom");
  cy.get("[data-test='submitSideBar']").should("have.css", "position", "fixed").and("have.css", "top", "60px");
  cy.scrollTo("top");
  cy.get("[data-test='submitSideBar']").should("have.css", "position", "relative").and("have.css", "top", "0px");
}
