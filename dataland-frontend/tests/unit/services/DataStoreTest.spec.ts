import {DataStore} from "@/services/DataStore";

function dummyFunction(number1: number, number2: number, number3: number): number {
    return number1 * number2 + number3
}

describe("DataStore", () => {

    it("gets schema using function", () => {
        const dataStore = new DataStore(dummyFunction)
        expect(Object.keys(dataStore.getSchema()).length).toEqual(3)
    })

    it("checks if the schema can be generated automatically", () => {
        const testSchema = {
            "required": [
                "listProp",
                "yesNoProp"
            ],
            "type": "object",
            "properties": {
                "textProp": {
                    "type": "string"
                },
                "numberProp": {
                    "type": "number"
                },
                "dateProp": {
                    "type": "string",
                    "format": "date"
                },
                "yesNoProp": {
                    "type": "string",
                    "enum": [
                        "Yes",
                        "No"
                    ]
                },
                "listProp": {
                    "type": "string",
                    "enum": [
                        "None",
                        "Some",
                        "Full"
                    ]
                }
            }
        }
        const dataStore = new DataStore(dummyFunction, testSchema)
        expect(Object.keys(dataStore.getSchema()).length).toEqual(5)
    })

    it("checks if the function is called properly", () => {
        const dataStore = new DataStore(dummyFunction)
        const actualResults = dataStore.perform(3, 7, 2)
        const expectedResults = dummyFunction(3, 7, 2)
        expect(actualResults).toEqual(expectedResults)
    })
})