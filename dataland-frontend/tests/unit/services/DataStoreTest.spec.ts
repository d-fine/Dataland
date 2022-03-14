import DataStore from "@/services/DataStore";
import backend from "@/clients/backend/backendOpenApi.json"

function dummyFunction(number1: number, number2: number): number {
    return number1 * number2
}

describe("DataStore", () => {

    it("gets scheme using function", () => {
        const dataStore = new DataStore(dummyFunction)
        expect(Object.keys(dataStore.getSchema()).length).toEqual(2)
    })

    it("gets scheme using json", () => {
        const contactSchema = backend.components.schemas.ContactInformation
        const dataStore = new DataStore(dummyFunction, contactSchema)
        expect(Object.keys(dataStore.getSchema()).length).toBeGreaterThan(0)
    })

    it("checks if the function is called properly", () => {
        const dataStore = new DataStore(dummyFunction)
        const actualResults = dataStore.perform(3, 7)
        const expectedResults = dummyFunction(3, 7)
        expect(actualResults).toEqual(expectedResults)
    })
})