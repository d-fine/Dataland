import RiskAssessmentFormElement from '@/components/forms/parts/elements/derived/RiskAssessmentFormElement.vue';
describe('Component tests for the lksg Risk Assessments Form Element', () => {
  it(
    'Ensure that the free text form field for counteracting measures is only displayed if the ' +
      'question before is set to yes ',
    () => {
      cy.mountWithPlugins(RiskAssessmentFormElement, {}).then(() => {
        cy.get('[data-test="counteractingMeasures"]').should('exist');
        cy.get('[data-test="listedMeasures"]').should('not.exist');
        cy.pause();
        cy.get('div[data-test="counteractingMeasures"]').find('input[type="checkbox"][value="Yes"]').click();
        cy.get('[data-test="listedMeasures"]').should('exist');

        cy.get('div[data-test="counteractingMeasures"]').find('input[type="checkbox"][value="No"]').click();

        cy.get('[data-test="listedMeasures"]').should('not.exist');
      });
    }
  );
});
