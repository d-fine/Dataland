import {indexObject} from "@/utils/indexMapper";
import apiSpecs from "@/../build/clients/backend/backendOpenApi.json";
describe("index Object ", () => {
    it("verifies that the parsed indices are correct", () => {
        const mappedIndices = indexObject()
        expect(Object.keys(mappedIndices)).toEqual(apiSpecs.components.schemas.CompanyInformation.properties["indices"].items.enum)
    })

})