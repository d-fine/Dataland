import {Data} from "@/model/Data.js";
import MockAdapter from "axios-mock-adapter";
import axios from "axios";

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
        expect(data.id).toBeNull();
        expect(data.name).toBeNull();
        expect(data.filteredResult).toBeNull();
        expect(data.allResult).toBeNull();
    })

    it("checks getFilteredResult", () => {
        data.getFilteredResult("dummyResults")
        expect(data.id).toBeNull();
        expect(data.name).toBeNull();
        expect(data.filteredResult).not.toBeNull();
        expect(data.allResult).toBeNull();
    })

    it("checks getAllResult", () => {
        data.getAllResult("dummyResults")
        expect(data.id).toBeNull();
        expect(data.name).toBeNull();
        expect(data.filteredResult).toBeNull();
        expect(data.allResult).not.toBeNull();
    })

})