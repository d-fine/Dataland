import PersonCard from '@/components/resources/aboutPage/PersonCard.vue';
import { getMountingFunction } from '@ct/testUtils/Mount';
import type { Person, AdvisoryPerson } from '@/components/resources/aboutPage/aboutContent';

const leadershipPerson: Person = {
  name: 'Jane Doe',
  role: 'Chief Executive Officer',
  bio: 'Jane is a seasoned executive with over 20 years of experience.',
  imagePath: '/static/about/team-jane-doe.webp',
};

const advisoryPerson: AdvisoryPerson = {
  name: 'John Smith',
  role: 'Advisory Board Member',
  organisation: 'Acme Corp',
  imagePath: '/static/about/team-john-smith.webp',
};

describe('PersonCard', () => {
  describe("variant='leadership'", () => {
    it('renders bio and does not render organisation label', () => {
      getMountingFunction()(PersonCard, {
        props: { person: leadershipPerson, variant: 'leadership' },
      }).then(() => {
        cy.get('[data-test="person-bio"]').should('be.visible');
        cy.get('[data-test="person-organisation"]').should('not.exist');
      });
    });
  });

  describe("variant='advisory'", () => {
    it('renders organisation label and does not render bio', () => {
      getMountingFunction()(PersonCard, {
        props: { person: advisoryPerson, variant: 'advisory' },
      }).then(() => {
        cy.get('[data-test="person-organisation"]').should('be.visible');
        cy.get('[data-test="person-bio"]').should('not.exist');
      });
    });
  });
});
