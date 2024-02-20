import CreateLksgDataset from "@/components/forms/CreateLksgDataset.vue";
import {minimalKeycloakMock} from "@ct/testUtils/Keycloak";
import {FixtureData, getPreparedFixture} from "@sharedUtils/Fixtures";
import {CompanyAssociatedDataLksgData, LksgData} from "@clients/backend";

describe("test YesNoBaseDataPointFormField for entries", () => {
  let preparedFixtures: Array<FixtureData<LksgData>>
  before(() => {
    cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
    });
  });

  it("Edit and subsequent upload should work properly when removing or changing referenced documents", () => {
    const dummyData = getPreparedFixture("lksg-all-fields", preparedFixtures).t;
    const dummyCompanyAssociatedData: CompanyAssociatedDataLksgData = {
      companyId: "company-id",
      reportingPeriod: "2024",
      data: dummyData,
    };
    cy.intercept("**/api/data/lksg/*", (interception) => {
      interception.reply({ statusCode: 200, body: dummyCompanyAssociatedData })
    })
    cy.mountWithPlugins(CreateLksgDataset, {
      keycloak: minimalKeycloakMock({}),
      data: () => ({
        route: {
          query: {
            templateDataId: "data-id"
          },
        },
      }),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        companyID: "company-id",
      },
    }).then(() => {
      cy.get("button[data-test='files-to-upload-remove']")
        .first()
        .parents('[data-test^="BaseDataPointFormField"]')
        .first()
        .find("input.p-radiobutton")
        .eq(1)
        .click()
        .find("button[data-test='files-to-upload-remove']")
        .should("not.exist");
    });
  });
});
