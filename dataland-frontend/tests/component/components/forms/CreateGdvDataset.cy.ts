import CreateGdvDataset from "@/components/forms/CreateGdvDataset.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { submitButton } from "@sharedUtils/components/SubmitButton";

describe("Component tests for the CreateP2pDataset that test dependent fields", () => {
  it("On the upload page, ensure that sectors can be selected and deselected and the submit looks as expected", () => {
    cy.mountWithPlugins(CreateGdvDataset, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      submitButton.buttonAppearsDisabled();

      cy.get('input[id="berichtspflichtUndEinwilligungZurVeroeffentlichung-option-no"]').click();
      submitButton.buttonAppearsDisabled();

      submitButton.clickButton();
      cy.contains("li", 'Sie müssen "Ja" wählen, um den Datensatz abschicken zu können.').should("exist");

      cy.get('input[id="berichtspflichtUndEinwilligungZurVeroeffentlichung-option-yes"]').click();
      submitButton.buttonAppearsEnabled();
      cy.contains("li", 'Sie müssen "Ja" wählen, um den Datensatz abschicken zu können.').should("not.exist");
    });
  });
});
