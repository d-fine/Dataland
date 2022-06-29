import {SchemaGenerator} from "@/services/SchemaGenerator";
import { expect } from '@jest/globals';

describe("SchemaGenerator", () => {

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
        const dataStore = new SchemaGenerator(testSchema)
        expect(Object.keys(dataStore.generate()).length).toEqual(Object.keys(testSchema.properties).length)
    })
})