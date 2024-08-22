<template>
  <LegalPage>
    <h2>Impressum</h2>
    <p>
      Dataland GmbH
      <br />
      Paul-Ehrlich-Straße 51
      <br />
      60596 Frankfurt am Main
    </p>
    <p>Vertretungsberechtige Geschäftsführer: Frederik Christiaan Breen, Dr. Moritz Benjamin Kiese</p>
    <p>
      Telefon: +49 1511 77 22 399
      <br />
      E-Mail: erik.breen@dataland.com
    </p>
    <p>
      Registergericht: Amtsgericht Frankfurt am Main
      <br />
      Registernummer: HRB 132445
    </p>
    <p>Umsatzsteuer-Identifikationsnummer: DE363713774</p>
    <p>Inhaltlich verantwortlich: Frederik Christiaan Breen</p>
  </LegalPage>
</template>

<script lang="ts">
import LegalPage from '@/components/generics/LegalPage.vue';

export default {
  name: 'TheImprint',
  components: { LegalPage },
};
</script>
