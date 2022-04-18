import {ApiWrapper} from "@/services/ApiWrapper"

function dummyFunction(number1: number, number2: number, number3: number): number {
    return number1 * number2 + number3
}

describe("ApiWrapper", () => {

    it("checks if the function is called properly", () => {
        const aiWrapper = new ApiWrapper(dummyFunction)
        const actualResults = aiWrapper.perform(3, 7, 2)
        const expectedResults = dummyFunction(3, 7, 2)
        expect(actualResults).toEqual(expectedResults)
    })
})