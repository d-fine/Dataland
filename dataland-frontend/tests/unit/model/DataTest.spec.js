import {Data} from "@/model/Data.js";

describe("DataTest", () => {
    let data = new Data();

    beforeAll(() => {
        data.id = "dummy"
        data.name = "dummy"
        data.filteredResult = "dummy"
        data.allResult = "dummy"
    });

    it("checks clearAll", () => {
        data.clearAll()
        expect(data.filteredResult).toBeNull();
        expect(data.allResult).toBeNull();
    })

    it("checks getFilteredResult", () => {
        data.getFilteredResult("dummyResults")
        expect(data.filteredResult).toMatch("dummyResults");
        expect(data.allResult).toBeNull();
    })

    it("checks getAllResult", () => {
        data.getAllResult("dummyResults")
        expect(data.filteredResult).toBeNull();
        expect(data.allResult).toMatch("dummyResults");
    })

})