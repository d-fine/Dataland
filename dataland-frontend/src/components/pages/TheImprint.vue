<template>
  <LegalPage>
    <h2>Impressum</h2>
    <p>
      Dataland GmbH
      <br />
      Am Steinernen Stock 1
      <br />
      Briefkasten Nr. 8
      <br />
      60320 Frankfurt am Main
    </p>
    <p>Vertretungsberechtige Geschäftsführer: Dr. Moritz Benjamin Kiese</p>
    <p>
      Telefon: +49 1511 77 22 399
      <br />
      E-Mail: mortiz.kiese@dataland.com
    </p>
    <p>
      Registergericht: Amtsgericht Frankfurt am Main
      <br />
      Registernummer: HRB 132445
    </p>
    <p>Umsatzsteuer-Identifikationsnummer: DE363713774</p>
    <p>Inhaltlich verantwortlich: Dr. Moritz Benjamin Kiese</p>
  </LegalPage>
</template>

<script lang="ts">
import LegalPage from '@/components/generics/LegalPage.vue';

export default {
  name: 'TheImprint',
  components: { LegalPage },
};
</script>
