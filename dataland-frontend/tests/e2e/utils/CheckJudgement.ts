import type { Interception } from 'cypress/types/net-stubbing';
import { AcceptedDataPointSource } from '@clients/qaservice';
import type { QaJudgement } from '@e2e/utils/CheckJudgementJson.ts';
import { recurse } from 'cypress-recurse';

/**
 * Selects the given data point type in the judge modal's "Next datapoint" dropdown.
 *
 * @param dataPointTypeId The data point type ID (e.g. DATA_POINT_TYPES.numberOfEmployees).
 */
export function selectNextDataPointToJudge(dataPointTypeId: string): void {
  cy.get('[data-test="next-datapoint-section"]').within(() => {
    cy.get('[data-test="next-datapoint-select"]').click();
  });

  const optionSelector = `[data-test="next-datapoint-option-${dataPointTypeId}"]`;

  cy.get(optionSelector).scrollIntoView();
  cy.get(optionSelector).click();
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
export function checkPatchDataPointsCalledCorrectly(interception: Interception, judgement: QaJudgement): void {
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
    expect(body.customDataPoint ?? null, 'customDataPoint in request body').to.eq(null);
  } else {
    expect(body.customDataPoint, 'customDataPoint in request body').to.not.be.undefined;
    expect(body.customDataPoint, 'customDataPoint in request body').to.not.be.null;
  }
}

/**
 * Scans QA report entries in the Judge modal until the target reporter label is found.
 *
 * On each iteration the function reads the current reporter label:
 * - if it matches `targetReporterName`, the loop stops and the function returns
 * - if the next button is disabled, an error is thrown
 * - otherwise the "next" button is clicked and the function waits for the label to change.
 *
 * @param targetReporterName Reporter label to find in the QA report sequence.
 * @throws {Error} When no further report entry is available before the target is found.
 */
export function navigateToQaReport(targetReporterName: string): Cypress.Chainable<JQuery<HTMLElement>> {
  return recurse(
    () => cy.get('[data-test="qa-current-reporter-label"]').invoke('text'),
    (label) => label.trim() === targetReporterName,
    {
      post({ value: currentLabel }) {
        cy.get('[data-test="corrected-datapoint-section"] [data-test="qa-next-button"]').then(($next) => {
          const isDisabled = $next.prop('disabled') === true || $next.is(':disabled');
          if (isDisabled) throw new Error(`Reporter "${targetReporterName}" not found. No more entries.`);
          cy.wrap($next).scrollIntoView();
          cy.wrap($next).click();
          cy.get('[data-test="qa-current-reporter-label"]').invoke('text').should('not.equal', currentLabel.trim());
        });
      },
    }
  ).then(() => cy.get('[data-test="accept-report-button"]').scrollIntoView());
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
