export class StringHumanizer {

    private convertCamelCaseToSentenceCase(text: string): string {
        // Split the sting to words
        const processedText = text.replace(/((?!^)[A-Z][a-z]+|([A-Z]+)$|[0-9]+)/g, " $1")
        // uppercase the first letter of the first word
        return processedText.charAt(0).toUpperCase() + processedText.slice(1)
    }

    humanize(text: string): string {
        const mappingObject: { [key: string]: string } = {
            cdax: "CDAX",
            dax: "DAX",
            gex: "GEX",
            mdax: "MDAX",
            sdax: "SDAX",
            tecdax: "TecDAX",
            scalehdax: "ScaleHDAX",
            dax50esg: "DAX 50 ESG"
        }

        if (text.toLowerCase() in mappingObject) {
            return mappingObject[text.toLowerCase()]
        }
        return this.convertCamelCaseToSentenceCase(text)
    }
}
