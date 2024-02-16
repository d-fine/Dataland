import { Generator } from "@e2e/utils/FakeFixtureUtils";
import {LksgProduct} from "@clients/backend/org/dataland/datalandfrontend/openApiClient/backend/model/lksg-product";

export class LksgGenerator extends Generator {
    generateLksgProduct(): LksgProduct {
        return {
           name: this.guaranteedShortString(),
            productionSteps: this.randomArray(() => this.guaranteedShortString(), 0, 5),
            relatedCorporateSupplyChain: this.randomShortString()
        }
    }
}


