import {humanizeString} from "@/utils/stringHumanizer"

describe("stringHumanizer", () => {

    it("verifies if the implementation works correctly", () => {
        expect(humanizeString("companyFullName")).toEqual("Company Full Name")
        expect(humanizeString("DAXIndex")).toEqual("DAX Index")
        expect(humanizeString("DAX_50_ESG")).toEqual("DAX 50 ESG")
    })

})