import { type FixtureData } from '@sharedUtils/Fixtures';
import { type VsmeData } from '@clients/backend';
import { generateVsmeFixtures } from '@e2e/fixtures/frameworks/vsme/VsmeDataFixtures';
import { VsmeGenerator } from '@e2e/fixtures/frameworks/vsme/VsmeGenerator';

/**
 * Generates one SME prepared fixture dataset by generating a random SME dataset and afterwards manipulating some fields
 * via a manipulator-function to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateVsmePreparedFixtures(): Array<FixtureData<VsmeData>> {
  const preparedFixtures = [];
  preparedFixtures.push(manipulateFixtureToIdentifyItAsNoNullFields(generateVsmeFixtures(1, 0)[0]));
  return preparedFixtures;
}

/**
 * Sets the company name to a specific value to be able to pick this dataset from the prepared fixtures.
 * Furthermore, one enum field needs to be sorted, so the object-comparison in the blanket test does not fail due to a
 * different order, and also the sectors shall have some nace codes.
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureToIdentifyItAsNoNullFields(input: FixtureData<VsmeData>): FixtureData<VsmeData> {
  const smeGeneratorNoUndefined = new VsmeGenerator(0);
  input.companyInformation.companyName = 'Vsme-dataset-with-no-null-fields';
  input.companyInformation.sector = 'randomSector';
  input.t.basic!.basisForPreparation!.subsidiary = smeGeneratorNoUndefined.randomArray(
    () => smeGeneratorNoUndefined.generateVsmeSubsidiary(),
    1,
    3
  );
  input.t.basic!.pollutionOfAirWaterSoil!.pollutionEmission = smeGeneratorNoUndefined.randomArray(
    () => smeGeneratorNoUndefined.generateVsmePollutionEmission(),
    1,
    3
  );
  input.t.basic!.resourceUseCircularEconomyAndWasteManagement!.wasteClassification =
    smeGeneratorNoUndefined.randomArray(
      () => smeGeneratorNoUndefined.generateRandomVsmeWasteClassificationObject(),
      1,
      3
    );
  input.t.basic!.biodiversity!.sitesAndAreas = smeGeneratorNoUndefined.randomArray(
    () => smeGeneratorNoUndefined.generateVsmeSiteAndArea(),
    1,
    3
  );
  input.t.basic!.workforceGeneralCharacteristics!.employeesPerCountry = smeGeneratorNoUndefined.randomArray(
    () => smeGeneratorNoUndefined.generateVsmeEmployeesPerCountry(),
    1,
    3
  );
  return input;
}
