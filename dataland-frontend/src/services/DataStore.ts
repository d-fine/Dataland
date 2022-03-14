import {humanizeString} from "@/utils/stringHumanizer"

export class DataStore {
    axiosFunction: any
    rawSchema: any

    constructor(axiosFunction: any, rawSchema?: any) {
        this.axiosFunction = axiosFunction
        this.rawSchema = rawSchema
    }

    getSchema(): Object {
        if (this.rawSchema) {
            return this._getSchemaFromJson()
        } else {
            return this._getSchemaFromFunction()
        }
    }

    private _getSchemaFromFunction(): Object {
        const getAllParams = require('get-parameter-names')
        const params = getAllParams(this.axiosFunction)
        const schema = []

        for (const index in params) {
            const value = params[index]
            if (value != "options") {
                schema.push({
                        $formkit: 'text',
                        label: humanizeString(value),
                        placeholder: humanizeString(value),
                        name: value,
                        validation: "required"
                    }
                )
            }
        }
        return schema
    }

    private _getSchemaFromJson(): Object {
        const schema = []
        for (const index in this.rawSchema.properties) {
            schema.push({
                    $formkit: 'text',
                    label: humanizeString(index),
                    placeholder: humanizeString(index),
                    name: index,
                    validation: this.rawSchema.required.includes(index) ? "required" : ""
                }
            )
        }
        return schema
    }

    perform(...args: any[]): any {
        return this.axiosFunction(...args)
    }
}
