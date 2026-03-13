import { type Card, type Content } from '@/types/ContentTypes';
import contentData from '@/assets/content.json';

const content: Content = contentData;
const getInTouchSection = content.pages.find((page) => page.url === '/')?.sections.find((section) => section.title === 'Get in touch');

export const homeEmailCard = getInTouchSection?.cards?.[3];
export const aboutEmailCard = getInTouchSection?.cards?.[4];

/**
 * Opens the email client with an email generated from the information provided by a content card
 * @param card the card providing the content
 */
export function openEmailClient(card?: Card): void {
  if (card) {
    const email = card?.icon ?? '';
    const subject = card?.title ?? '';
    const body = card?.text ?? '';

    if (email && subject && body) {
      globalThis.location.href = `mailto:${email}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
    }
  }
}
