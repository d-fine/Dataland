import {DataStore} from "@/services/DataStore";

function dummyFunction(number1: number, number2: number, number3: number): number {
    return number1 * number2 + number3
}

describe("DataStore", () => {

    it("gets schema using function", () => {
        const dataStore = new DataStore(dummyFunction)
        expect(Object.keys(dataStore.getSchema()).length).toEqual(3)
    })

    it("gets schema using json", () => {
        const contactSchema = {
            "required": [
                "Attestation",
                "Reporting Obligation"
            ],
            "type": "object",
            "properties": {
                "industrialSector": {
                    "type": "string"
                },
                "marketCap": {
                    "type": "number"
                },
                "reportingDateOfMarketCap": {
                    "type": "string",
                    "format": "date"
                },
                "Reporting Obligation": {
                    "type": "string",
                    "enum": [
                        "Yes",
                        "No"
                    ]
                },
                "Attestation": {
                    "type": "string",
                    "enum": [
                        "None",
                        "Some",
                        "Full"
                    ]
                }
            }
        }
        const dataStore = new DataStore(dummyFunction, contactSchema)
        expect(Object.keys(dataStore.getSchema()).length).toEqual(5)
    })

    it("checks if the function is called properly", () => {
        const dataStore = new DataStore(dummyFunction)
        const actualResults = dataStore.perform(3, 7, 2)
        const expectedResults = dummyFunction(3, 7, 2)
        expect(actualResults).toEqual(expectedResults)
    })
})