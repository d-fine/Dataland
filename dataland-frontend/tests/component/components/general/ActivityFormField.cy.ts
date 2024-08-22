// @ts-nocheck
import AlignedActivitiesFormField from '@/components/forms/parts/kpiSelection/AlignedActivitiesFormField.vue';

describe('Component test for ActivitiesFormField', () => {
  it('ActivitiesFormField component works correctly', () => {
    cy.mountWithPlugins(AlignedActivitiesFormField, {});
    cy.get('div[data-test="dataPointToggleButton"]').should('exist').click();
    cy.get('button[data-test="addNewProductButton"]').should('exist').click();
    cy.get('button[data-test="addNewProductButton"]').should('exist').click();
    cy.get('button[data-test="dataTestChooseActivityButton"]').should('have.length', 2);
    cy.get('button[data-test="dataTestChooseActivityButton"]').eq(1).click();
    cy.get('div[data-test="activityOverlayPanel"]').should('be.visible');
    cy.contains('.p-treenode-content', 'Construction and real estate').find('button').click();
    cy.get('ul.p-treenode-children')
      .should('be.visible')
      .find('li.p-treenode')
      .should('have.length', 10)
      .eq(1)
      .should('contain', 'Renovation of existing buildings')
      .find('div.p-radiobutton-box')
      .click({ force: true });
    cy.get('div[data-test="activityOverlayPanel"]').should('not.exist');
    cy.get('div[data-test="activityFormElement"]').should('contain', 'Renovation of existing buildings');
    cy.get('button[data-test="dataTestChooseActivityButton"]').eq(1).should('contain', 'Change Activity');
    cy.get('div[data-test="selectNaceCodes"]').eq(1).should('exist').find('div.p-multiselect-label-container').click();
    cy.get('li.p-multiselect-item').should('have.length', 2);
    cy.get('li.p-multiselect-item').eq(0).find('.p-checkbox-box').click();
    cy.get('li.p-multiselect-item').eq(1).find('.p-checkbox-box').click();
    cy.get('div[data-test="selectNaceCodes"]')
      .eq(1)
      .find('div.p-multiselect-label')
      .should('contain', '41 - Construction of buildings, 43 - Specialised construction activities');
    cy.get('em[data-test="removeButton"]').eq(1).click();
    cy.get('div[data-test="alignedActivitiesSection"]').eq(1).should('not.exist');
  });
});
