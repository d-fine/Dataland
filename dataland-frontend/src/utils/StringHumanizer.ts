/**
 * Module to convert string to a human-readable text
 */

    /**
     * convert camel case string to sentence case string using regex
     *
     * @param  {string} rawText      is the string to be converted to a human-readable string
     */
    function convertCamelCaseToSentenceCase(rawText: string): string {
        // Split the sting to words
        const processedText = rawText.replace(/((?!^)[A-Z])/g, " $1")
        // uppercase the first letter of the first word
        return processedText.charAt(0).toUpperCase() + processedText.slice(1)
    }

    /**
     * get the representable text from the mapping object
     *
     * @param  {string} rawText      is the string to be converted to a human-readable string
     */
    function getStringFromMapper(rawText: string): string {
        const mappingObject: { [key: string]: string } = {
            cdax: "CDAX",
            dax: "DAX",
            gex: "GEX",
            mdax: "MDAX",
            sdax: "SDAX",
            tecdax: "TecDAX",
            hdax: "HDAX",
            dax50esg: "DAX 50 ESG",
            isin: "ISIN",
            permid: "PERM Id",
            lei: "LEI"
        }
        const lowerCaseText = rawText.toLowerCase()
        return (lowerCaseText in mappingObject) ? mappingObject[lowerCaseText] : ""
    }

    /**
     * convert string to a human-readable text
     *
     * @param  {string} rawText      is the string to be converted to a human-readable string
     */
    export function humanize(rawText: string): string {

        const valueFromMapping = getStringFromMapper(rawText)
        return (valueFromMapping == "") ? convertCamelCaseToSentenceCase(rawText) : valueFromMapping
    }
