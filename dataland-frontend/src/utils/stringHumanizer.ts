export function humanizeString(text: string): string {
    // Split the sting to words
    const processedText = text.replace(/([A-Z])/g, " $1");
    // uppercase the first letter of the first word
    return processedText.charAt(0).toUpperCase() + processedText.slice(1);
}