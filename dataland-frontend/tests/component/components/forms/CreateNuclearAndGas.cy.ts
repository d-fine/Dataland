import CreateNuclearAndGasDataset from '@/components/forms/CreateNuclearAndGasDataset.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { TEST_PDF_FILE_BASEPATH, TEST_PDF_FILE_NAME } from '@sharedUtils/ConstantsForPdfs';
import {
  CompanyAssociatedDataNuclearAndGasData
} from '@clients/backend';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import DataPointFormWithToggle from '@/components/forms/parts/kpiSelection/DataPointFormWithToggle.vue';
import { UploadReports } from '@sharedUtils/components/UploadReports';
import { selectItemFromDropdownByIndex, selectItemFromDropdownByValue } from '@sharedUtils/Dropdown';
import { getFilledKpis } from '@/utils/DataPoint';
import { getMountingFunction } from '@ct/testUtils/Mount';
import { PAGE_NUMBER_VALIDATION_ERROR_MESSAGE } from '@/utils/ValidationUtils';

describe('Component tests for the Nuclear and Gas that test dependent fields', () => {
  const uploadReports = new UploadReports('referencedReports');



  /**
   * this method fills and checks the general section
   * @param reports the name of the reports that are uploaded
   */
  function fillAndValidateGeneralSection(reports: string[]): void {
    cy.get('div[data-test="nuclearEnergyRelatedActivitiesSection426"] input[type="checkbox"][value="Yes"]').check();
    cy.get('div[data-test="nuclearEnergyRelatedActivitiesSection427"] input[type="checkbox"][value="Yes"]').check();
    cy.get('div[data-test="nuclearEnergyRelatedActivitiesSection428"] input[type="checkbox"][value="Yes"]').check();
    cy.get('div[data-test="fossilGasRelatedActivitiesSection429"] input[type="checkbox"][value="Yes"]').check();
    cy.get('div[data-test="fossilGasRelatedActivitiesSection430"] input[type="checkbox"][value="Yes"]').check();
    cy.get('div[data-test="fossilGasRelatedActivitiesSection430"] input[type="checkbox"][value="Yes"]').check();

  }


  /**
   * this method fills and checks the Taxonomy-aligned (denominator) section
   * @param reports the name of the reports that are uploaded
   */
  function fillAndValidateOtherSections(reports: string[]): void {

    const sections: { [key: string]: string } = {
      'TaxonomyAlignedRevenueDenominator': 'Denominator',
      'TaxonomyAlignedCapexDenominator': 'Denominator',
      'TaxonomyAlignedRevenueNumerator': 'Numerator',
      'TaxonomyAlignedCapexNumerator': 'Numerator'
    };
    const subsections = ['NAndG426', 'NAndG427', 'NAndG428', 'NAndG429','NAndG430','NAndG431','OtherActivities',''];

    Object.keys(sections).forEach(key =>{
      cy.get(`div[data-test="nuclearAndGas${key}"] div[data-test="toggleDataPointWrapper"] div[data-test="dataPointToggleButton"]`).should('exist')
          .click();
      subsections.forEach(subsection => {
        cy.get(`div[data-test="nuclearAndGas${key}"] div[data-test="taxonomyAlignedShare${sections[key]}${subsection}"] input[name="mitigationAndAdaptation"]`).clear().type('12');
        cy.get(`div[data-test="nuclearAndGas${key}"] div[data-test="taxonomyAlignedShare${sections[key]}${subsection}"] input[name="mitigation"]`).clear().type('5');
        cy.get(`div[data-test="nuclearAndGas${key}"] div[data-test="taxonomyAlignedShare${sections[key]}${subsection}"] input[name="adaptation"]`).clear().type('8');
      })
    });
  }

  /**
   * This method returns a mocked dataset for nuclear and gas with some fields filled.
   * @returns the dataset
   */
  function createMockCompanyAssociatedDataNuclearAndGas(): CompanyAssociatedDataNuclearAndGasData {
    return {
      companyId: 'abc',
      reportingPeriod: '2020',
      data: {
        general: {
          general: {
            nuclearEnergyRelatedActivitiesSection426: {
              value: 'Yes'
            },
            nuclearEnergyRelatedActivitiesSection427: {
              value: 'Yes'
            },
            nuclearEnergyRelatedActivitiesSection428: {
              value: 'Yes'
            },
            fossilGasRelatedActivitiesSection429: {
              value: 'Yes'
            },
            fossilGasRelatedActivitiesSection430: {
              value: 'Yes'
            },
            fossilGasRelatedActivitiesSection431: {
              value: 'Yes'
            },
          },
          taxonomyAlignedDenominator: {
            nuclearAndGasTaxonomyAlignedRevenueDenominator: {
              value: {
                taxonomyAlignedShareDenominatorNAndG426: {
                  mitigationAndAdaptation: 12,
                  mitigation: 5,
                  adaptation: 8
                },
                taxonomyAlignedShareDenominatorNAndG427: {
                  mitigationAndAdaptation: 12,
                  mitigation: 5,
                  adaptation: 8
                },
                taxonomyAlignedShareDenominatorNAndG428: {
                  mitigationAndAdaptation: 12,
                  mitigation: 5,
                  adaptation: 8
                },
                taxonomyAlignedShareDenominatorNAndG429: {
                  mitigationAndAdaptation: 12,
                  mitigation: 5,
                  adaptation: 8
                },
                taxonomyAlignedShareDenominatorNAndG430: {
                  mitigationAndAdaptation: 12,
                  mitigation: 5,
                  adaptation: 8
                },
                taxonomyAlignedShareDenominatorNAndG431: {
                  mitigationAndAdaptation: 12,
                  mitigation: 5,
                  adaptation: 8
                },
                taxonomyAlignedShareDenominatorOtherActivities: {
                  mitigationAndAdaptation: 12,
                  mitigation: 5,
                  adaptation: 8
                },
                taxonomyAlignedShareDenominator: {
                  mitigationAndAdaptation: 12,
                  mitigation: 5,
                  adaptation: 8
                },
              }
            },
            nuclearAndGasTaxonomyAlignedCapexDenominator: {
              value: {
                taxonomyAlignedShareDenominatorNAndG426: {
                  mitigationAndAdaptation: 12,
                  mitigation: 5,
                  adaptation: 8
                },
                taxonomyAlignedShareDenominatorNAndG427: {
                  mitigationAndAdaptation: 12,
                  mitigation: 5,
                  adaptation: 8
                },
                taxonomyAlignedShareDenominatorNAndG428: {
                  mitigationAndAdaptation: 12,
                  mitigation: 5,
                  adaptation: 8
                },
                taxonomyAlignedShareDenominatorNAndG429: {
                  mitigationAndAdaptation: 12,
                  mitigation: 5,
                  adaptation: 8
                },
                taxonomyAlignedShareDenominatorNAndG430: {
                  mitigationAndAdaptation: 12,
                  mitigation: 5,
                  adaptation: 8
                },
                taxonomyAlignedShareDenominatorNAndG431: {
                  mitigationAndAdaptation: 12,
                  mitigation: 5,
                  adaptation: 8
                },
                taxonomyAlignedShareDenominatorOtherActivities: {
                  mitigationAndAdaptation: 12,
                  mitigation: 5,
                  adaptation: 8
                },
                taxonomyAlignedShareDenominator: {
                  mitigationAndAdaptation: 12,
                  mitigation: 5,
                  adaptation: 8
                },
              },
            },
          },
        },
      },
    };
  }

  const companyAssociatedDataNuclearAndGas = createMockCompanyAssociatedDataNuclearAndGas();


  it('Open upload page, fill out and validate the upload form, except for new activities', () => {
    cy.stub(DataPointFormWithToggle);
    getMountingFunction({
      keycloak: minimalKeycloakMock(),
      // Ignored, as overwriting the data function is not safe anyway
      // @ts-ignore
    })(CreateNuclearAndGasDataset, {
      data() {
        return {
          //companyAssociatedNuclearAndGasData: companyAssociatedDataNuclearAndGas,
          //listOfFilledKpis: getFilledKpis(companyAssociatedDataNuclearAndGas),
        };
      },
    }).then(() => {
      //uploadReports.selectFile(TEST_PDF_FILE_NAME);
      fillAndValidateGeneralSection([TEST_PDF_FILE_NAME]);
      fillAndValidateOtherSections([TEST_PDF_FILE_NAME]);
    });
  });
});
