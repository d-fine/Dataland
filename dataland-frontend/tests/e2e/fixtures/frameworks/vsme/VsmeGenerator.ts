import { Generator } from "@e2e/utils/FakeFixtureUtils";
import {
  ReleaseMedium,
  WasteClassifications,
  type SmePollutionEmission,
  type SmeSubsidiary,
  type SmeWasteClassificationObject,
  type SmeSiteAndArea,
  type SmeEmployeesPerCountry,
  AreaAdjointness,
} from "@clients/backend";
import { generateAddress } from "@e2e/fixtures/common/AddressFixtures";
import { faker } from "@faker-js/faker";
import { generateFloat } from "@e2e/fixtures/common/NumberFixtures";
import { pickOneElement } from "@e2e/fixtures/FixtureUtils";

export class VsmeGenerator extends Generator {
  /**
   * Generates a random subsidiary
   * @returns a random subsidiary
   */
  generateVsmeSubsidiary(): SmeSubsidiary {
    return {
      nameOfSubsidiary: this.valueOrNull(faker.company.name()),
      addressOfSubsidiary: generateAddress(this.nullProbability),
    };
  }
  /**
   * Generates a random pollution emission
   * @returns a random pollution emission
   */
  generateVsmePollutionEmission(): SmePollutionEmission {
    return {
      pollutionType: this.valueOrNull(faker.science.chemicalElement())?.name,
      emissionInKilograms: this.valueOrNull(generateFloat()),
      releaseMedium: pickOneElement(Object.values(ReleaseMedium)),
    };
  }

  /**
   * Generates a random waste classification object
   * @returns a random waste classification object
   */
  generateRandomVsmeWasteClassificationObject(): SmeWasteClassificationObject {
    return {
      wasteClassification: pickOneElement(Object.values(WasteClassifications)),
      typeOfWaste: this.randomShortString(),
      totalAmountTons: this.randomFloat(0, 1e8),
      wasteRecycleOrReuseTons: this.randomFloat(0, 1e8),
      wasteDisposalTons: this.randomFloat(0, 1e8),
      totalAmountCubicMeters: this.randomFloat(0, 1e2),
      wasteRecycleOrReuseCubicMeters: this.randomFloat(0, 1e2),
      wasteDisposalCubicMeters: this.randomFloat(0, 1e2),
    };
  }
  generateVsmeSiteAndArea(): SmeSiteAndArea {
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
  generateVsmeEmployeesPerCountry(): SmeEmployeesPerCountry {
    return {
      country: faker.location.countryCode(),
      numberOfEmployeesInHeadCount: this.randomInt(0, 1e4),
      numberOfEmployeesInFullTimeEquivalent: this.randomFloat(0, 1e4),
    };
  }
}