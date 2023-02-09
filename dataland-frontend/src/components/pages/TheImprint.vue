<template>
  <div class="pb-8">
    <div class="m-0">
      <div class="grid align-items-center m-0">
        <div class="col-6 text-left">
          <router-link to="/">
            <img src="@/assets/images/logos/logo_dataland_long.svg" alt="Dataland Logo" class="pl-3 pt-2" />
          </router-link>
        </div>
      </div>
      <MarginWrapper class="text-left pl-2">
        <BackButton />
      </MarginWrapper>
      <MarginWrapper class="text-left pl-2">
        <div class="lg:col-6 md:col-12 sm:col-12 text-justify">
          <h2>Imprint</h2>
          <p>
            Diese Beta-Version von Dataland ist ein Gemeinschaftsprojekt der d-fine GmbH, der PricewaterhouseCoopers
            GmbH Wirtschaftsprüfungsgesellschaft.
          </p>
          <p>
            Kontakt: d-fine GmbH, An der Hauptwache 7, 60313 Frankfurt am Main, Telefon: +49 69 90737-0,
            dataland@d-fine.com
          </p>
          <p>
            Registergericht: Amtsgericht Frankfurt am Main, Registernummer: HRB 48 103,
            Umsatzsteuer-Identifikationsnummer gemäß § 27a Umsatzsteuergesetz:, Umsatzsteueridentifikationsnr. DE 813
            483 155, Sitz der Gesellschaft ist Frankfurt.
          </p>
          <p>
            Geschäftsführer: Dr. Florian Baumann, Christoph Belafi, Dr. Christoph Bennemann, Dr. Oliver Bohr, Dr.
            Christoph Burmester, Dr. Ashot Davtyan, Todor Dobrikov, Dr. Arndt Dombert, Dr. Uwe Dörr, Dr. Andreas Geyer,
            Nadine Gottschalk, Dr. Oliver Hein, Dr. Stefan Heinrichs, Dr. Matthias Hirtschulz, Dr. Bodo Huckestein, Dr.
            Sascha Hügle, Dr. Tilman Huhne, Jens Itzenhäuser, Ulf Henning Jacobs, Dr. Jan Jureit, Dr. Oliver
            Kayser-Herold, Dr. Andreas Keese, Dr. Henriette Kröner, Dr. Florian Merz, Dr. Jochen Meyer, Dr. Karsten
            Meyer, Dr. Mathias Michel, Dr. Cornelius Mund, Dr. Christian Oehler, Wolfgang Pleyer, Dr. Jörn Rank, Dr.
            Christian Romeike, Alexander Schalk, Dr. Egbert Schark, Dr. Christoph Schneggenburger, Nadja Schuster, Dr.
            Markus Seifert, Dr. Constantin Sobiella, Artur Steiner, Dr. Roland Uhlig, Markus von Rothkirch, Dr.
            Hans-Peter Wächter, Dr. Andreas Werner, Dr. Magnus Wobben
          </p>
        </div>
      </MarginWrapper>
    </div>
  </div>
  <DatalandFooter />
</template>

<script lang="ts">
import BackButton from "@/components/general/BackButton.vue";
import MarginWrapper from "@/components/wrapper/MarginWrapper.vue";
import DatalandFooter from "@/components/general/DatalandFooter.vue";

export default {
  name: "TheImprint",
  components: {
    MarginWrapper,
    BackButton,
    DatalandFooter,
  },
};
</script>
