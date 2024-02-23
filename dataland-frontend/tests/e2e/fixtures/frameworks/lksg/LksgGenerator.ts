import { Generator } from "@e2e/utils/FakeFixtureUtils";
import { type LksgProduct } from "@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model/lksg-product";
import { type LksgProcurementCategory, type LksgProductionSite } from "@clients/backend";
import { ProcurementCategoryType } from "@/api-models/ProcurementCategoryType";
import { generateAddress } from "@e2e/fixtures/common/AddressFixtures";
import { faker } from "@faker-js/faker";
import { generateNaceCodes } from "@e2e/fixtures/common/NaceCodeFixtures";
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

  generateProcurementCategories(localNullProbability = this.nullProbability): { [p: string]: LksgProcurementCategory } {
    const categoryTypes = Object.values(ProcurementCategoryType).filter(
      () => faker.number.float() > localNullProbability,
    );
    const lksgProcurementCategoriesMap = new Map<ProcurementCategoryType, LksgProcurementCategory>();
    categoryTypes.forEach((categoryType) =>
      lksgProcurementCategoriesMap.set(categoryType, this.generateLkSGProcurementCategory()),
    );
    return Object.fromEntries(lksgProcurementCategoriesMap);
  }
  /**
   * Generates a random production site
   * @param localNullProbability the probability (as number between 0 and 1) for "null" values in optional fields
   * @returns a random production site
   */
  generateLksgProductionSite(localNullProbability = this.nullProbability): LksgProductionSite {
    return {
      nameOfProductionSite: this.valueOrNull(faker.company.name()),
      addressOfProductionSite: generateAddress(localNullProbability),
      listOfGoodsOrServices: this.valueOrNull(this.guaranteedListOfGoodsOrServices()),
    };
  }
  /**
   * Generates a random array of goods or services
   * @returns random array of goods or services
   */
  guaranteedListOfGoodsOrServices(): string[] {
    return this.guaranteedArray(() => faker.commerce.productName(), 1);
  }
}
