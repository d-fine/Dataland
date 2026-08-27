import CreateEuTaxonomyFinancials from '@/components/forms/CreateEuTaxonomyFinancials.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { getMountingFunction } from '@ct/testUtils/Mount';
import { getFilledKpis, restoreInferableDocumentFields } from '@/utils/DataPoint';
import { eutaxonomyFinancialsDataModel } from '@/frameworks/eutaxonomy-financials/UploadConfig';
import { type CompanyAssociatedDataEutaxonomyFinancialsData, type CompanyReport } from '@clients/backend';

const testFileReference = 'bbebf6077b4ab868fd3e5f83ac70c864fc301c9ab9b3e1a53f52ac8a31b97ff7';
const testFileName = 'TestReport';

/**
 * Builds a value for a single field of the eu taxonomy financials data model based on its component type, so that
 * every possible kpi in the model ends up with a non-null value (mirroring a "no null fields" dataset). Note that
 * the dataSource does not contain a fileName/publicationDate, mirroring the backend, which no longer persists
 * these "inferable" fields on individual data points.
 * @param field the field configuration from the upload config
 * @returns a value object for the given field, or undefined if the field's component is not a plain kpi field
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function buildValueForField(field: any): unknown {
  const dataSource = {
    fileName: null,
    fileReference: testFileReference,
    page: '1',
    publicationDate: null,
  };
  const base = {
    quality: 'Estimated',
    comment: 'test',
    dataSource: dataSource,
  };
  switch (field.component) {
    case 'CurrencyExtendedDataPointFormField':
      return { ...base, value: 1000, currency: 'EUR' };
    case 'PercentageExtendedDataPointFormField':
      return { ...base, value: 15 };
    case 'BigDecimalExtendedDataPointFormField':
      return { ...base, value: 15 };
    case 'DateExtendedDataPointFormField':
      return { ...base, value: '2023-09-11' };
    case 'RadioButtonsExtendedDataPointFormField':
       
      return { ...base, value: field.options?.[0]?.value ?? 'Yes' };
    case 'YesNoExtendedDataPointFormField':
      return { ...base, value: 'Yes' };
    default:
      return undefined;
  }
}

/**
 * Builds a fully populated (no null kpis) CompanyAssociatedDataEutaxonomyFinancialsData object by walking the
 * eu taxonomy financials data model and generating a value for every leaf field. This mirrors a real "no null
 * fields" dataset as used by the corresponding DataIntegrity e2e test.
 * @returns the mock dataset
 */
function createFullyPopulatedEuTaxonomyFinancialsData(): CompanyAssociatedDataEutaxonomyFinancialsData {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const data: any = {};
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  for (const category of eutaxonomyFinancialsDataModel as any[]) {
    data[category.name] = data[category.name] ?? {};
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    for (const subcategory of category.subcategories as any[]) {
      data[category.name][subcategory.name] = data[category.name][subcategory.name] ?? {};
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      for (const field of subcategory.fields as any[]) {
        if (field.component === 'UploadReports') continue;
        if (field.component === 'AssuranceFormField') {
          data[category.name][subcategory.name][field.name] = {
            value: 'None',
            provider: 'Assurance Provider',
            dataSource: {
              fileName: null,
              fileReference: testFileReference,
              page: '1',
              publicationDate: null,
            },
          };
          continue;
        }
        const value = buildValueForField(field);
        if (value !== undefined) {
          data[category.name][subcategory.name][field.name] = value;
        }
      }
    }
  }
  data.general.general.referencedReports = {
    [testFileName]: {
      fileReference: testFileReference,
      publicationDate: '2023-07-12',
    },
  };
  return {
    companyId: 'company-id-does-not-matter-in-this-test',
    reportingPeriod: '2023',
    data,
  } as unknown as CompanyAssociatedDataEutaxonomyFinancialsData;
}

describe('Component test for the Eu Taxonomy Financials edit-mode prefill', () => {
  it('Mounts the edit form with a fully populated dataset without throwing a "Maximum recursive updates" error', () => {
    const companyAssociatedData = createFullyPopulatedEuTaxonomyFinancialsData();
    const referencedReports = companyAssociatedData.data.general?.general?.referencedReports ?? {};
    const fileReferenceToReport = new Map<string, { fileName: string; publicationDate?: string | null }>(
      Object.entries(referencedReports as Record<string, CompanyReport>).map(([fileName, report]) => [
        report.fileReference,
        { fileName, publicationDate: report.publicationDate },
      ])
    );
    // This mirrors the backfilling of inferable document fields performed in loadEuTaxonomyFinancialsData().
    companyAssociatedData.data = restoreInferableDocumentFields(companyAssociatedData.data, fileReferenceToReport);

    getMountingFunction({
      keycloak: minimalKeycloakMock(),
      // @ts-ignore -- overwriting the data function is used here to simulate a prefilled edit-mode form
    })(CreateEuTaxonomyFinancials, {
      props: {
        companyID: 'company-id-does-not-matter-in-this-test',
      },
      data() {
        // The mock data is deep-cloned here so that `referencedReportsForPrefill` and
        // `companyAssociatedEuTaxonomyFinancialsData` do not alias the same nested object (which they would in
        // production too, since `CreateEuTaxonomyFinancials.vue` builds its live form model via `objectDropNull`,
        // a deep clone of the loaded data). Without this clone, FormKit writing a newly selected report's
        // fileReference/publicationDate into the shared `referencedReports` object would also mutate
        // `referencedReportsForPrefill`, causing the new report to non-deterministically be reclassified as
        // "already uploaded" by `UploadReports.vue`'s `prefillAlreadyUploadedReports`.
        const clonedData = structuredClone(companyAssociatedData);
        return {
          waitingForData: false,
          editMode: true,
          referencedReportsForPrefill: clonedData.data.general?.general?.referencedReports,
          companyAssociatedEuTaxonomyFinancialsData: clonedData,
          listOfFilledKpis: getFilledKpis(clonedData.data),
        };
      },
    });

    cy.get('[data-test="pageWrapperTitle"]').should('exist');
    cy.get('#general').should('exist');
  });
});
