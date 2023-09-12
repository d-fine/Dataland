// ContentTypes.ts
export interface Card {
  icon?: string;
  title?: string;
  text: string;
  date?: string;
}

export interface Section {
  title: string;
  text: string[];
  image?: string[];
  cards?: Card[];
}

export interface Page {
  title: string;
  url: string;
  sections: Section[];
}

export interface Content {
  pages: Page[];
}
