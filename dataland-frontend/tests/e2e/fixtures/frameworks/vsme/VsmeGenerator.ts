import { Generator } from '@e2e/utils/FakeFixtureUtils';
import {
  ReleaseMedium,
  WasteClassifications,
  type VsmePollutionEmission,
  type VsmeSubsidiary,
  type VsmeWasteClassificationObject,
  type VsmeSiteAndArea,
  type VsmeEmployeesPerCountry,
  AreaAdjointness,
} from '@clients/backend';
import { generateAddress } from '@e2e/fixtures/common/AddressFixtures';
import { faker } from '@faker-js/faker';
import { generateFloat } from '@e2e/fixtures/common/NumberFixtures';
import { pickOneElement } from '@e2e/fixtures/FixtureUtils';

export class VsmeGenerator extends Generator {
  /**
   * Generates a random subsidiary
   * @returns a random subsidiary
   */
  generateVsmeSubsidiary(): VsmeSubsidiary {
    return {
      nameOfSubsidiary: faker.company.name(),
      addressOfSubsidiary: generateAddress(this.nullProbability),
    };
  }
  /**
   * Generates a random pollution emission
   * @returns a random pollution emission
   */
  generateVsmePollutionEmission(): VsmePollutionEmission {
    return {
      pollutionType: faker.science.chemicalElement()?.name,
      emissionInKilograms: this.valueOrNull(generateFloat()),
      releaseMedium: pickOneElement(Object.values(ReleaseMedium)),
    };
  }

  /**
   * Generates a random waste classification object
   * @returns a random waste classification object
   */
  generateRandomVsmeWasteClassificationObject(): VsmeWasteClassificationObject {
    return {
      wasteClassification: pickOneElement(Object.values(WasteClassifications)),
      typeOfWaste: this.guaranteedShortString(),
      totalAmountOfWasteInTonnes: this.randomFloat(0, 1e8),
      wasteRecycleOrReuseInTonnes: this.randomFloat(0, 1e8),
      wasteDisposalInTonnes: this.randomFloat(0, 1e8),
      totalAmountOfWasteInCubicMeters: this.randomFloat(0, 1e2),
      wasteRecycleOrReuseInCubicMeters: this.randomFloat(0, 1e2),
      wasteDisposalInCubicMeters: this.randomFloat(0, 1e2),
    };
  }
  generateVsmeSiteAndArea(): VsmeSiteAndArea {
    return {
      siteName: faker.company.name(),
      areaAddress: generateAddress(this.nullProbability),
      areaInHectare: this.guaranteedFloat(0, 1e5),
      biodiversitySensitiveArea: this.guaranteedParagraphs(),
      siteAddress: generateAddress(this.nullProbability),
      specificationOfAdjointness: pickOneElement(Object.values(AreaAdjointness)),
      siteGeocoordinateLongitudeval: this.randomFloat(0, 1e8),
      siteGeocoordinateLatitude: this.randomFloat(0, 1e8),
      areaGeocoordinateLongitude: this.randomFloat(0, 1e8),
      areaGeocoordinateLatitude: this.randomFloat(0, 1e8),
    };
  }
  generateVsmeEmployeesPerCountry(): VsmeEmployeesPerCountry {
    return {
      country: faker.location.countryCode(),
      numberOfEmployeesInHeadCount: this.randomInt(0, 1e4),
      numberOfEmployeesInFullTimeEquivalent: this.randomFloat(0, 1e4),
    };
  }
}
