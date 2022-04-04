import {nFormatter} from "@/utils/currencyMagnitude";
describe("nFormatter", () => {
    it("verifies whether the implementation works correctly", () => {
    const testData = [
        { num: 0, digits: 1 },
        { num: 1234, digits: 1 },
        { num: 100000000, digits: 1 },
        { num: 299792458, digits: 2 },
        { num: 3299792458, digits: 2 },
        { num: 3293792458, digits: 2 },
        { num: 759878, digits: 0 },
        { num: 123, digits: 1 },
        { num: 123.456, digits: 1 },
        { num: 123.456, digits: 2 },
        { num: 123.456, digits: 4 }
    ];
        expect(nFormatter(testData[0].num, testData[0].digits)).toEqual("0")
        expect(nFormatter(testData[1].num, testData[1].digits)).toEqual("1.2 k")
        expect(nFormatter(testData[2].num, testData[2].digits)).toEqual("100 m")
        expect(nFormatter(testData[3].num, testData[3].digits)).toEqual("299.79 m")
        expect(nFormatter(testData[4].num, testData[4].digits)).toEqual("3.3 b")
        expect(nFormatter(testData[5].num, testData[5].digits)).toEqual("3.29 b")
    })

})