import { admin_name, admin_pw, premium_user_name, premium_user_pw, reader_name, reader_pw } from "@e2e/utils/Cypress";
import { type Interception } from "cypress/types/net-stubbing";
import { describeIf } from "@e2e/support/TestUtility";
import { DataTypeEnum, type LksgData, type StoredCompany } from "@clients/backend";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { uploadFrameworkData } from "@e2e/utils/FrameworkUpload";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";
import { humanizeStringOrNumber } from "@/utils/StringFormatter";
import { singleDataRequestPage } from "@sharedUtils/components/SingleDataRequest";
import { type SingleDataRequest } from "@clients/communitymanager";

describeIf(
  "As a premium user, I want to be able to navigate to the single data request page and submit a request",
  {
    executionEnvironments: ["developmentLocal", "ci", "developmentCd"],
  },
  () => {
    const uniqueCompanyMarker = Date.now().toString();
    const testCompanyName = "Company-for-single-data-request" + uniqueCompanyMarker;
    let testStoredCompany: StoredCompany;
    let lksgPreparedFixtures: Array<FixtureData<LksgData>>;

    /**
     * Uploads a company with lksg data
     */
    function uploadCompanyWithData(): void {
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then(
          async (storedCompany) => {
            testStoredCompany = storedCompany;
            return uploadFrameworkData(
              DataTypeEnum.Lksg,
              token,
              storedCompany.companyId,
              "2015",
              getPreparedFixture("LkSG-date-2022-07-30", lksgPreparedFixtures).t,
            );
          },
        );
      });
    }
    before(() => {
      cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
        lksgPreparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
        uploadCompanyWithData();
      });
    });
    beforeEach(() => {
      cy.ensureLoggedIn(premium_user_name, premium_user_pw);
    });

    it("Navigate to the single request page via the company cockpit", () => {
      cy.visitAndCheckAppMount(`/companies/${testStoredCompany.companyId}`);
      cy.get('[data-test="singleDataRequestButton"]').should("exist").click();
      cy.url().should("contain", `/singledatarequest/${testStoredCompany.companyId}`);
    });

    it("Navigate to the single request page via the view page and verify that the viewed framework is preselected.", () => {
      cy.visitAndCheckAppMount(`/companies/${testStoredCompany.companyId}/frameworks/${DataTypeEnum.Lksg}`);
      cy.get('[data-test="singleDataRequestButton"]').should("exist").click();
      cy.url().should("contain", `/singledatarequest/${testStoredCompany.companyId}`);
      cy.get('[data-test="datapoint-framework"]').should("have.value", "lksg");
    });

    it("Fill out the request page and check correct validation, request and success message", () => {
      cy.intercept("POST", "**/community/requests/single").as("postRequestData");
      cy.visitAndCheckAppMount(`/singleDataRequest/${testStoredCompany.companyId}`);
      checkCompanyInfoSheet();
      checkValidation();
      singleDataRequestPage.chooseReportingPeriod2023();
      checkDropdownLabels();
      singleDataRequestPage.chooseFrameworkLksg();

      cy.get('[data-test="contactEmail"]').type("example@Email.com");
      cy.get('[data-test="dataRequesterMessage"]').type("Frontend test message");
      submit();
      cy.wait("@postRequestData", { timeout: Cypress.env("short_timeout_in_ms") as number }).then((interception) => {
        checkIfRequestBodyIsValid(interception);
      });
      checkCompanyInfoSheet();
      cy.get("[data-test=submittedDiv]").should("exist");
      cy.get("[data-test=requestStatusText]").should("contain.text", "Submitting your data request was successful.");
      cy.get('[data-test="backToCompanyPageButton"]').click();
      cy.url().should("contain", "/companies/");
    });

    it("As a data_reader trying to submit a request should lead to an appropriate error message", () => {
      cy.ensureLoggedIn(reader_name, reader_pw);
      cy.visitAndCheckAppMount(`/singleDataRequest/${testStoredCompany.companyId}`);
      singleDataRequestPage.chooseReportingPeriod2023();
      singleDataRequestPage.chooseFrameworkLksg();
      submit();
      cy.get("[data-test=submittedDiv]").should("exist");
      cy.get("[data-test=requestStatusText]").should(
        "contain.text",
        "The submission of your data request was unsuccessful.",
      );
    });

    /**
     * Checks if the request body that is sent to the backend is valid and matches the given information
     * @param interception the object of interception with the backend
     */
    function checkIfRequestBodyIsValid(interception: Interception): void {
      type SingleDataRequestTypeInInterception = Omit<SingleDataRequest, "reportingPeriods" | "contacts"> & {
        reportingPeriods: string[];
        contacts: string[];
      };
      if (interception.request !== undefined) {
        const requestBody = interception.request.body as SingleDataRequestTypeInInterception;
        const expectedRequest: SingleDataRequestTypeInInterception = {
          companyIdentifier: testStoredCompany.companyId,
          dataType: DataTypeEnum.Lksg,
          reportingPeriods: ["2023"],
          contacts: ["example@Email.com"],
          message: "Frontend test message",
        };
        expect(requestBody).to.deep.equal(expectedRequest);
      }
    }
    /**
     * Clicks submit button
     */
    function submit(): void {
      cy.get("button[type='submit']").should("exist").click();
    }

    /**
     * Checks if all expected human-readable labels are visible in the dropdown options
     */
    function checkDropdownLabels(): void {
      const dropdown = cy.get("[data-test='datapoint-framework']").should("exist");
      ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE.forEach((framework) => {
        dropdown.should("contain.text", humanizeStringOrNumber(framework));
      });
    }

    /**
     * Checks basic validation
     */
    function checkValidation(): void {
      submit();
      cy.get("div[data-test='reportingPeriods'] p[data-test='reportingPeriodErrorMessage'")
        .should("be.visible")
        .should("contain.text", "Select at least one reporting period to submit your request.");

      cy.get("div[data-test='selectFramework'] li[data-message-type='validation']")
        .should("be.visible")
        .should("contain.text", "Select a framework to submit your request");
    }

    /**
     * Checks if the information on the company banner is correct
     */
    function checkCompanyInfoSheet(): void {
      cy.get("[data-test='companyNameTitle']").should("contain.text", testCompanyName);
    }
  },
);
