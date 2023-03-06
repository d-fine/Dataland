import { getKeycloakToken } from "@e2e/utils/Auth";
import { reader_name, reader_pw, uploader_name, uploader_pw } from "@e2e/utils/Cypress";
import {
  CompanyDataControllerApi,
  Configuration,
  DataTypeEnum,
  LksgData,
  LksgDataControllerApi,
} from "@clients/backend";
import { FixtureData } from "@e2e/fixtures/FixtureUtils";
import { getPreparedFixture } from "@e2e/utils/GeneralApiUtils";
import { uploadCompanyAndLksgDataViaApi } from "@e2e/utils/LksgUpload";

describe("Validates the edit button functionality on the view framework page", () => {
  /**
   * Uses the dataland API to retrieve the id of an arbitrary company with data for the specified data framework
   *
   * @param dataType the framework to search a company for
   * @returns a cypress chainable containing the id of a company with data for the specified framework
   */
  function getFirstCompanyIdFromApiWithFramework(dataType: DataTypeEnum): Cypress.Chainable<string> {
    return getKeycloakToken(reader_name, reader_pw).then(async (token) => {
      const data = await new CompanyDataControllerApi(new Configuration({ accessToken: token })).getCompanies(
        undefined,
        new Set([dataType])
      );
      return data.data[0].companyId;
    });
  }

  it("Should not display the edit and create new dataset button on the framework view page for a data reader", () => {
    getFirstCompanyIdFromApiWithFramework(DataTypeEnum.Lksg).then((companyId) => {
      cy.ensureLoggedIn(reader_name, reader_pw);
      cy.visit(`/companies/${companyId}/frameworks/lksg`);
      cy.get("h2").contains("LkSG Data").should("be.visible");
      cy.get("button:contains(EDIT)").should("not.exist");
      cy.get("button:contains(NEW DATASET)").should("not.exist");
    });
  });

  it("Should only display the add new dataset button for a framework for which the add new dataset functionality is not implemented", () => {
    getFirstCompanyIdFromApiWithFramework(DataTypeEnum.Sfdr).then((companyId) => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
      cy.visit(`/companies/${companyId}/frameworks/sfdr`);
      cy.get("h2").contains("SFDR data").should("be.visible");
      cy.get("button:contains(EDIT)").should("not.exist");
      cy.get("button:contains(NEW DATASET)").should("exist").click();
      cy.get("div").contains("New Dataset - Framework").should("be.visible");
    });
  });

  let preparedFixtures: Array<FixtureData<LksgData>>;

  before(function () {
    cy.fixture("CompanyInformationWithLksgPreparedFixtures").then(function (jsonContent) {
      preparedFixtures = jsonContent as Array<FixtureData<LksgData>>;
    });
  });

  it("Should display a working edit button to data uploaders on the LKSG page", () => {
    const preparedFixture = getPreparedFixture("lksg-all-fields", preparedFixtures);
    getKeycloakToken(uploader_name, uploader_pw)
      .then(async (token: string) =>
        uploadCompanyAndLksgDataViaApi(
          token,
          preparedFixture.companyInformation,
          preparedFixture.t,
          preparedFixture.reportingPeriod
        )
      )
      .then((uploadId) => {
        cy.ensureLoggedIn(uploader_name, uploader_pw);
        cy.visit(`/companies/${uploadId.companyId}/frameworks/lksg`);
        cy.get("h2").contains("LkSG Data").should("be.visible");
        cy.get("button:contains(EDIT)").should("be.visible").click();
        cy.get("a").contains("EDIT").should("be.visible").click();
        cy.get("div").contains("New Dataset - LkSG").should("be.visible");
        cy.get("button").contains("ADD DATA").should("exist").click();
        cy.get("h4")
          .contains("Upload successfully executed.")
          .should("exist")
          .then(() => {
            return getKeycloakToken(uploader_name, uploader_pw).then(async (token) => {
              const data = await new LksgDataControllerApi(
                new Configuration({ accessToken: token })
              ).getAllCompanyLksgData(uploadId.companyId, false);
              expect(data.data).to.have.length(2);
              expect(data.data[0].data).to.deep.equal(data.data[1].data);
            });
          });
      });
  });
});
