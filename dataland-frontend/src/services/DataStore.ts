import {humanizeString} from "@/utils/stringHumanizer"

export class DataStore {
    axiosFunction: any
    rawSchema: any

    constructor(axiosFunction: any, rawSchema?: any) {
        this.axiosFunction = axiosFunction
        this.rawSchema = rawSchema
    }

    getSchema(): object {
        if (this.rawSchema) {
            return this.processRawSchema()
        } else {
            return this.getSchemaFromFunction()
        }
    }

    private getSchemaFromFunction(): object {
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

    private processRawSchema(): object {
        const propertiesSchema = this.rawSchema.properties
        const schema = []
        for (const index in propertiesSchema) {
            const validation = this.rawSchema.required.includes(index) ? "required" : ""
            if ("enum" in propertiesSchema[index]) {
                const enumProperties = propertiesSchema[index].enum
                if (enumProperties.length > 2) {
                    /* create a select form */
                    schema.push({
                            $formkit: 'select',
                            label: humanizeString(index),
                            placeholder: "Please Choose",
                            name: index,
                            validation: validation,
                            options: enumProperties
                        }
                    )
                } else {
                    /* create a radio form */
                    schema.push({
                            $formkit: 'radio',
                            label: humanizeString(index),
                            name: index,
                            validation: validation,
                            classes: {
                                outer: {'formkit-outer': false},
                                inner: {'formkit-inner': false},
                                input: {'formkit-input': false}
                            },
                            options: enumProperties
                        }
                    )
                }
            } else {
                /* create a text form */
                schema.push({
                        $formkit: 'text',
                        label: humanizeString(index),
                        placeholder: humanizeString(index),
                        name: index,
                        validation: validation,
                    }
                )
            }
        }
        return schema
    }

    perform(...args: any): any {
        try {
            return this.axiosFunction(...args, {baseURL: process.env.VUE_APP_API_URL})
        } catch (error) {
            console.error(error)
        }
    }
}
