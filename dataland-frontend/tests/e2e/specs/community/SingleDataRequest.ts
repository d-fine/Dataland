import { admin_name, admin_pw, premium_user_name, premium_user_pw, reader_name, reader_pw } from "@e2e/utils/Cypress";
import { type Interception } from "cypress/types/net-stubbing";
import {
  RequestControllerApi,
  RequestStatus,
  type SingleDataRequest,
  type StoredDataRequest,
} from "@clients/communitymanager";
import { describeIf } from "@e2e/support/TestUtility";
import { Configuration, DataTypeEnum, type LksgData, type StoredCompany } from "@clients/backend";
import { getKeycloakToken } from "@e2e/utils/Auth";
import { generateDummyCompanyInformation, uploadCompanyViaApi } from "@e2e/utils/CompanyUpload";
import { uploadFrameworkData } from "@e2e/utils/FrameworkUpload";
import { type FixtureData, getPreparedFixture } from "@sharedUtils/Fixtures";
import { ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE } from "@/utils/Constants";
import { humanizeStringOrNumber } from "@/utils/StringFormatter";
import { singleDataRequestPage } from "@sharedUtils/components/SingleDataRequest";

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
     * @param reportingPeriod the year for which the data is uploaded
     */
    function uploadCompanyWithData(reportingPeriod: string): void {
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadCompanyViaApi(token, generateDummyCompanyInformation(testCompanyName)).then((storedCompany) => {
          testStoredCompany = storedCompany;
          return uploadFrameworkDataForCompany(storedCompany.companyId, reportingPeriod);
        });
      });
    }
    /**
     * Sets the status of a single data request from open to answered
     * @param companyId id of the company
     * @param reportingPeriod the year for which the framework is uploaded
     */
    function uploadFrameworkDataForCompany(companyId: string, reportingPeriod: string): void {
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        return uploadFrameworkData(
          DataTypeEnum.Lksg,
          token,
          companyId,
          reportingPeriod,
          getPreparedFixture("LkSG-date-2022-07-30", lksgPreparedFixtures).t,
        );
      });
    }
    before(() => {
      cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
        lksgPreparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
        uploadCompanyWithData("2020");
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
      singleDataRequestPage.chooseReportingPeriod("2023");
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
      singleDataRequestPage.chooseReportingPeriod("2023");
      singleDataRequestPage.chooseFrameworkLksg();
      submit();
      cy.get("[data-test=submittedDiv]").should("exist");
      cy.get("[data-test=requestStatusText]").should(
        "contain.text",
        "The submission of your data request was unsuccessful.",
      );
    });

    it("Create a single data request, set the status to answered, then close the request on the view page", () => {
      cy.ensureLoggedIn(premium_user_name, premium_user_pw);
      cy.visitAndCheckAppMount(`/companies/${testStoredCompany.companyId}/frameworks/${DataTypeEnum.Lksg}`);
      cy.get('[data-test="reOpenRequestButton"]').should("not.exist");
      cy.get('[data-test="closeRequestButton"]').should("not.exist");
      cy.get('[data-test="singleDataRequestButton"]').should("exist").click();
      singleDataRequestPage.chooseReportingPeriod("2020");
      cy.intercept("POST", "**/community/requests/single").as("postRequestData");
      submit();
      let dataRequestId: string;
      cy.wait("@postRequestData", { timeout: Cypress.env("short_timeout_in_ms") as number }).then((interception) => {
        if (interception.response !== undefined) {
          const responseBody = interception.response.body as StoredDataRequest[];
          dataRequestId = responseBody[0].dataRequestId;
        }
      });
      setRequestStatusToAnswered(dataRequestId);
      cy.visitAndCheckAppMount(`/companies/${testStoredCompany.companyId}/frameworks/${DataTypeEnum.Lksg}`);
      checkForReviewButtonsAndClick("closeRequestButton", "reOpenRequestButton");
      cy.visitAndCheckAppMount(`/companies/${testStoredCompany.companyId}/frameworks/${DataTypeEnum.Lksg}`);
      setRequestStatusToAnswered(dataRequestId);
      checkForReviewButtonsAndClick("reOpenRequestButton", "closeRequestButton");
    });

    it("Create two datasets, set the status of one request of them to answered, then update this request on the view page", () => {
      cy.ensureLoggedIn(premium_user_name, premium_user_pw);
      cy.visitAndCheckAppMount(`/companies/${testStoredCompany.companyId}/frameworks/${DataTypeEnum.Lksg}`);
      cy.get('[data-test="singleDataRequestButton"]').should("exist").click();
      singleDataRequestPage.chooseReportingPeriod("2021");
      cy.intercept("POST", "**/community/requests/single").as("postRequestData");
      submit();
      let dataRequestId: string;
      cy.wait("@postRequestData", { timeout: Cypress.env("short_timeout_in_ms") as number }).then((interception) => {
        uploadFrameworkDataForCompany(testStoredCompany.companyId, "2021");
        if (interception.response !== undefined) {
          const responseBody = interception.response.body as StoredDataRequest[];
          dataRequestId = responseBody[0].dataRequestId;
        }
        setRequestStatusToAnswered(dataRequestId);
        cy.visitAndCheckAppMount(`/companies/${testStoredCompany.companyId}/frameworks/${DataTypeEnum.Lksg}`);
        checkForReviewButtonsAndClickOnDropDownReportingPeriod("reOpenRequestButton", "closeRequestButton");

        setRequestStatusToAnswered(dataRequestId);
        cy.visitAndCheckAppMount(`/companies/${testStoredCompany.companyId}/frameworks/${DataTypeEnum.Lksg}`);
        setRequestStatusToAnswered(dataRequestId);
        checkForReviewButtonsAndClickOnDropDownReportingPeriod("closeRequestButton", "reOpenRequestButton");
      });
    });

    /**
     * Checks for the two review request buttons, clicks the one to click and checks if the buttons are gone afterwards
     * @param buttonToClick the data-test label of the button to click
     * @param buttonNotToClick the data-test label of the button not to click
     */
    function checkForReviewButtonsAndClick(buttonToClick: string, buttonNotToClick: string): void {
      const buttonNotToClickSelector = `[data-test="${buttonNotToClick}"]`;
      const buttonToClickSelector = `[data-test="${buttonToClick}"]`;

      cy.get(buttonNotToClickSelector).should("exist");
      cy.get(buttonToClickSelector).should("exist").click();
      cy.get('button[aria-label="CLOSE"]').should("be.visible").click();
      cy.get(buttonNotToClickSelector).should("not.exist");
      cy.get(buttonToClickSelector).should("not.exist");
    }
    /**
     * Checks for the two review request buttons, clicks the one to click and chooses the clickable
     * reporting period and checks if the buttons are gone afterwards
     * @param buttonToClick the data-test label of the button to click
     * @param buttonNotToClick the data-test label of the button not to click
     */
    function checkForReviewButtonsAndClickOnDropDownReportingPeriod(
      buttonToClick: string,
      buttonNotToClick: string,
    ): void {
      const buttonNotToClickSelector = `[data-test="${buttonNotToClick}"]`;
      const buttonToClickSelector = `[data-test="${buttonToClick}"]`;
      cy.get(buttonNotToClickSelector).should("exist");
      cy.get(buttonToClickSelector).should("exist").click();
      cy.get('[data-test="reporting-periods"] a').contains("2020").should("not.have.class", "link");
      cy.get('[data-test="reporting-periods"] a').contains("2021").should("have.class", "link").click();
      cy.get('button[aria-label="CLOSE"]').should("be.visible").click();
      cy.get(buttonNotToClickSelector).should("not.exist");
      cy.get(buttonToClickSelector).should("not.exist");
    }
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
    /**
     * Sets the status of a single data request from open to answered
     * @param dataRequestId the ID of the request
     */
    function setRequestStatusToAnswered(dataRequestId: string): void {
      cy.intercept("PATCH", "**/community/requests/*/requestStatus*").as("patchRequestStatus");
      getKeycloakToken(admin_name, admin_pw).then((token: string) => {
        const requestControllerApi = new RequestControllerApi(new Configuration({ accessToken: token }));
        requestControllerApi.patchDataRequestStatus(dataRequestId, RequestStatus.Answered).catch((reason) => {
          console.error(reason);
          throw reason;
        });
      });
      cy.wait("@patchRequestStatus", { timeout: Cypress.env("short_timeout_in_ms") as number });
    }
  },
);
