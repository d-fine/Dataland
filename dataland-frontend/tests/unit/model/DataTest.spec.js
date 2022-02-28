import {Data} from "@/model/Data.js";

describe("DataTest", () => {
    let data = new Data();

    beforeAll(() => {
        data.id = "dummy"
        data.name = "dummy"
        data.result = "dummy"
    });

    it("checks clearAll", () => {
        data.clearAll()
        expect(data.result).toBeNull();
        expect(data.id).toBeNull();
        expect(data.name).toBeNull();
    })

    it("checks Result", () => {
        data.getResult("dummyResults")
        expect(data.result).toMatch("dummyResults");
    })

})