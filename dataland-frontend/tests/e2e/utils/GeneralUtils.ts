import { type DataTypeEnum } from '@clients/backend';
import { type Interception } from 'cypress/types/net-stubbing';

/**
 * Visits the edit page for a framework via UI navigation.
 * @param companyId the id of the company for which to edit a dataset
 * @param dataType the framework
 * @returns a cypress chainable to the interception of the data request on the edit page
 */
export function goToEditFormOfMostRecentDatasetForCompanyAndFramework(
  companyId: string,
  dataType: DataTypeEnum
): Cypress.Chainable<Interception> {
  const getRequestAlias = 'fetchDataForPrefill';
  cy.intercept({
    method: 'GET',
    url: '**/api/data/**',
    times: 2,
  }).as(getRequestAlias);
  cy.visit(`/companies/${companyId}/frameworks/${dataType}`);
  cy.wait(`@${getRequestAlias}`, { timeout: Cypress.env('medium_timeout_in_ms') as number });
  cy.get('[data-test="editDatasetButton"]').click();
  return cy.wait(`@${getRequestAlias}`, { timeout: Cypress.env('medium_timeout_in_ms') as number });
}

/**
 * This method can be used in tests to check if two objects are equal. Equal means in this case, that the field names
 * in both objects match and also the values match.
 * @param objA the first object of the comparison
 * @param objB the second object of the comparison
 * @param path is the path of the current key of object A that is being compared to object B
 * @param ignoreFields optional list of path endings to be ignored in the comparison
 */
export function compareObjectKeysAndValuesDeep(
  objA: Record<string, object>,
  objB: Record<string, object>,
  path = '',
  ignoreFields?: Array<string>
): void {
  const keysA = Object.keys(objA);
  const keysB = Object.keys(objB);

  for (const key of keysA) {
    const newPath = path ? `${path}.${key}` : key;

    if (!keysB.includes(key) && !ignoredValue(key, ignoreFields)) {
      throw new Error(`A field with the key ${newPath} exists in A but not in B`);
    }

    const valueA = objA[key] as Record<string, object>;
    const valueB = objB[key] as Record<string, object>;
    checkIfContentIsIdentical(valueA, valueB, newPath, ignoreFields);
  }

  for (const key of keysB) {
    if (!keysA.includes(key) && !ignoredValue(key, ignoreFields)) {
      throw new Error(`A field with the key ${path}.${key} exists in B but not in A`);
    }
  }
}
/**
 * This method compares if two values are the same, if not it will throw an error
 * @param valueA the first value of the comparison
 * @param valueB the second value of the comparison
 * @param newPath is the path of the current key of value A that is being compared to value B
 * @param ignoreFields optional list of path endings to be ignored in the comparison
 */
function checkIfContentIsIdentical(
  valueA: Record<string, object>,
  valueB: Record<string, object>,
  newPath: string,
  ignoreFields?: Array<string>
): void {
  const throwErrorBecauseOfFieldValue = (fieldPath: string): void => {
    throw new Error(`Field ${fieldPath} is not equal.`);
  };
  if (typeof valueA === 'object' && typeof valueB === 'object') {
    if (valueA === null || valueB === null) {
      if (valueA !== valueB && !ignoredValue(newPath, ignoreFields)) {
        throwErrorBecauseOfFieldValue(newPath);
      }
    } else {
      compareObjectKeysAndValuesDeep(valueA, valueB, newPath);
    }
  } else if (valueA !== valueB && !ignoredValue(newPath, ignoreFields)) {
    throwErrorBecauseOfFieldValue(newPath);
  }
}

/**
 * This method checks if a field should be ignored in the comparison, if no ignoreFields are provided it will return false
 * @param path the path of the field
 * @param ignoreFields optional list of path endings to be ignored in the comparison
 * @returns true if the field should be ignored, false otherwise
 */
function ignoredValue(path: string, ignoreFields?: Array<string>): boolean {
  if (ignoreFields) {
    return ignoreFields.some((field) => path.endsWith("."+field));
  }
  return false
}
