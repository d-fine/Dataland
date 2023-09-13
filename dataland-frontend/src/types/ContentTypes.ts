export interface Content {
  pages: Page[];
}

export interface Page {
  title: string;
  url: string;
  sections: Section[];
}

export interface Section {
  title: string;
  text: string[];
  image?: string[];
  cards?: Card[];
}

export interface Card {
  icon?: string;
  text: string;
  title?: string;
  date?: string;
}
