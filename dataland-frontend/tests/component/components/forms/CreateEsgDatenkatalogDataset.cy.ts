// @ts-nocheck
import CreateEsgDatenkatalogDataset from '@/components/forms/CreateEsgDatenkatalogDataset.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { submitButton } from '@sharedUtils/components/SubmitButton';

describe('Component tests for the esg datenkatalog upload page', () => {
  it('Ensure that the customized validation message appears if a user selects No in the first question ', () => {
    //ToDo Test und die dazugehörige Logik entfernen
    cy.mountWithPlugins(CreateEsgDatenkatalogDataset, {
      keycloak: minimalKeycloakMock({}),
    }).then(() => {
      submitButton.buttonAppearsDisabled();

      cy.get('input[id="berichtspflichtUndEinwilligungZurVeroeffentlichung-option-no"]').click();
      submitButton.buttonAppearsDisabled();

      submitButton.clickButton();
      cy.contains('li', 'Sie müssen "Ja" wählen, um den Datensatz abschicken zu können.').should('exist');

      cy.get('input[id="berichtspflichtUndEinwilligungZurVeroeffentlichung-option-yes"]').click();
      submitButton.buttonAppearsEnabled();
      cy.contains('li', 'Sie müssen "Ja" wählen, um den Datensatz abschicken zu können.').should('not.exist');

      cy.get('input[id="berichtspflichtUndEinwilligungZurVeroeffentlichung-option-yes"]').click();
      submitButton.buttonAppearsDisabled();
      cy.contains('li', 'Berichtspflicht und Einwilligung zur Veröffentlichung is required.').should('exist');

      submitButton.clickButton();
      cy.contains('li', 'Berichtspflicht und Einwilligung zur Veröffentlichung is required.').should('exist');

      cy.get('input[id="berichtspflichtUndEinwilligungZurVeroeffentlichung-option-no"]').click();
      submitButton.buttonAppearsDisabled();
      cy.contains('li', 'Sie müssen "Ja" wählen, um den Datensatz abschicken zu können.').should('exist');

      cy.get('input[id="berichtspflichtUndEinwilligungZurVeroeffentlichung-option-yes"]').click();
      submitButton.buttonAppearsEnabled();
      cy.contains('li', 'Berichtspflicht und Einwilligung zur Veröffentlichung is required.').should('not.exist');
      cy.contains('li', 'Sie müssen "Ja" wählen, um den Datensatz abschicken zu können.').should('not.exist');
    });
  });
});
