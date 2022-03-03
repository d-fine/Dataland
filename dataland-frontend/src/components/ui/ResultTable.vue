<template>
  <table>
    <caption><h4>Table of Results</h4></caption>
    <thead>
      <tr>
        <th v-for="(header, i) in headers" :key="i">{{ header }}</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(dataset, i) in data" :key="i">
        <td>{{dataset.name}}</td>
        <td>{{dataset.address[0]}}</td>
        <td>{{dataset.website[0]}}</td>
        <td>{{dataset.email[0]}}</td>
        <td>{{dataset.phone[0]}}</td>
        <td>Type: {{dataset.identifier[0].type}}, Name: {{dataset.identifier[0].name}} Number: {{dataset.identifier[0].number}}</td>
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