export default class SchemaProcessor {
    constructor() {
    }

    process(rawSchema: any): Object {
        return {
            $formkit: 'text',
            for: ['item', 'key', rawSchema.properties],
            label: "$key",
            placeholder: "$key",
            name: "$key"
        };
    }
}
