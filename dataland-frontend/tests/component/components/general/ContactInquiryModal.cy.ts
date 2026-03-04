import ContactInquiryModal from '@/components/general/ContactInquiryModal.vue';
import { getMountingFunction } from '@ct/testUtils/Mount';
import { useContactModal } from '@/composables/useContactModal';

describe('ContactInquiryModal', () => {
  beforeEach(() => {
    useContactModal().openModal();
  });

  afterEach(() => {
    useContactModal().closeModal();
  });

  it('idle state — form fields are rendered', () => {
    getMountingFunction()(ContactInquiryModal, {}).then(() => {
      cy.get('[name="contactName"]').should('exist');
      cy.get('[name="organisation"]').should('exist');
      cy.get('[name="contactEmail"]').should('exist');
      cy.get('[name="message"]').should('exist');
    });
  });

  it('submit button is disabled when required fields are empty', () => {
    getMountingFunction()(ContactInquiryModal, {}).then(() => {
      cy.get('[data-test="submit-button"]').should('be.disabled');
    });
  });

  it('blur contactName without value shows validation error', () => {
    getMountingFunction()(ContactInquiryModal, {}).then(() => {
      cy.get('[name="contactName"]').focus().blur();
      cy.get('.formkit-messages').should('exist');
    });
  });

  it('blur contactEmail with invalid format shows validation error', () => {
    getMountingFunction()(ContactInquiryModal, {}).then(() => {
      cy.get('[name="contactEmail"]').type('not-an-email').blur();
      cy.get('.formkit-messages').should('exist');
    });
  });

  it('success flow — button disabled during submission, success message shown, modal auto-closes after 3s', () => {
    cy.intercept('POST', '/community/inquiry', { statusCode: 200, body: {} }).as('inquiry');
    cy.clock();
    getMountingFunction()(ContactInquiryModal, {}).then(() => {
      cy.get('[name="contactName"]').type('Jane Doe');
      cy.get('[name="contactEmail"]').type('jane@example.com');
      cy.get('[name="message"]').type('Hello Dataland');
      cy.get('[data-test="submit-button"]').click();
      cy.get('[data-test="submit-button"]').should('be.disabled');
      cy.wait('@inquiry');
      cy.get('[aria-live="polite"]').should('contain', 'Your message has been sent');
      cy.tick(3000);
      cy.get('[role="dialog"]').should('not.exist');
    });
  });

  it('error flow — error message shown and modal stays open', () => {
    cy.intercept('POST', '/community/inquiry', { statusCode: 500, body: {} }).as('inquiry');
    getMountingFunction()(ContactInquiryModal, {}).then(() => {
      cy.get('[name="contactName"]').type('Jane Doe');
      cy.get('[name="contactEmail"]').type('jane@example.com');
      cy.get('[name="message"]').type('Hello Dataland');
      cy.get('[data-test="submit-button"]').click();
      cy.wait('@inquiry');
      cy.get('[aria-live="assertive"]').should('contain', 'Something went wrong');
      cy.get('[role="dialog"]').should('exist');
    });
  });

  it('double-submit prevention — submit button disabled while submitting', () => {
    cy.intercept('POST', '/community/inquiry', (req) => {
      req.reply({ delay: 500, statusCode: 200, body: {} });
    }).as('inquiry');
    getMountingFunction()(ContactInquiryModal, {}).then(() => {
      cy.get('[name="contactName"]').type('Jane Doe');
      cy.get('[name="contactEmail"]').type('jane@example.com');
      cy.get('[name="message"]').type('Hello Dataland');
      cy.get('[data-test="submit-button"]').click();
      cy.get('[data-test="submit-button"]').should('be.disabled');
      cy.wait('@inquiry');
    });
  });

  it('Escape key closes modal in idle state', () => {
    getMountingFunction()(ContactInquiryModal, {}).then(() => {
      cy.get('[role="dialog"]').should('exist');
      cy.get('body').type('{esc}');
      cy.get('[role="dialog"]').should('not.exist');
    });
  });
});