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

    private getType(param: string): string {
        const propertiesSchema = this.rawSchema.properties
        if ("format" in propertiesSchema[param]) {
            const format = propertiesSchema[param].format
            if (format == "date") {
                return 'date_format:DD.MM.YYYY|date_format:DD/MM/YYYY'
            }
        }
        if ("type" in propertiesSchema[param]) {
            const type = propertiesSchema[param].type
            if (type == "number") {
                return type
            }
        }
        return ""
    }

    private getValidationStatus(param: string): string {
        const required = this.rawSchema.required.includes(param) ? "required" : ""
        const type = this.getType(param)
        return `${required}|${type}`
    }

    private processRawSchema(): object {
        const propertiesSchema = this.rawSchema.properties
        const schema = []
        for (const index in propertiesSchema) {
            const validation = this.getValidationStatus(index)
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
            } else if (this.getType(index).includes("date")) {
                /* create a data form */
                schema.push({
                        $formkit: "date",
                        label: humanizeString(index),
                        name: index,
                        validation: validation,
                    }
                )
            } else {
                {
                    /* create a text form */
                    schema.push({
                            $formkit: "text",
                            label: humanizeString(index),
                            placeholder: humanizeString(index),
                            name: index,
                            validation: validation,
                        }
                    )
                }
            }
        }
        return schema
    }

    perform(...args: any): any {
        return this.axiosFunction(...args, {baseURL: process.env.VUE_APP_BASE_API_URL})
    }
}
