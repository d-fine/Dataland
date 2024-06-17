import { type Card } from '@/types/ContentTypes';

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
      window.location.href = `mailto:${email}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
    }
  }
}
