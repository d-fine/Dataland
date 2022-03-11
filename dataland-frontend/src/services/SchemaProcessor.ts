import backend from "../schema/backendOpenApi.json"

const rawSchema = backend.components.schemas.CompanyMetaInformation

export default class SchemaProcessor {
    constructor() {
    }
    process(): Object {
        return {
            $formkit: 'text',
            for: ['item', 'key', rawSchema.properties],
            label: "$key",
            placeholder: "$key",
            name: "$key"
        };

    }
}
