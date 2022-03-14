import {humanizeString} from "@/utils/stringHumanizer"

export default class DataStore {
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
        const scheme = []

        for (const index in params) {
            const value = params[index]
            if (value != "options") {
                scheme.push({
                        $formkit: 'text',
                        label: humanizeString(value),
                        placeholder: humanizeString(value),
                        name: value
                    }
                )
            }
        }
        return scheme
    }

    private _getSchemaFromJson(): Object {
        const scheme = []
        for (const index in this.rawSchema.properties) {
            scheme.push({
                    $formkit: 'text',
                    label: humanizeString(index),
                    placeholder: humanizeString(index),
                    name: index
                }
            )
        }
        return scheme
    }

    perform(...args: any[]): any {
        this.axiosFunction(...args)
    }
}
