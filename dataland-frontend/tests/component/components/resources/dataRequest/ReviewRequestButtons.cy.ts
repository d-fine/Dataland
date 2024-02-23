import ReviewRequestButtonsComponent from "@/components/resources/dataRequest/ReviewRequestButtons.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { type DataMetaInformation, DataTypeEnum } from "@clients/backend";
import { RequestStatus, type StoredDataRequest } from "@clients/communitymanager";
describe("Component tests for the data request review buttons", function (): void {
  const mockCompanyId: string = "Mock-company-id";

  it("Check review functionality", function () {
    mockUserRequestsOnMounted();
    mockPatchRequestsOnMounted();

    const mockMapOfReportingPeriodToActiveDataset = new Map<string, DataMetaInformation>([
      ["2022", {} as DataMetaInformation],
    ]);
    mountReviewRequestButtonsWithProps(mockCompanyId, DataTypeEnum.Lksg, mockMapOfReportingPeriodToActiveDataset);
    checkForReviewButtonsPopUpModal("successText");
  });

  it("Check review functionality with error message", function () {
    mockUserRequestsOnMounted();
    const mockMapOfReportingPeriodToActiveDataset = new Map<string, DataMetaInformation>([
      ["2022", {} as DataMetaInformation],
    ]);
    mountReviewRequestButtonsWithProps(mockCompanyId, DataTypeEnum.Lksg, mockMapOfReportingPeriodToActiveDataset);
    checkForReviewButtonsPopUpModal("noSuccessText");
  });

  it("Check review functionality with multiple reporting periods", function () {
    mockUserRequestsOnMounted();
    mockPatchRequestsOnMounted();

    const mockMapOfReportingPeriodToActiveDataset = new Map<string, DataMetaInformation>([
      ["2020", {} as DataMetaInformation],
      ["2021", {} as DataMetaInformation],
      ["2022", {} as DataMetaInformation],
    ]);
    mountReviewRequestButtonsWithProps(mockCompanyId, DataTypeEnum.Lksg, mockMapOfReportingPeriodToActiveDataset);

    checkForReviewButtonsAndClickOnDropDownReportingPeriod("closeRequestButton", "reOpenRequestButton");

    checkForReviewButtonsAndClickOnDropDownReportingPeriod("reOpenRequestButton", "closeRequestButton");
  });
  /**
   * Checks for pop up modal
   * @param expectedPopUp expected pop up dialog
   */
  function checkForReviewButtonsPopUpModal(expectedPopUp: string): void {
    const popUpdataTestId = `[data-test="${expectedPopUp}"]`;
    cy.get('[data-test="closeRequestButton"]').should("exist").click();
    cy.get(popUpdataTestId).should("exist");
    cy.get('button[aria-label="CLOSE"]').should("be.visible").click();

    cy.get('[data-test="reOpenRequestButton"]').should("exist").click();
    cy.get(popUpdataTestId).should("exist");
    cy.get('button[aria-label="CLOSE"]').should("be.visible").click();
  }
  /**
   * Checks dropdown functionality of request review button
   * @param buttonToClick desired dialog
   * @param buttonNotToClick if false, display error message
   */
  function checkForReviewButtonsAndClickOnDropDownReportingPeriod(
    buttonToClick: string,
    buttonNotToClick: string,
  ): void {
    const buttonNotToClickSelector = `[data-test="${buttonNotToClick}"]`;
    const buttonToClickSelector = `[data-test="${buttonToClick}"]`;
    cy.get(buttonNotToClickSelector).should("exist");
    cy.get(buttonToClickSelector).should("exist").click();

    cy.get('[data-test="reporting-periods"] a').contains("2024").should("not.exist");
    cy.get('[data-test="reporting-periods"] a').contains("2020").should("not.have.class", "link");
    cy.get('[data-test="reporting-periods"] a').contains("2021").should("not.have.class", "link");
    cy.get('[data-test="reporting-periods"] a').contains("2022").should("have.class", "link").click();
    cy.get('button[aria-label="CLOSE"]').should("be.visible").click();
  }
  /**
   * Mocks the answers for all the requests
   */
  function mockUserRequestsOnMounted(): void {
    cy.intercept(`**/community/requests/user`, {
      body: [
        {
          dataType: DataTypeEnum.Lksg,
          dataRequestCompanyIdentifierValue: mockCompanyId,
          reportingPeriod: "2021",
          requestStatus: RequestStatus.Open,
          dataRequestId: "Mock-Request-Id",
        } as StoredDataRequest,
        {
          dataType: DataTypeEnum.Lksg,
          dataRequestCompanyIdentifierValue: mockCompanyId,
          reportingPeriod: "2022",
          requestStatus: RequestStatus.Answered,
          dataRequestId: "Mock-Request-Id",
        } as StoredDataRequest,
        {
          dataType: DataTypeEnum.Lksg,
          dataRequestCompanyIdentifierValue: mockCompanyId,
          reportingPeriod: "2024",
          requestStatus: RequestStatus.Answered,
          dataRequestId: "Mock-Request-Id",
        } as StoredDataRequest,
      ],
    }).as("fetchUserRequests");
  }
  /**
   * Mocks the answer for patching the request status
   */
  function mockPatchRequestsOnMounted(): void {
    cy.intercept(`**/requestStatus?requestStatus=Closed`, {
      body: {
        requestStatus: RequestStatus.Closed,
      } as StoredDataRequest,
      status: 200,
    }).as("closeUserRequest");
    cy.intercept(`**/requestStatus?requestStatus=Open`, {
      body: {
        requestStatus: RequestStatus.Open,
      } as StoredDataRequest,
      status: 200,
    }).as("reOpenUserRequest");
  }
  /**
   * Mount review request button component with given props
   * @param companyId companyId
   * @param framework framework
   * @param map mapOfReportingPeriodToActiveDataset
   */
  function mountReviewRequestButtonsWithProps(
    companyId: string,
    framework: DataTypeEnum,
    map: Map<string, DataMetaInformation>,
  ): void {
    cy.mountWithPlugins(ReviewRequestButtonsComponent, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        companyId: companyId,
        framework: framework,
        mapOfReportingPeriodToActiveDataset: map,
      },
    });
  }
});
