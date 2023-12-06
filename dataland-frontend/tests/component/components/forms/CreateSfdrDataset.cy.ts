import CreateSfdrDataset from "@/components/forms/CreateSfdrDataset.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { submitButton } from "@sharedUtils/components/SubmitButton";
import { DataTypeEnum } from "@clients/backend";
import { uploadDocuments } from "@sharedUtils/components/UploadDocuments";

const createSfdrDataset = {
  fillRequiredFields(): void {
    this.fillDateFieldWithFutureDate("dataDate");
    cy.get('input[name="fiscalYearDeviation"][value="Deviation"]').click();
    this.fillDateFieldWithFutureDate("fiscalYearEnd");
  },
  fillDateFieldWithFutureDate(fieldName: string): void {
    cy.get(`[data-test="${fieldName}"] button`).should("have.class", "p-datepicker-trigger").click();
    cy.get(`input[name="${fieldName}"]`).should("not.be.visible");
    cy.get("div.p-datepicker").find('button[aria-label="Next Month"]').click();
    cy.get("div.p-datepicker").find('span:contains("11")').click();
  },
};

describe("Component tests for the CreateSfdrDataset that test report uploading", () => {
  const hashForFileWithOneByteSize = "6e340b9cffb37a989ca544e6bb780a2c78901d3fb33738768511a30617afa01d";
  const hashForFileWithTwoBytesSize = "96a296d224f285c67bee93c30f8a309157f0daa35dc5b87e410b78630a09cfc7";
  const hashForFileWithThreeBytesSize = "709e80c88487a2411e1ee4dfb9f22a861492d20c4765150c0c794abd70f8147c";

  it("Check if the document uploads do not interfere", () => {
    const companyId = "company-id";
    console.log(hashForFileWithOneByteSize);
    const setOfHashesThatShouldBeCheckedForExistence = new Set([
      hashForFileWithOneByteSize,
      hashForFileWithTwoBytesSize,
      hashForFileWithThreeBytesSize,
    ]);
    setOfHashesThatShouldBeCheckedForExistence.forEach((hash) => {
      console.log(hash);
      cy.intercept("HEAD", "**/documents/" + hash, (request) => {
        request.reply(200, {});
      }).as(`documentExists-${hash}`);
    });
    cy.intercept(`/documents/*`, cy.spy().as("documentExists"));
    cy.intercept("POST", `/api/data/${DataTypeEnum.Sfdr}`, {
      statusCode: 200,
    });
    cy.mountWithPlugins(CreateSfdrDataset, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        companyID: companyId,
      },
    }).then(() => {
      createSfdrDataset.fillRequiredFields();

      uploadDocuments.selectDummyFile("first", 1, "referencedReports");
      cy.get("div[data-test='scope1GhgEmissionsInTonnes'] select[name='fileName']").select("first");

      cy.get('[data-test="sustainableAgriculturePolicy"] input[type="radio"][name="value"][value="Yes"]').check();
      uploadDocuments.selectDummyFile("second", 2, "sustainableAgriculturePolicy");
      cy.get('[data-test="sustainableOceansAndSeasPolicy"] input[type="radio"][name="value"][value="Yes"]').check();
      uploadDocuments.selectDummyFile("third", 3, "sustainableOceansAndSeasPolicy");
      cy.get('[data-test="environmentalPolicy"] input[type="radio"][name="value"][value="Yes"]').check();
      uploadDocuments.selectDummyFile("fourth", 1, "environmentalPolicy");

      cy.wait(100);
      submitButton.buttonAppearsEnabled();
      submitButton.clickButton();

      setOfHashesThatShouldBeCheckedForExistence.forEach((hash) => {
        cy.wait(`@documentExists-${hash}`);
      });
      cy.wait(100);
      cy.get("@documentExists").should("have.been.calledThrice");
    });
  });

  it("Check if the document uploads still work properly if some document got removed or replaced", () => {
    const companyId = "company-id";
    console.log(hashForFileWithOneByteSize);
    const setOfHashesThatShouldBeCheckedForExistence = new Set([hashForFileWithTwoBytesSize]);
    setOfHashesThatShouldBeCheckedForExistence.forEach((hash) => {
      console.log(hash);
      cy.intercept("HEAD", "**/documents/" + hash, (request) => {
        request.reply(200, {});
      }).as(`documentExists-${hash}`);
    });
    cy.intercept(`**/documents/*`, cy.spy().as("documentExists"));
    cy.intercept("POST", `/api/data/${DataTypeEnum.Sfdr}`, {
      statusCode: 200,
    });
    cy.mountWithPlugins(CreateSfdrDataset, {
      keycloak: minimalKeycloakMock({}),
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        companyID: companyId,
      },
    }).then(() => {
      createSfdrDataset.fillRequiredFields();

      cy.get('[data-test="sustainableAgriculturePolicy"] input[type="radio"][name="value"][value="Yes"]').check();
      uploadDocuments.selectDummyFile("second", 1, "sustainableAgriculturePolicy");
      cy.wait(100);
      cy.get('div[data-test="sustainableAgriculturePolicy"] button .pi-times').click();
      uploadDocuments.selectDummyFile("third", 2, "sustainableAgriculturePolicy");
      cy.get('[data-test="environmentalPolicy"] input[type="radio"][name="value"][value="Yes"]').check();
      uploadDocuments.selectDummyFile("fourth", 3, "environmentalPolicy");
      cy.wait(100);
      cy.get('div[data-test="environmentalPolicy"] button .pi-times').click();

      cy.wait(100);
      submitButton.buttonAppearsEnabled();
      submitButton.clickButton();

      setOfHashesThatShouldBeCheckedForExistence.forEach((hash) => {
        cy.wait(`@documentExists-${hash}`);
      });
      cy.wait(100);
      cy.get("@documentExists").should("have.been.calledOnce");
    });
  });
});
