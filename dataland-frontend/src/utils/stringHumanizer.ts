export function humanizeString(text: string): string {
    // Split the sting to words
    const processedText = text.replace(/([A-Z][a-z])/g, " $1").replaceAll("_", " ")
    // uppercase the first letter of the first word
    return processedText.charAt(0).toUpperCase() + processedText.slice(1)
}