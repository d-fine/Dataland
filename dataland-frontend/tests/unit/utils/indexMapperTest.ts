import {stockIndexObject} from "@/utils/indexMapper";
import apiSpecs from "@/../build/clients/backend/backendOpenApi.json";
describe("stockIndex Object ", () => {
    it("verifies that the parsed indices are correct", () => {
        const mappedIndices = stockIndexObject()
        expect(Object.keys(mappedIndices)).toEqual(apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum)
    })

})