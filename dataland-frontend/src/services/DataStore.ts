import {humanizeString} from "@/utils/stringHumanizer";

export default class DataStore {
    axiosFunction: any;
    rawSchema: any;

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
            const value = params[index];
            if ("options" == value) {
                params.splice(index, 1)
            }
        }
        for (const k in params) {
            scheme.push({
                    $formkit: 'text',
                    label: humanizeString(params[k]),
                    placeholder: humanizeString(params[k]),
                    name: params[k]
                }
            )
        }
        return scheme
    }

    private _getSchemaFromJson(): Object {
        const scheme = []
        for (const k in this.rawSchema.properties) {
            scheme.push({
                    $formkit: 'text',
                    label: humanizeString(k),
                    placeholder: humanizeString(k),
                    name: k
                }
            )
        }
        return scheme
    }

    perform(...args: any[]): any {
        this.axiosFunction(...args)
    }
}
