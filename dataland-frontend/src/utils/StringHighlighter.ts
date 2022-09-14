export interface StringSplitSearchMatch {
  text: string;
  highlight: boolean;
}

export function splitStringBySearchMatch(
  rawText: string,
  searchString: string
): Array<StringSplitSearchMatch> {
  if (searchString == "") return [{ text: rawText, highlight: false }];
  const ret = [];
  const escapedSearchString = searchString.replace(
    /[-/\\^$*+?.()|[\]{}]/g,
    "\\$&"
  );
  const regex = new RegExp(escapedSearchString, "gi");
  let match;
  let lastIndex = 0;
  while ((match = regex.exec(rawText)) != null) {
    if (lastIndex < match.index) {
      ret.push({
        text: rawText.substring(lastIndex, match.index),
        highlight: false,
      });
    }
    ret.push({
      text: rawText.substring(match.index, match.index + match[0].length),
      highlight: true,
    });
    lastIndex = match.index + match[0].length;
  }
  if (lastIndex < rawText.length) {
    ret.push({
      text: rawText.substring(lastIndex, rawText.length),
      highlight: false,
    });
  }
  return ret;
}
