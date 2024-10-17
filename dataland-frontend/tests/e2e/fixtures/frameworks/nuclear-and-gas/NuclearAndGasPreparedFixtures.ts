import {type FixtureData} from '@sharedUtils/Fixtures';
import {type NuclearAndGasData} from '@clients/backend';
import {generateNuclearAndGasData, generateNuclearAndGasFixtures} from './NuclearAndGasDataFixtures';

/**
 * Generates prepared Nuclear-and-Gas fixtures by generating random Nuclear-and-Gas datasets and
 * afterward manipulating some fields via manipulator-functions to set specific values for those fields.
 * @returns the prepared fixtures
 */
export function generateNuclearAndGasPreparedFixtures(): Array<FixtureData<NuclearAndGasData>> {
    const preparedFixtures = [];
    // Note: Put the code for prepared fixture generation below. This file will not be overwritten automatically

    const manipulatorFunctions: Array<(input: FixtureData<NuclearAndGasData>) => FixtureData<NuclearAndGasData>> = [
        createCompanyWithAllFieldsDefinedAndAName
    ];
    const preparedFixturesBeforeManipulation: FixtureData<NuclearAndGasData>[] = generateNuclearAndGasFixtures(manipulatorFunctions.length);

    for (let i = 0; i < manipulatorFunctions.length; i++) {
        preparedFixtures.push(manipulatorFunctions[i](preparedFixturesBeforeManipulation[i]));
    }

    return preparedFixtures;
}

/**
 * Creates a prepared fixture that has only defined fields and no fields with missing values
 * @param input the base fixture to modify
 * @param companyName the name of the associated company
 * @param reportingPeriod of the dataset
 * @returns the modified fixture
 */
function createDatasetWithAllFieldsDefined(
    input: FixtureData<NuclearAndGasData>,
    companyName: string,
    reportingPeriod: string
): FixtureData<NuclearAndGasData> {
    input.companyInformation.companyName = companyName;
    input.reportingPeriod = reportingPeriod;
    input.t = generateNuclearAndGasData(0);
    return input;
}

/**
 * Creates a prepared fixture that has no fields with missing values and a specific name
 * @param input the base fixture to modify
 * @returns the modified fixture
 */
function createCompanyWithAllFieldsDefinedAndAName(
    input: FixtureData<NuclearAndGasData>
): FixtureData<NuclearAndGasData> {
    return createDatasetWithAllFieldsDefined(input, 'All-fields-defined-for-EU-NuclearAndGas-Framework', '2024');
}
