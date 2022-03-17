import {humanizeString} from "@/utils/stringHumanizer"

describe("stringHumanizer", () => {

    it("verifies if the implementation works correctly", () => {
        const camelCaseText = "companyFullName"
        const processedText = humanizeString(camelCaseText)

        expect(processedText).toEqual("Company Full Name")
    })

})