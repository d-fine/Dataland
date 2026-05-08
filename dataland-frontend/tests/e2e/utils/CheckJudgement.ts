import type { Interception } from 'cypress/types/net-stubbing';
import { AcceptedDataPointSource } from '@clients/qaservice';
import type { QaJudgement } from '@e2e/utils/CheckJudgementJson.ts';

/**
 * Selects the given data point type in the judge modal's "Next datapoint" dropdown.
 *
 * @param dataPointTypeId The data point type ID (e.g. DATA_POINT_TYPES.numberOfEmployees).
 */
export function selectNextDataPointToJudge(dataPointTypeId: string): void {
  cy.get('[data-test="next-datapoint-section"]').within(() => {
    cy.get('[data-test="next-datapoint-select"]').click();
  });

  // Store the selector in a variable to keep the code clean
  const optionSelector = `[data-test="next-datapoint-option-${dataPointTypeId}"]`;

  // Split the chain into two separate Cypress commands
  cy.get(optionSelector).scrollIntoView();
  cy.get(optionSelector).click({ force: true });
}

/**
 * Navigates to the currently selected data point in the judge modal.
 *
 * Used after selecting an entry in the "Next datapoint" section to open that
 * data point for review.
 */
export function goToSelectedDataPoint(): void {
  cy.get('[data-test="next-datapoint-section"]').within(() => {
    cy.get('[data-test="go-to-datapoint-button"]').click();
  });
}

/**
 * Helper that checks that the PATCH request to update a data point is called with the expected request body based on the judgement configuration.
 *
 * @param interception The Cypress interception object containing request and response details of the PATCH request.
 * @param judgement    The judgement configuration used to determine the expected request body values.
 */
export function checkPATCHDataPointsCalledCorrectly(interception: Interception, judgement: QaJudgement): void {
  expect(interception.response?.statusCode, 'PATCH status code').to.eq(200);

  const body = interception.request.body ?? {};

  if (judgement.acceptedSource == null) {
    expect(body.acceptedSource ?? null, 'acceptedSource in request body').to.eq(null);
  } else {
    expect(body.acceptedSource, 'acceptedSource in request body').to.eq(judgement.acceptedSource);
  }

  if (judgement.reporterUserIdOfAcceptedQaReport == null) {
    expect(body.reporterUserIdOfAcceptedQaReport ?? null, 'reporterUserIdOfAcceptedQaReport in request body').to.eq(
      null
    );
  } else {
    expect(body.reporterUserIdOfAcceptedQaReport, 'reporterUserIdOfAcceptedQaReport in request body').to.eq(
      judgement.reporterUserIdOfAcceptedQaReport
    );
  }

  if (judgement.customValue == null) {
    expect(body.customValue ?? null, 'customValue in request body').to.eq(null);
  } else {
    expect(body.customValue, 'customValue in request body').to.eq(judgement.customValue);
  }
}

/**
 * Advances to the next QA report entry in the judge modal and continues recursive navigation.
 *
 * The helper clicks the "next" control in the corrected data point section, verifies that the
 * current reporter label changed, and then calls `navigateToQaReport(...)`.
 *
 * Throws an error when no further entry can be opened (next button disabled), which indicates
 * that the target reporter was not found in the remaining QA reports.
 *
 * @param targetReporterName Reporter label that recursive navigation is trying to find.
 * @param currentLabel Current reporter label before clicking "next"; used to assert progress.
 */
function goToNextReportAndRecurse(targetReporterName: string, currentLabel: string): Cypress.Chainable<void> {
  cy.get('[data-test="corrected-datapoint-section"] [data-test="qa-next-button"]').then(($buttons) => {
    const $visible = $buttons.filter(':visible');
    const $next = $visible.length > 0 ? $visible.first() : $buttons.first();
    const isDisabled = $next.prop('disabled') === true || $next.is(':disabled');

    if (isDisabled) {
      throw new Error(`Reporter "${targetReporterName}" not found. No more entries.`);
    }

    cy.wrap($next).click({ force: $visible.length === 0 });
  });

  return cy
    .get('[data-test="qa-current-reporter-label"]')
    .invoke('text')
    .should('not.equal', currentLabel)
    .then(() => undefined) as unknown as Cypress.Chainable<void>;
}

/**
 * This function is used inside makeJudgementDecision to recursively scan QA report entries in the Judge modal until
 * the target reporter label is found.
 *
 * Failure behaviour: should throw if no further QA entry exists (next button disabled) and target was not found.
 *
 * The function reads the current reporter label and:
 * - clicks `accept-report-button` when the label matches `targetReporterName`
 * - otherwise advances to the next report entry via `goToNextReportAndRecurse(...)`
 *
 * @param targetReporterName Reporter label to find and accept in the QA report sequence.
 * @throws {Error} Propagates an error if no further report entry is available before the target is found.
 */
export function navigateToQaReport(targetReporterName: string): Cypress.Chainable<JQuery<HTMLElement>> {
  return cy
    .get('[data-test="qa-current-reporter-label"]')
    .invoke('text')
    .then((txt) => {
      const current = txt.trim();

      if (current === targetReporterName) {
        return cy.get('[data-test="accept-report-button"]').scrollIntoView();
      } else {
        return goToNextReportAndRecurse(targetReporterName, current).then(() => {
          return navigateToQaReport(targetReporterName);
        });
      }
    });
}

/**
 * Helper that executes the UI interaction needed to apply a judgement in the open Judge modal.
 *
 * @param judgement Judgement configuration defining source selection and optional custom value.
 */
export function makeJudgementDecision(judgement: QaJudgement): void {
  if (judgement.customValue != null) {
    cy.get('[data-test="custom-value-field"]').click();
    cy.get('[data-test="custom-value-field"]').clear();
    cy.get('[data-test="custom-value-field"]').type(judgement.customValue);
  }

  if (judgement.acceptedSource === AcceptedDataPointSource.Original) {
    cy.get('[data-test="accept-original-button"]').click();
    return;
  }

  if (judgement.acceptedSource === AcceptedDataPointSource.Custom) {
    cy.get('[data-test="accept-custom-button"]').click();
    return;
  }

  if (judgement.acceptedSource === AcceptedDataPointSource.Qa) {
    const target = judgement.reporterUserNameOfAcceptedQaReport;

    if (!target) {
      throw new Error('Qa judgement requires reporterUserNameOfAcceptedQaReport for modal matching');
    }

    navigateToQaReport(target).click();
  }
}
