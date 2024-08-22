import { Generator } from '@e2e/utils/FakeFixtureUtils';
import { type LksgProduct } from '@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model/lksg-product';
import {
  type LksgGrievanceAssessmentMechanism,
  type LksgProcurementCategory,
  type LksgProductionSite,
  type LksgRiskOrViolationAssessment,
  RiskPositionType,
} from '@clients/backend';
import { ProcurementCategoryType } from '@/api-models/ProcurementCategoryType';
import { generateAddress } from '@e2e/fixtures/common/AddressFixtures';
import { faker } from '@faker-js/faker';
import { generateNaceCodes } from '@e2e/fixtures/common/NaceCodeFixtures';
import { generateIso2CountryCode } from '@e2e/fixtures/common/CountryFixtures';
import { generateArray, pickSubsetOfElements } from '@e2e/fixtures/FixtureUtils';
export class LksgGenerator extends Generator {
  generateLksgProduct(): LksgProduct {
    return {
      name: this.guaranteedShortString(),
      productionSteps: this.randomArray(() => this.guaranteedShortString(), 0, 5),
      relatedCorporateSupplyChain: this.randomShortString(),
    };
  }

  generateCustomRandomMap(numEntries: number): { [p: string]: number } {
    const map = new Map<string, number>();
    const randomStringArray = this.guaranteedArray(() => this.guaranteedShortString(), 0, numEntries);
    randomStringArray.forEach((stringEntry) => map.set(stringEntry, this.guaranteedInt()));
    return Object.fromEntries(map);
  }

  generateLkSGProcurementCategory(): LksgProcurementCategory {
    if (this.nullProbability == 0)
      return {
        procuredProductTypesAndServicesNaceCodes: generateNaceCodes(),
        numberOfSuppliersPerCountryCode: this.generateCustomRandomMap(this.guaranteedInt(0, 10)),
        shareOfTotalProcurementInPercent: this.guaranteedInt(0, 100),
      };
    else
      return {
        procuredProductTypesAndServicesNaceCodes: this.guaranteedArray(() => this.guaranteedShortString(), 0, 10),
        numberOfSuppliersPerCountryCode: this.generateCustomRandomMap(this.guaranteedInt(0, 10)),
        shareOfTotalProcurementInPercent: this.randomInt(0, 100),
      };
  }

  generateProcurementCategories(): { [p: string]: LksgProcurementCategory } {
    const categoryTypes = Object.values(ProcurementCategoryType).filter(
      () => faker.number.float() > this.nullProbability
    );
    const lksgProcurementCategoriesMap = new Map<ProcurementCategoryType, LksgProcurementCategory>();
    categoryTypes.forEach((categoryType) =>
      lksgProcurementCategoriesMap.set(categoryType, this.generateLkSGProcurementCategory())
    );
    return Object.fromEntries(lksgProcurementCategoriesMap);
  }

  generateSubcontractingCompanies(): { [key: string]: Array<string> } {
    const countryCodes = generateArray(generateIso2CountryCode);
    const lksgSubcontractingCompaniesMap = new Map<string, string[]>();
    countryCodes.forEach((countryCode) => lksgSubcontractingCompaniesMap.set(countryCode, generateNaceCodes()));
    return Object.fromEntries(lksgSubcontractingCompaniesMap);
  }
  /**
   * Generates a random production site
   * @returns a random production site
   */
  generateLksgProductionSite(): LksgProductionSite {
    return {
      nameOfProductionSite: this.valueOrNull(faker.company.name()),
      addressOfProductionSite: generateAddress(this.nullProbability),
      listOfGoodsOrServices: this.valueOrNull(this.guaranteedListOfGoodsOrServices()),
    };
  }

  /**
   * Generates a random Lksg risk assessment or general violation assessment
   * @returns a random generated object
   */
  generateLksgRiskOrViolationAssessment(): LksgRiskOrViolationAssessment {
    return {
      riskPosition: pickSubsetOfElements(Object.values(RiskPositionType), 1, 1)[0],
      measuresTaken: this.guaranteedYesNo(),
      listedMeasures: this.randomShortString(),
    };
  }

  generateLksgGrievanceMechanismAssessment(): LksgGrievanceAssessmentMechanism {
    return {
      riskPositions: pickSubsetOfElements(Object.values(RiskPositionType)),
      specifiedComplaint: this.guaranteedShortString(),
      measuresTaken: this.guaranteedYesNo(),
      listedMeasures: this.randomShortString(),
    };
  }

  /**
   * Generates a random array of goods or services
   * @returns random array of goods or services
   */
  guaranteedListOfGoodsOrServices(): string[] {
    return generateNaceCodes(1);
  }
}
