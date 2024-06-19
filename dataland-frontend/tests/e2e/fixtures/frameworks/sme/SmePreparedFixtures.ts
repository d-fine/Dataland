import { type FixtureData } from "@sharedUtils/Fixtures";
import { type SmeData } from "@clients/backend";
import { generateSmeFixtures } from "@e2e/fixtures/frameworks/sme/SmeDataFixtures";
import { SmeGenerator } from "@e2e/fixtures/frameworks/sme/SmeGenerator";

/**
 * Generates one SME prepared fixture dataset by generating a random SME dataset and afterwards manipulating some fields
 * via a manipulator-function to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateSmePreparedFixtures(): Array<FixtureData<SmeData>> {
  const preparedFixtures = [];
  preparedFixtures.push(manipulateFixtureToIdentifyItAsNoNullFields(generateSmeFixtures(1, 0)[0]));
  return preparedFixtures;
}

/**
 * Sets the company name to a specific value to be able to pick this dataset from the prepared fixtures.
 * Furthermore, one enum field needs to be sorted, so the object-comparison in the blanket test does not fail due to a
 * different order, and also the sectors shall have some nace codes.
 * @param input Fixture data to be manipulated
 * @returns the manipulated fixture data
 */
function manipulateFixtureToIdentifyItAsNoNullFields(input: FixtureData<SmeData>): FixtureData<SmeData> {
  const smeGeneratorNoUndefined = new SmeGenerator(0);
  input.companyInformation.companyName = "Sme-dataset-with-no-null-fields";
  input.companyInformation.sector = "randomSector";
  input.t.basic!.basisForPreparation!.subsidiary = smeGeneratorNoUndefined.randomArray(
    () => smeGeneratorNoUndefined.generateSmeSubsidiary(),
    1,
    3,
  );
  input.t.basic!.pollutionOfAirWaterSoil!.pollutionEmission = smeGeneratorNoUndefined.randomArray(
    () => smeGeneratorNoUndefined.generateSmePollutionEmission(),
    1,
    3,
  );
  input.t.basic!.resourceUseCircularEconomyAndWasteManagement!.wasteClassification =
    smeGeneratorNoUndefined.randomArray(
      () => smeGeneratorNoUndefined.generateRandomSmeWasteClassificationObject(),
      1,
      3,
    );
  input.t.basic!.biodiversity!.sitesAndAreas = smeGeneratorNoUndefined.randomArray(
    () => smeGeneratorNoUndefined.generateSmeSiteAndArea(),
    1,
    3,
  );
  input.t.basic!.workforceGeneralCharacteristics!.employeesPerCountry = smeGeneratorNoUndefined.randomArray(
    () => smeGeneratorNoUndefined.generateSmeEmployeesPerCountry(),
    1,
    3,
  );
  return input;
}
