export function humanizeString(text: string): string {
    // Split the sting to words
    // FixMe: improve the regex and the code
    const processedText = text.replace(/((?!^)[A-Z][a-z]+)/g, " $1")
        .replace(/([A-Z]+)$/g, " $1")
        .replace(/([0-9]+)/g, " $1")
    // uppercase the first letter of the first word
    return processedText.charAt(0).toUpperCase() + processedText.slice(1)
}