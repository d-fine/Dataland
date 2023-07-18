import { QaStatus, DataTypeEnum, StoredCompany, IdentifierType } from "@clients/backend";
import Keycloak from "keycloak-js";
import { getCompanyDataForFrameworkDataSearchPage } from "@/utils/SearchCompaniesForFrameworkDataPageDataRequester";

describe("Component test for ViewFrameworkBase", () => {
  it("Should display only accepted datasets", async () => {
    const acceptedCompanyId = "company-1";
    const companiesResponseBody: Array<StoredCompany> = [
      {
        companyId: acceptedCompanyId,
        companyInformation: {
          companyName: "company-1",
          headquarters: "company-1-hq",
          countryCode: "DE",
          identifiers: {
            [IdentifierType.PermId]: ["company-1"],
          },
        },
        dataRegisteredByDataland: [
          {
            companyId: acceptedCompanyId,
            dataType: "sfdr",
            currentlyActive: true,
            reportingPeriod: "dataset-1",
            uploadTime: 0,
            dataId: "dataset-1",
            qaStatus: QaStatus.Accepted,
          },
          {
            companyId: acceptedCompanyId,
            dataType: "sfdr",
            currentlyActive: false,
            reportingPeriod: "dataset-2",
            uploadTime: 0,
            dataId: "dataset-2",
            qaStatus: QaStatus.Pending,
          },
        ],
      },
      {
        companyId: "company-2",
        companyInformation: {
          headquarters: "company-2-hq",
          countryCode: "DE",
          companyName: "company-2",
          identifiers: {
            [IdentifierType.PermId]: ["company-2"],
          },
        },
        dataRegisteredByDataland: [
          {
            companyId: "company-2",
            dataType: "sfdr",
            currentlyActive: false,
            reportingPeriod: "dataset-3",
            uploadTime: 0,
            dataId: "dataset-3",
            qaStatus: QaStatus.Pending,
          },
        ],
      },
    ];
    cy.intercept("**/api/companies*", {
      statusCode: 200,
      body: companiesResponseBody,
    });
    const storedCompanies = await getCompanyDataForFrameworkDataSearchPage(
      "",
      false,
      new Set<DataTypeEnum>(),
      new Set<string>(),
      new Set<string>(),
      Promise.resolve({} as Keycloak)
    );
    cy.wait(100).then(() => {
      expect(storedCompanies).to.have.length(1);
      expect(storedCompanies[0].companyId).to.equal(acceptedCompanyId);
    });
  });
});
