// ContentTypes.ts
export interface Section {
  title: string;
  text: string[];
  image?: string[];
}

export interface Page {
  title: string;
  url: string;
  sections: Section[];
}

export interface Content {
  pages: Page[];
}
