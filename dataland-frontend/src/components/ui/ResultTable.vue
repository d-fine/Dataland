<template>
  <table>
    <caption>Table of Results</caption>
    <thead>
      <tr>
        <th v-for="(header, i) in headers" :key="i">{{ header }}</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(dataset, i) in data" :key="i">
        <td>{{dataset.id}}</td>
        <td>{{dataset.name}}</td>
      </tr>
    </tbody>
  </table>
</template>

<script>
export default {
  name: "ResultTable",
  props: ["headers", "data"]
}
</script>