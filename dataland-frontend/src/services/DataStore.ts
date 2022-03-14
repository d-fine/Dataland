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
        for (const index in params) {
            const value = params[index];
            if ("options" == value) {
                params.splice(index, 1)
            }
        }
        return {
            $formkit: 'text',
            for: ['item', 'key', params],
            label: "$item",
            placeholder: "$item",
            name: "$item"
        }
    }

    private _getSchemaFromJson(): Object {
        return {
            $formkit: 'text',
            for: ['item', 'key', this.rawSchema.properties],
            label: "$key",
            placeholder: "$key",
            name: "$key"
        };
    }

    perform(...args: any[]): any {
        this.axiosFunction(...args)
    }
}
