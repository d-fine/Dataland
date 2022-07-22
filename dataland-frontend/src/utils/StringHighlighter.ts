import DOMPurify from "dompurify";

/**
 * Given a (potentially user-controlled) input string and search string,
 * this function will generate XSS-Safe HTML that encapsulates all occurrences
 * of searchString in a <span class="searchMatchClass"></span>
 */
export function highlightSearchMatches(rawText: string, searchString: string, searchMatchClass: string): string {
  const escapedSearchString = searchString.replace(/[-/\\^$*+?.()|[\]{}]/g, "\\$&");
  const regex = new RegExp(escapedSearchString, "gi");
  const ret = rawText.replace(regex, `<span class="${searchMatchClass}">$&</span>`);
  return DOMPurify.sanitize(ret);
}
