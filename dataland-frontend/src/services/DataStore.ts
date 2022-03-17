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
            return this.getSchemaJSON()
        } else {
            return this.getSchemaFromFunction()
        }
    }

    private getSchemaFromFunction(): Object {
        const getAllParams = require('get-parameter-names')
        const params = getAllParams(this.axiosFunction)
        const schema = []

        for (const index in params) {
            const value = params[index]
            if (value != "options") {
                console.log(value)
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

    private getSchemaJSON(): Object {
        const schema = []
        for (const index in this.rawSchema.properties) {
            if ("enum" in this.rawSchema.properties[index]) {
                if (this.rawSchema.properties[index].enum.length > 2) {
                    schema.push({
                        $formkit: 'select',
                        label: humanizeString(index),
                        placeholder: "Please Choose",
                        name: index,
                        validation: this.rawSchema.required.includes(index) ? "required" : "",
                            options: this.rawSchema.properties[index].enum
                        }
                    )
                } else {
                    schema.push({
                            $formkit: 'radio',
                            label: humanizeString(index),
                            name: index,
                            validation: this.rawSchema.required.includes(index) ? "required" : "",
                            classes: {
                                outer: {'formkit-outer': false},
                                inner: {'formkit-inner': false},
                                input: {'formkit-input': false}
                            },
                            options: this.rawSchema.properties[index].enum
                        }
                    )
                }
            } else {
                schema.push({
                        $formkit: 'text',
                        label: humanizeString(index),
                        placeholder: humanizeString(index),
                        name: index,
                        validation: this.rawSchema.required.includes(index) ? "required" : ""
                    }
                )
            }
        }
        return schema
    }

    perform(...args: any[]): any {
        return this.axiosFunction(...args)
    }
}
