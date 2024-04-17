import ViewDataRequestPage from "@/components/pages/ViewDataRequestPage.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { RequestStatus, type StoredDataRequest, type StoredDataRequestMessageObject } from "@clients/communitymanager";
import type { BasicCompanyInformation, DataMetaInformation } from "@clients/backend";
import { QaStatus } from "@clients/backend";
import { convertUnixTimeInMsToDateString } from "@/utils/DataFormatUtils";
import { humanizeStringOrNumber } from "@/utils/StringFormatter";

describe("Component tests for the view data request page", function (): void {
  const requestId = "dummyRequestId";
  const dummyCompanyId = "dummyCompanyId";
  const dummyCompanyName = "dummyCompanyName";
  const dummyFramework = "dummyFramework";
  const dummyReportingYear = "dummyReportingYear";
  const dummyLastModifiedDate = 1709204495770;
  const dummyCreationTime = 1709104495770;
  const dummyMessageObject = {
    contacts: new Set<string>(["test@example.com", "test2@example.com"]),
    message: "test message",
    creationTimestamp: dummyCreationTime,
  } as StoredDataRequestMessageObject;
  /**
   * Return a stored data request
   * @param requestStatus the request status
   * @param messageHistory the message history
   * @returns stored data request
   */
  function getStoredDataRequest(
    requestStatus: RequestStatus,
    messageHistory: Array<StoredDataRequestMessageObject>,
  ): StoredDataRequest {
    return {
      requestStatus: requestStatus,
      reportingPeriod: dummyReportingYear,
      dataType: dummyFramework,
      dataRequestId: requestId,
      lastModifiedDate: dummyLastModifiedDate,
      datalandCompanyId: dummyCompanyId,
      messageHistory: messageHistory,
      creationTimestamp: dummyCreationTime,
      userId: "dummyUserId",
    } as StoredDataRequest;
  }
  /**
   * Mocks the community-manager answer for single data request of the users
   * @param request the request to mock
   */
  function interceptUserAskForSingleDataRequestsOnMounted(request: StoredDataRequest): void {
    cy.intercept(`**/community/requests/dummyRequestId`, {
      body: request,
      status: 200,
    }).as("fetchSingleDataRequests");
  }
  /**
   * Mocks the api-manager answer for basic company information
   * @param qaStatus the desired active dataset
   */
  function interceptUserActiveDatasetOnMounted(qaStatus: QaStatus): void {
    cy.intercept(`**/api/metadata?**`, {
      body: [
        {
          qaStatus: qaStatus,
        } as DataMetaInformation,
      ],
      status: 200,
    }).as("fetchActiveDatasets");
  }
  /**
   * Mocks the api-manager answer for basic company information
   */
  function interceptUserAskForCompanyNameOnMounted(): void {
    cy.intercept(`**/companies/dummyCompanyId/info`, {
      body: {
        companyName: dummyCompanyName,
      } as BasicCompanyInformation,
      status: 200,
    }).as("fetchCompanyName");
  }
  /**
   * Mocks the community-manager answer for patching a data request
   */
  function interceptPatchRequest(): void {
    cy.intercept(`**/community/requests/dummyRequestId/requestStatus?**`, {
      status: 200,
    }).as("fetchCompanyName");
  }
  /**
   * Checks the existence of basic elements of the page
   * @param requestStatus the request Status to check for
   */
  function checkBasicPageElements(requestStatus: RequestStatus): void {
    cy.contains("Data Request").should("exist");
    cy.contains("Request Details").should("exist").should("have.class", "card__title");
    cy.contains("Provided Contact Details & Messages").should("exist").should("have.class", "card__title");
    cy.contains("Request is").should("exist").should("have.class", "card__title");

    cy.get('[data-test="card_requestDetails"]')
      .should("exist")
      .within(() => {
        cy.contains("Company").should("exist");
        cy.contains(`${dummyCompanyName}`).should("exist");
        cy.contains("Framework").should("exist");
        cy.contains(`${humanizeStringOrNumber(dummyFramework)}`).should("exist");
        cy.contains("Reporting year").should("exist");
        cy.contains(`${dummyReportingYear}`).should("exist");
      });
    cy.get('[data-test="card_requestIs"]')
      .should("exist")
      .within(() => {
        cy.contains(`${requestStatus}`).should("exist");
        cy.contains(`${convertUnixTimeInMsToDateString(dummyLastModifiedDate)}`).should("exist");
      });
  }
  it("Check view data request page for open request without data and withdraw the data request", function () {
    interceptUserAskForSingleDataRequestsOnMounted(getStoredDataRequest(RequestStatus.Open, []));
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(QaStatus.Pending);
    interceptPatchRequest();
    cy.mountWithPlugins(ViewDataRequestPage, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        requestId: requestId,
      },
    }).then(() => {
      checkBasicPageElements(RequestStatus.Open);
      cy.get('[data-test="card_providedContactDetails"]')
        .should("exist")
        .get('[data-test="newMessage"]')
        .should("exist");

      cy.get('[data-test="resolveRequestButton"]').should("not.exist");
      cy.get('[data-test="viewDataset"]').should("not.exist");
      cy.get('[data-test="card_withdrawn"]')
        .should("exist")
        .within(() => {
          cy.contains(
            "Once a data request is withdrawn, it will be removed from your data request list." +
              " The data owner will not be notified anymore.",
          ).should("exist");
          cy.contains("Withdraw Request").should("exist");
          cy.contains("Withdraw request.").should("exist").click();
        });
      cy.get('[data-test="successModal"]').should("exist").should("be.visible").contains("CLOSE").click();
      cy.get('[data-test="successModal"]').should("not.exist");
    });
  });
  it("Check view data request page for open request with data and check the routing to data view page", function () {
    interceptUserAskForSingleDataRequestsOnMounted(getStoredDataRequest(RequestStatus.Open, []));
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(QaStatus.Accepted);
    interceptPatchRequest();
    cy.mountWithPlugins(ViewDataRequestPage, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        requestId: requestId,
      },
    }).then((mounted) => {
      checkBasicPageElements(RequestStatus.Open);
      cy.get('[data-test="viewDataset"]').should("exist").click();
      cy.wrap(mounted.component)
        .its("$route.path")
        .should("eq", `/companies/${dummyCompanyId}/frameworks/${dummyFramework}`);
    });
  });
  it("Check view data request page for answered request and check the routing to data view page", function () {
    const dummyRequest = getStoredDataRequest(RequestStatus.Answered, []);
    interceptUserAskForSingleDataRequestsOnMounted(dummyRequest);
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(QaStatus.Accepted);
    interceptPatchRequest();
    cy.mountWithPlugins(ViewDataRequestPage, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        requestId: requestId,
      },
    }).then((mounted) => {
      checkBasicPageElements(dummyRequest.requestStatus);
      cy.get('[data-test="resolveRequestButton"]').should("exist").click();
      cy.wrap(mounted.component)
        .its("$route.path")
        .should("eq", `/companies/${dummyCompanyId}/frameworks/${dummyFramework}`);
    });
  });
  it(
    "Check view data request page for open request and check that the message history is displayed " +
      "and that a user can add a new message",
    function () {
      interceptUserAskForSingleDataRequestsOnMounted(
        getStoredDataRequest(RequestStatus.Open, [dummyMessageObject] as Array<StoredDataRequestMessageObject>),
      );
      interceptUserAskForCompanyNameOnMounted();
      interceptUserActiveDatasetOnMounted(QaStatus.Accepted);
      interceptPatchRequest();
      cy.mountWithPlugins(ViewDataRequestPage, {
        keycloak: minimalKeycloakMock({}),
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        props: {
          requestId: requestId,
        },
      }).then(() => {
        checkBasicPageElements(RequestStatus.Open);
        const testEmail = "test1234@example.com";
        const testMessage = "test message 1234";
        cy.get('[data-test="newMessage"]').should("exist").click();
        cy.get('[data-test="newMessageModal"]')
          .should("exist")
          .should("be.visible")
          .within(() => {
            cy.get('[data-test="checkbox"]').should("not.exist");
            cy.get('[data-test="contactEmail"]').should("exist").type(testEmail);
            cy.get('[data-test="dataRequesterMessage"]').should("exist").type(testMessage);
            cy.get('[data-test="checkbox"]').should("exist").should("be.visible");
          });
        cy.get('[data-test="addMessageButton"]').should("exist").click();
        cy.get('[data-test="newMessageModal"]')
          .should("exist")
          .should("be.visible")
          .contains("You have to accept the terms and conditions to add a message");
        cy.get('[data-test="checkbox"]').should("exist").should("be.visible").click();
        cy.get('[data-test="addMessageButton"]').should("exist").click();
        cy.get('[data-test="newMessageModal"]').should("not.exist");
      });
    },
  );
  it("Check view data request page for closed request with data renders as expected", function () {
    interceptUserAskForSingleDataRequestsOnMounted(
      getStoredDataRequest(RequestStatus.Closed, [dummyMessageObject] as Array<StoredDataRequestMessageObject>),
    );
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(QaStatus.Accepted);
    interceptPatchRequest();
    cy.mountWithPlugins(ViewDataRequestPage, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        requestId: requestId,
      },
    }).then((mounted) => {
      checkBasicPageElements(RequestStatus.Closed);
      cy.get('[data-test="newMessage"]').should("not.exist");
      cy.get('[data-test="card_withdrawn"]').should("not.exist");
      cy.get('[data-test="resolveRequestButton"]').should("not.exist");
      cy.get('[data-test="viewDataset"]').should("exist").click();
      cy.wrap(mounted.component)
        .its("$route.path")
        .should("eq", `/companies/${dummyCompanyId}/frameworks/${dummyFramework}`);
    });
  });
  it.only("Check view data request page for withdrawn request without data renders as expected", function () {
    interceptUserAskForSingleDataRequestsOnMounted(getStoredDataRequest(RequestStatus.Withdrawn, []));
    interceptUserAskForCompanyNameOnMounted();
    interceptUserActiveDatasetOnMounted(QaStatus.Rejected);
    interceptPatchRequest();
    cy.mountWithPlugins(ViewDataRequestPage, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        requestId: requestId,
      },
    }).then(() => {
      checkBasicPageElements(RequestStatus.Withdrawn);
      cy.get('[data-test="newMessage"]').should("not.exist");
      cy.get('[data-test="card_withdrawn"]').should("not.exist");
      cy.get('[data-test="resolveRequestButton"]').should("not.exist");
      cy.get('[data-test="viewDataset"]').should("not.exist");
    });
  });
});
