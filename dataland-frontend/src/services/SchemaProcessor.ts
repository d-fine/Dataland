export default class SchemaProcessor {
    axiosFunction: any;
    rawSchema: any;

    constructor(axiosFunction: any, rawSchema?: any) {
        this.axiosFunction = axiosFunction
        this.rawSchema = rawSchema
    }

    getSchemaFromFunction(): Object {
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

    getSchemaFromJson(): Object {
        return {
            $formkit: 'text',
            for: ['item', 'key', this.rawSchema.properties],
            label: "$key",
            placeholder: "$key",
            name: "$key"
        };
    }

    getSchema(): Object {
        if (this.rawSchema) {
            return this.getSchemaFromJson()
        } else {
            return this.getSchemaFromFunction()
        }
    }

    perform(...args: any[]): any {
        this.axiosFunction(...args)
    }
}
