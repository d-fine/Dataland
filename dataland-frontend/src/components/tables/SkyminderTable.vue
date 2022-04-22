<template>
  <table>
    <caption><h4>Skyminder Results</h4></caption>
    <thead>
      <tr>
        <th v-for="(header, i) in headers" :key="i">{{ header }}</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(dataset, i) in data" :key="i">
        <td>{{dataset.name}}</td>
        <td>{{dataset.addresses[0]}}</td>
        <td>{{dataset.websites[0]}}</td>
        <td>{{dataset.emails[0]}}</td>
        <td>{{dataset.phones[0]}}</td>
        <td>Type: {{dataset.identifiers[0].type}}, Name: {{dataset.identifiers[0].name}} Number: {{dataset.identifiers[0].number}}</td>
      </tr>
    </tbody>
  </table>
</template>

<script>
export default {
  name: "SkyminderTable",
  props: ["headers", "data"]
}
</script>