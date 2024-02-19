import { Generator } from "@e2e/utils/FakeFixtureUtils";
import {LksgProduct} from "@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model/lksg-product";
import { LksgProcurementCategory } from "@clients/backend"
import { ProcurementCategoryType } from "@/api-models/ProcurementCategoryType";
export class LksgGenerator extends Generator {
    generateLksgProduct(): LksgProduct {
        return {
           name: this.guaranteedShortString(),
            productionSteps: this.randomArray(() => this.guaranteedShortString(), 0, 5),
            relatedCorporateSupplyChain: this.randomShortString()
        }
    }

    generateCustomRandomMap(numEntries: number): { [p: string]: any } {
        let map = new Map<string, number>
        const randomStringArray = this.guaranteedArray(() => this.guaranteedShortString())
        randomStringArray.forEach((stringEntry) => map.set(stringEntry, this.guaranteedInt()))
        return Object.fromEntries(map)
    }

    generateLkSGProcurementCategory(): LksgProcurementCategory {
        return {
            procuredProductTypesAndServicesNaceCodes: this.guaranteedArray(() => this.guaranteedShortString(), 0, 10),
            numberOfSuppliersPerCountryCode: this.generateCustomRandomMap(this.guaranteedInt(0, 10)),
            shareOfTotalProcurementInPercent: this.randomInt(0,100)
        }
    }

    generateProcurementCategories(localNullProbability = this.nullProbability): { [p: string]: LksgProcurementCategory } {
        const categoryTypes = Object.values(ProcurementCategoryType).filter(
            ()=> Math.random() < localNullProbability);
        const lksgProcurementCategoriesMap =
            new Map<ProcurementCategoryType, LksgProcurementCategory>
        categoryTypes.forEach((categoryType) => lksgProcurementCategoriesMap.set(categoryType,
            this.generateLkSGProcurementCategory()))
        return Object.fromEntries(lksgProcurementCategoriesMap)

}
}


