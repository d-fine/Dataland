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
            return propertiesSchema[param].type
        }
        return ""
    }

    private processEnum(rawEnumProperties: any): any {
        const enumProperties: any = {}
        for (const enumItem of rawEnumProperties) {
            enumProperties[enumItem] = humanizeString(enumItem)
        }
        return enumProperties
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
                const enumProperties = this.processEnum(propertiesSchema[index].enum)
                console.log(enumProperties)
                if (Object.keys(enumProperties).length > 2) {
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
                                input: {'formkit-input': false, 'p-radiobutton:': true}
                            },
                            options: enumProperties
                        }
                    )
                }
            } else if (this.getType(index).includes("date")) {
                /* create a date form */
                schema.push({
                        $formkit: "date",
                        label: humanizeString(index),
                        name: index,
                        validation: validation,
                        classes: {
                            inner: {'formkit-inner': false, 'p-inputwrapper': true},
                            input: {'formkit-input': false, 'p-inputtext': true}
                        }
                    }
                )
            } else if (this.getType(index) == "array") {
                /* create a checkbox form */
                if ("enum" in propertiesSchema[index].items) {
                    const enumProperties = this.processEnum(propertiesSchema[index].items.enum)
                    schema.push({
                            $formkit: "checkbox",
                            label: humanizeString(index),
                            placeholder: humanizeString(index),
                            name: index,
                            validation: validation,
                            options: enumProperties,
                            classes: {
                                outer: {'formkit-outer': false},
                                inner: {'formkit-inner': false},
                                input: {'formkit-input': false}
                            }
                        }
                    )
                }
            } else {
                {
                    /* create a text form */
                    schema.push({
                            $formkit: "text",
                            label: humanizeString(index),
                            placeholder: humanizeString(index),
                            name: index,
                            validation: validation,
                            classes: {
                                inner: {'formkit-inner': false, 'p-inputwrapper': true},
                                input: {'formkit-input': false, 'p-inputtext': true}
                            }
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
