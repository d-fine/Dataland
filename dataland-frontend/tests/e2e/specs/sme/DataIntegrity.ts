import { describeIf } from "@e2e/support/TestUtility";
import { admin_name, admin_pw, admin_userId, getBaseUrl } from "@e2e/utils/Cypress";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { type DataMetaInformation, DataTypeEnum, type StoredCompany } from "@clients/backend";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import * as MLDT from "@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils";
import { UploadReports } from "@sharedUtils/components/UploadReports";
import { TEST_PDF_FILE_NAME, TEST_PRIVATE_PDF_FILE_PATH } from "@sharedUtils/ConstantsForPdfs";
import { postDataOwner } from "@e2e/utils/DataOwnerUtils";

let tokenForAdminUser: string;
let storedTestCompany: StoredCompany;
const uploadReports = new UploadReports("referencedReports");
/*TODO
- Mach beide Tests zu einem
- Der Test geht auf die upload page, trägt ein paar Daten (in die custom components v.a.) ein
- Danach selektiert er auch noch ein pdf file
- Anschließend klickt er auf submit
- Dann auf die view page des datasets und checken, ob der download funktioniert


- Deaktivier EDIT button für sme
- Eröffne backlog item um EDIT Funktionalität wieder einzuführen
 */
describeIf(
  "As a user, I expect to be able to edit and submit Sme data via the upload form",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
    onlyExecuteWhenEurodatIsLive: true,
  },
  function (): void {
    beforeEach(() => {
      const uniqueCompanyMarker = Date.now().toString();
      const testCompanyName = "Company-Created-In-Sme-Blanket-Test-" + uniqueCompanyMarker;

      getKeycloakToken(admin_name, admin_pw)
        .then((token: string) => {
          tokenForAdminUser = token;

          return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName));
        })
        .then((storedCompany) => {
          storedTestCompany = storedCompany;
          void postDataOwner(tokenForAdminUser, admin_userId, storedTestCompany.companyId);
        });
    });

    it("Create a company and a Sme dataset via api, then assure that the dataset equals the pre-uploaded one", () => {
      cy.ensureLoggedIn(admin_name, admin_pw);
      cy.intercept("**/api/companies/" + storedTestCompany.companyId + "/info").as("getCompanyInformation");
      cy.visitAndCheckAppMount(
        "/companies/" + storedTestCompany.companyId + "/frameworks/" + DataTypeEnum.Sme + "/upload",
      );
      //cy.wait("@getCompanyInformation", { timeout: Cypress.env("medium_timeout_in_ms") as number });
      cy.get("h1").should("contain", storedTestCompany.companyInformation.companyName);
      cy.intercept({
        url: `**/api/data/${DataTypeEnum.Sme}`,
        times: 1,
      }).as("postCompanyAssociatedData");
      //TODO Refactor to make it more readable
      cy.get('[data-test="reportingPeriod"]').click();
      cy.get("div.p-datepicker").get("div.p-yearpicker").click();
      cy.get('[data-test="addNewSubsidiaryButton"]').click();
      cy.get('[data-test="subsidiarySection"]').should("exist");
      cy.get('[data-test="subsidiarySection"]').get('[name="nameOfSubsidiary"]').type("Test-Subsidiary");
      cy.get('[data-test="subsidiarySection"]')
        .find('[data-test="AddressFormField"]')
        .find('[name="streetAndHouseNumber"]')
        .type("Test-Address");
      cy.get('[data-test="subsidiarySection"]')
        .find('[data-test="AddressFormField"]')
        .find('[data-test="country"]')
        .click();
      cy.get("ul.p-dropdown-items li").contains(`Afghanistan`).click();
      cy.get('[data-test="subsidiarySection"]')
        .find('[data-test="AddressFormField"]')
        .find('[name="city"]')
        .type("Test-City");
      cy.get('[data-test="subsidiarySection"]')
        .find('[data-test="AddressFormField"]')
        .find('[name="postalCode"]')
        .type("12345");
      cy.get('[data-test="PollutionEmissionSection"]').should("exist");
      cy.get('[data-test="PollutionEmissionSection"]').get('[name="pollutionType"]').type("Test-Waste-Type");
      cy.get('[data-test="PollutionEmissionSection"]').get('[name="emissionInKilograms"]').type("12345");
      cy.get('[data-test="PollutionEmissionSection"]').find('[name="releaseMedium"]').first().click();
      //TODO select of releaseMedium is strange, why first() necessary
      cy.get("ul.p-dropdown-items li").contains(`Air`).click();
      cy.get('[data-test="SiteAndAreaSection"]').should("exist");
      cy.get('[data-test="SiteAndAreaSection"]').get('[name="siteName"]').type("Test-Site-Name");
      cy.get('[data-test="SiteAndAreaSection"]')
        .find('[data-test="AddressFormFieldSite"]')
        .find('[name="streetAndHouseNumber"]')
        .type("Test-Address-Site");
      cy.get('[data-test="SiteAndAreaSection"]')
        .find('[data-test="AddressFormFieldSite"]')
        .find('[data-test="country"]')
        .click();
      cy.get("ul.p-dropdown-items li").contains(`Afghanistan`).click();
      cy.get('[data-test="SiteAndAreaSection"]')
        .find('[data-test="AddressFormFieldSite"]')
        .find('[name="city"]')
        .type("Test-City");
      cy.get('[data-test="SiteAndAreaSection"]')
        .find('[data-test="AddressFormFieldSite"]')
        .find('[name="postalCode"]')
        .type("12345");
      cy.get('[data-test="SiteAndAreaSection"]').find('[name="siteGeocoordinateLongitudeval"]').type("12345");
      cy.get('[data-test="SiteAndAreaSection"]').find('[name="siteGeocoordinateLatitude"]').type("12345");
      cy.get('[data-test="SiteAndAreaSection"]')
        .get('[name="biodiversitySensitiveArea"]')
        .type("Test-Site-Biodiversity-Area");
      cy.get('[data-test="SiteAndAreaSection"]')
        .find('[data-test="AddressFormFieldArea"]')
        .find('[name="streetAndHouseNumber"]')
        .type("Test-Address-Site");
      cy.get('[data-test="SiteAndAreaSection"]')
        .find('[data-test="AddressFormFieldArea"]')
        .find('[data-test="country"]')
        .click();
      cy.get("ul.p-dropdown-items li").contains(`Afghanistan`).click();
      cy.get('[data-test="SiteAndAreaSection"]')
        .find('[data-test="AddressFormFieldArea"]')
        .find('[name="city"]')
        .type("Test-City");
      cy.get('[data-test="SiteAndAreaSection"]')
        .find('[data-test="AddressFormFieldArea"]')
        .find('[name="postalCode"]')
        .type("12345");
      cy.get('[data-test="SiteAndAreaSection"]').find('[name="areaInHectare"]').type("12345");
      cy.get('[data-test="SiteAndAreaSection"]').find('[name="areaGeocoordinateLatitude"]').type("12345");
      cy.get('[data-test="SiteAndAreaSection"]').find('[name="areaGeocoordinateLongitude"]').type("12345");
      //TODO select of releaseMedium is strange, why first() necessary
      cy.get('[data-test="SiteAndAreaSection"]').find('[name="specificationOfAdjointness"]').first().click();
      cy.get("ul.p-dropdown-items li").contains(`In`).click();
      cy.get('[data-test="WasteClassificationSection"]').should("exist");
      cy.get('[data-test="WasteClassificationSection"]').find('[name="wasteClassification"]').first().click();
      cy.get("ul.p-dropdown-items li").contains(`Hazardous`).click();
      //TODO select of releaseMedium is strange, why first() necessary
      cy.get('[data-test="WasteClassificationSection"]').find('[name="totalAmountTons"]').type("12345");
      cy.get('[data-test="WasteClassificationSection"]').find('[name="wasteRecycleOrReuseTons"]').type("12345");
      cy.get('[data-test="WasteClassificationSection"]').find('[name="wasteDisposalTons"]').type("12345");
      cy.get('[data-test="WasteClassificationSection"]').find('[name="totalAmountCubicMeters"]').type("12345");
      cy.get('[data-test="WasteClassificationSection"]').find('[name="wasteRecycleOrReuseCubicMeters"]').type("12345");
      cy.get('[data-test="WasteClassificationSection"]').find('[name="wasteDisposalCubicMeters"]').type("12345");
      cy.get('[data-test="addNewEmployeesPerCountryButton"]').click();
      cy.get('[data-test="employeesPerCountrySection"]').should("exist");
      cy.get('[data-test="employeesPerCountrySection"]').find('[data-test="country"]').click();
      cy.get("ul.p-dropdown-items li").contains(`Afghanistan`).click();
      cy.get('[data-test="employeesPerCountrySection"]').find('[name="numberOfEmployeesInHeadCount"]').type("12345");
      cy.get('[data-test="employeesPerCountrySection"]')
        .find('[name="numberOfEmployeesInFullTimeEquivalent"]')
        .type("12345");
      uploadReports.selectFile(`${TEST_PDF_FILE_NAME}-private`);
      uploadReports.validateReportToUploadHasContainerInTheFileSelector(`${TEST_PDF_FILE_NAME}-private`);
      uploadReports.validateReportToUploadHasContainerWithInfoForm(`${TEST_PDF_FILE_NAME}-private`);

      cy.get('[data-test="electricityTotal"]')
        .find('div[data-test="toggleDataPointWrapper"]')
        .find('div[data-test="dataPointToggleButton"]')
        .click();
      cy.get('[data-test="electricityTotal"]').find('div[data-test="value"]').find('[name="value"]').type("12345");
      cy.get('[data-test="electricityTotal"]').find('div[name="quality"]').click();
      cy.get("ul.p-dropdown-items li").contains(`Audited`).click();
      cy.get('[data-test="electricityTotal"]').find('div[name="fileName"]').click();
      cy.get("ul.p-dropdown-items li").contains(`${TEST_PDF_FILE_NAME}-private`).click();
      cy.intercept({
        url: `**/api/data/${DataTypeEnum.Sme}`,
        times: 1,
      }).as("postCompanyAssociatedData");
      cy.intercept("**/api/users/**").as("waitOnMyDatasetPage");
      submitButton.clickButton();
      cy.wait("@waitOnMyDatasetPage", { timeout: Cypress.env("medium_timeout_in_ms") as number });
      cy.wait("@postCompanyAssociatedData", { timeout: Cypress.env("medium_timeout_in_ms") as number }).then(
        (postResponseInterception) => {
          cy.url().should("eq", getBaseUrl() + "/datasets");
          const dataMetaInformationOfReuploadedDataset = postResponseInterception.response?.body as DataMetaInformation;

          cy.visitAndCheckAppMount(
            "/companies/" +
              storedTestCompany.companyId +
              "/frameworks/" +
              DataTypeEnum.Sme +
              "/" +
              dataMetaInformationOfReuploadedDataset.dataId,
          );

          MLDT.getSectionHead("Energy and greenhous gas emissions").should(
            "have.attr",
            "data-section-expanded",
            "true",
          );
          MLDT.getCellValueContainer("Electricity total").find("a.link").should("include.text", "MWh").click();
          const expectedPathToDownloadedReport =
            Cypress.config("downloadsFolder") + `/${TEST_PDF_FILE_NAME}-private.pdf`;
          cy.readFile(expectedPathToDownloadedReport).should("not.exist");
          cy.intercept("**/api/data/sme/documents*").as("documentDownload");
          cy.get('[data-test="Report-Download-some-document-private"]').click();
          cy.wait(500);
          cy.wait("@documentDownload");
          cy.readFile(`../${TEST_PRIVATE_PDF_FILE_PATH}`, "binary", {
            timeout: Cypress.env("medium_timeout_in_ms") as number,
          }).then((expectedFileBinary) => {
            cy.task("calculateHash", expectedFileBinary).then((expectedFileHash) => {
              cy.readFile(expectedPathToDownloadedReport, "binary", {
                timeout: Cypress.env("medium_timeout_in_ms") as number,
              }).then((receivedFileHash) => {
                cy.task("calculateHash", receivedFileHash).should("eq", expectedFileHash);
              });
              cy.task("deleteFolder", Cypress.config("downloadsFolder"));
            });
          });
        },
      );
    });
  },
);
