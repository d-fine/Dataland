import {Data} from "@/model/Data.js"

describe("DataTest", () => {
    const data = new Data()

    beforeAll(() => {
        data.code = "dummy"
        data.name = "dummy"
        data.result = "dummy"
    })

    it("checks clearAll", () => {
        data.clearAll()
        expect(data.result).toBeNull()
        expect(data.code).toBeNull()
        expect(data.name).toBeNull()
    })

    it("checks Result", () => {
        data.getResult("dummyResults")
        expect(data.result).toMatch("dummyResults")
    })

})