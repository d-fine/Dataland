import { type FixtureData } from '@sharedUtils/Fixtures';
import { type AdditionalCompanyInformationData } from '@clients/backend';
import { generateAdditionalCompanyInformationFixtures } from './AdditionalCompanyInformationDataFixtures';
import { getAllFakeFixtureDocumentIds } from '@e2e/utils/DocumentReference.ts';

/**
 * Generates additional-company-information prepared fixtures by generating random additional-company-information datasets and
 * afterwards manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateAdditionalCompanyInformationPreparedFixtures(): Array<
  FixtureData<AdditionalCompanyInformationData>
> {
  const preparedFixtures = [];
  preparedFixtures.push(generateFixturesWithNoNullFields());
  preparedFixtures.push(generateFixtureWithNullishFields());
  return preparedFixtures;
}

/**
 * Generate a prepared Fixture with no null entries
 * @returns the fixture
 */
function generateFixturesWithNoNullFields(): FixtureData<AdditionalCompanyInformationData> {
  const newFixture = generateAdditionalCompanyInformationFixtures(1, 0)[0];
  newFixture.companyInformation.companyName = 'additional-company-information-dataset-with-no-null-fields';
  return newFixture;
}

/**
 * Generate a prepared Fixture with nullish entries like the VOEB dataset that caused the migration to fail
 * @returns the fixture
 */
function generateFixtureWithNullishFields(): FixtureData<AdditionalCompanyInformationData> {
  const newFixture = generateAdditionalCompanyInformationFixtures(1, 0)[0];
  const allDocumentRefs = getAllFakeFixtureDocumentIds()[0];
  newFixture.companyInformation.companyName = 'additional-company-information-dataset-with-nullish-fields';
  newFixture.t = {
    general: {
      general: {
        fiscalYearDeviation: {
          value: 'NoDeviation',
          quality: null,
          comment: '',
          dataSource: {
            page: '58',
            tagName: null,
            fileName: '2024_VOEBS_Jahresbericht_2024_web',
            fileReference: allDocumentRefs[0],
          },
        },
        fiscalYearEnd: null,
        referencedReports: {
          '2024_VOEBS_Jahresbericht_2024_web': {
            fileReference: allDocumentRefs[0],
            fileName: null,
            publicationDate: null,
          },
        },
      },
      financialInformation: {
        equity: null,
        debt: null,
        balanceSheetTotal: null,
        evic: null,
      },
    },
  };
  return newFixture;
}
