import CreateEsgQuestionnaireDataset from "@/components/forms/CreateEsgQuestionnaireDataset.vue";
import { minimalKeycloakMock } from "@ct/testUtils/Keycloak";
import { submitButton } from "@sharedUtils/components/SubmitButton";

describe("Component tests for the esg questionnaire upload page", () => {
  it("Ensure that the customized validation message appears if a user selects No in the first question ", () => {
    cy.mountWithPlugins(CreateEsgQuestionnaireDataset, {
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
