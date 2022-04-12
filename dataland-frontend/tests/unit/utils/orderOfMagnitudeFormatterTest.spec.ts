import {numberFormatter} from "@/utils/currencyMagnitude";
describe("nFormatter", () => {
    it("verifies whether the implementation works correctly", () => {
        expect(numberFormatter(0, 1)).toEqual("0")
        expect(numberFormatter(1234, 1)).toEqual("1.2 k")
        expect(numberFormatter(100000000, 1)).toEqual("100 m")
        expect(numberFormatter(299792458, 2)).toEqual("299.79 m")
        expect(numberFormatter(3299792458, 2)).toEqual("3.3 b")
        expect(numberFormatter(3293792458, 2)).toEqual("3.29 b")
    })

})