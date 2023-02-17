import { DatasetQualityStatus, DataTypeEnum } from "@clients/backend";
import Keycloak from "keycloak-js";
import { getCompanyDataForFrameworkDataSearchPage } from "@/utils/SearchCompaniesForFrameworkDataPageDataRequester";

describe("Component test for ViewFrameworkBase", () => {
  it("Should display only accepted datasets", async () => {
    const acceptedCompanyName = "company-1";
    cy.intercept("**/api/companies*", {
      statusCode: 200,
      body: [
        {
          companyId: acceptedCompanyName,
          companyInformation: {
            companyName: "company-1",
            identifiers: [
              {
                identifierType: "PermId",
                identifierValue: "company-1",
              },
            ],
          },
          dataRegisteredByDataland: [
            {
              dataId: "dataset-1",
              qualityStatus: DatasetQualityStatus.Accepted,
            },
            {
              dataId: "dataset-2",
              qualityStatus: DatasetQualityStatus.Pending,
            },
          ],
        },
        {
          companyId: "company-2",
          companyInformation: {
            companyName: "company-2",
            identifiers: [
              {
                identifierType: "PermId",
                identifierValue: "company-2",
              },
            ],
          },
          dataRegisteredByDataland: [
            {
              dataId: "dataset-3",
              qualityStatus: DatasetQualityStatus.Pending,
            },
          ],
        },
      ],
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
      expect(storedCompanies[0].companyId).to.equal(acceptedCompanyName);
    });
  });
});
