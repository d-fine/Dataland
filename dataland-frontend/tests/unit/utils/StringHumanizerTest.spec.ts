import {humanize} from "@/utils/StringHumanizer"
import { expect } from '@jest/globals';

describe("Test StringHumanizer", () => {
    it("verifies if the implementation works correctly", () => {
        expect(humanize("companyFullName")).toEqual("Company Full Name")
        expect(humanize("Hdax")).toEqual("HDAX")
        expect(humanize("Dax50Esg")).toEqual("DAX 50 ESG")
        expect(humanize("PrimeStandards")).toEqual("Prime Standards")
    })

})