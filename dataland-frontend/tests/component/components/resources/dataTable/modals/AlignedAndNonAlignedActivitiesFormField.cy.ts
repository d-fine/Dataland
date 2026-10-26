import { mountEditFormField } from '@ct/testUtils/MountEditFormField.ts';
import AlignedActivitiesFormField from '@/components/resources/dataTable/modals/AlignedActivitiesFormField.vue';
import NonAlignedActivitiesFormField from '@/components/resources/dataTable/modals/NonAlignedActivitiesFormField.vue';

const row = { activityName: 'Afforestation', naceCodes: ['A2'], share: { relativeShareInPercent: 10 } };
const dataPoint = { value: [row], quality: 'Reported', comment: '' };

const variants = [
  {
    name: 'AlignedActivitiesFormField',
    component: AlignedActivitiesFormField,
    expectsYesNoColumns: true,
    expectsGroupHeaderRow: true,
  },
  {
    name: 'NonAlignedActivitiesFormField',
    component: NonAlignedActivitiesFormField,
    expectsYesNoColumns: false,
    expectsGroupHeaderRow: false,
  },
];

variants.forEach(({ name, component, expectsYesNoColumns, expectsGroupHeaderRow }) => {
  describe(`Component test for ${name}`, () => {
    it('renders the activity read-only and the expected columns', () => {
      mountEditFormField(component, dataPoint);
      cy.get('[data-test="activity-name"]').should('have.text', 'Afforestation');
      cy.get('[data-test="activity-select"]').should('not.exist');
      cy.get('[data-test="percent-share.relativeShareInPercent"]').should('exist');
      if (expectsYesNoColumns) {
        cy.get('[data-test="yesno-minimumSafeguards"]').should('exist');
        cy.get('[data-test="yesno-dnshToClimateChangeMitigation"]').should('exist');
        cy.get('[data-test="percent-substantialContributionToClimateChangeMitigationInPercent"]').should('exist');
      } else {
        cy.get('[data-test^="yesno-"]').should('not.exist');
      }
    });

    it('renders the same headers as the view modal', () => {
      mountEditFormField(component, dataPoint);
      const thead = '[data-test="activities-edit-table"] thead';
      cy.get(thead).should('contain.text', 'Revenue (%)');
      cy.get(`${thead} tr`).should('have.length', expectsGroupHeaderRow ? 2 : 1);
      if (expectsGroupHeaderRow) {
        cy.get(thead).should('contain.text', 'DNSH Criteria');
        cy.get(thead).should('contain.text', 'Minimum Safeguards');
      }
    });

    it('adds a row via the select and deletes rows', () => {
      mountEditFormField(component, dataPoint);
      cy.get('[data-test="add-activity-select"]').click();
      cy.get('.p-select-option').first().click();
      cy.get('[data-test="activity-name"]').should('have.length', 2);
      cy.get('[data-test="delete-activity"]').last().click();
      cy.get('[data-test="activity-name"]').should('have.length', 1);
    });

    it('returns the list in the api body', () => {
      mountEditFormField(component, dataPoint);
      cy.get('@component').then((instance: { buildApiBodyWithExtendedInfo: () => string }) => {
        const body = JSON.parse(instance.buildApiBodyWithExtendedInfo());
        expect(body.value).to.have.length(1);
        expect(body.value[0].activityName).to.equal('Afforestation');
      });
    });

    it('writes edited amount, currency and share into the api body', () => {
      mountEditFormField(component, dataPoint);
      cy.get('input#amount-0').clear().type('1500').blur();
      cy.get('[data-test="currency-select"]').click();
      cy.get('.p-select-filter').type('(EUR)');
      cy.get('.p-select-option').contains('(EUR)').click();
      cy.get('input#share\\.relativeShareInPercent-0').clear().type('42').blur();
      cy.get('@component').then((instance: { buildApiBodyWithExtendedInfo: () => string }) => {
        const activity = JSON.parse(instance.buildApiBodyWithExtendedInfo()).value[0];
        expect(activity.share.absoluteShare).to.deep.equal({ amount: 1500, currency: 'EUR' });
        expect(activity.share.relativeShareInPercent).to.equal(42);
      });
    });

    it('is invalid when a percentage is out of range', () => {
      mountEditFormField(component, {
        ...dataPoint,
        value: [{ ...row, share: { relativeShareInPercent: 150 } }],
      });
      cy.get('@component').its('isPageValid').should('equal', false);
    });

    it('uses the provided KPI type in the column headers', () => {
      mountEditFormField(component, dataPoint, { activityKpiType: 'opex' });
      cy.get('[data-test="activities-edit-table"] thead th').then(($headers) => {
        const headerTexts = [...$headers].map((header) => header.innerText.trim());
        expect(headerTexts).to.include.members(['OpEx', 'OpEx (%)']);
        expect(headerTexts).not.to.include('Revenue');
      });
    });
  });
});
