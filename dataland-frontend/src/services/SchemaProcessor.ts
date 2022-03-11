interface SchemaObject {
    [key: string]: any
}

export default class SchemaProcessor {
    constructor() {
    }
    process(rawSchema: SchemaObject): Object {
        return {
            $formkit: 'text',
            for: ['item', 'key', rawSchema.properties],
            label: "$key",
            placeholder: "$key",
            name: "$key"
        };

    }
}
