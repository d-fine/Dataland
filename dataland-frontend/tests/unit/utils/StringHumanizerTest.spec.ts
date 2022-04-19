import {StringHumanizer} from "@/utils/StringHumanizer"

describe("Test StringHumanizer", () => {
    const stringHumanizer = new StringHumanizer()
    it("verifies if the implementation works correctly", () => {
        expect(stringHumanizer.humanize("companyFullName")).toEqual("Company Full Name")
        expect(stringHumanizer.humanize("ScaleHdax")).toEqual("ScaleHDAX")
        expect(stringHumanizer.humanize("Dax50Esg")).toEqual("DAX 50 ESG")
        expect(stringHumanizer.humanize("PrimeStandards")).toEqual("Prime Standards")
    })

})