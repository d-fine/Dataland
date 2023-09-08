// ContentTypes.ts
export interface Card {
  icon: string;
  text: string;
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
