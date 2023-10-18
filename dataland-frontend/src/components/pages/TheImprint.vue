<template>
  <LegalPage>
    <h2>Imprint</h2>
    <p>
      Dataland GmbH
      <br />
      Paul-Ehrlich-Straße 51
      <br />
      60596 Frankfurt am Main
    </p>
    <p>Vertretungsberechtige Geschäftsführer: Frederik Christiaan Breen, Dr. Moritz Benjamin Kiese</p>
    <p>
      Telefon: …
      <br />
      E-Mail: …
    </p>
    <p>Registergericht: Amtsgericht Frankfurt am Main Registernummer: …</p>
    <p>Umsatzsteuer-Identifikationsnummer: …</p>
  </LegalPage>
</template>

<script lang="ts">
import LegalPage from "@/components/generics/LegalPage.vue";

export default {
  name: "TheImprint",
  components: { LegalPage },
};
</script>
