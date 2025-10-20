import CheckboxesListFormElementExtended from '@/components/forms/parts/elements/basic/CheckboxesListFormElementExtended.vue';

describe('Checks correct value setting of CheckboxesListFormElementExtended', () => {
  const options = [
    { value: 'yes', label: 'Yes' },
    { value: 'no', label: 'No' },
  ];

  /**
   * Mount the component with necessary props
   */
  function mountComponent(): void {
    // @ts-ignore
    cy.mountWithPlugins(CheckboxesListFormElementExtended, {
      props: {
        name: 'test',
        options,
        label: 'Test Label',
        description: 'Test Description',
        required: false,
      },
    });
  }

  it('sets yesNoValue to last value if multiple checkboxes are selected', () => {
    mountComponent();
    cy.get('.yes-no-checkboxes input').eq(0).click();
    cy.get('.yes-no-checkboxes input').eq(1).click();
    cy.get('.yes-no-checkboxes input').eq(0).should('not.be.checked');
    cy.get('.yes-no-checkboxes input').eq(1).should('be.checked');
    cy.get('input[name="value"]').should('have.value', 'no');
  });

  it('sets yesNoValue to only value if one checkbox is selected', () => {
    mountComponent();
    cy.get('.yes-no-checkboxes input').eq(0).click();
    cy.get('.yes-no-checkboxes input').eq(0).should('be.checked');
    cy.get('.yes-no-checkboxes input').eq(1).should('not.be.checked');
    cy.get('input[name="value"]').should('have.value', 'yes');
  });

  it('sets yesNoValue to undefined if no checkbox is selected', () => {
    mountComponent();
    cy.get('.yes-no-checkboxes input').eq(0).click();
    cy.get('.yes-no-checkboxes input').eq(0).click();
    cy.get('.yes-no-checkboxes input').eq(1).click();
    cy.get('.yes-no-checkboxes input').eq(1).click();
    cy.get('input[name="value"]').should('not.exist');
  });
});
