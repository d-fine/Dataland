<template>
  <nav>
    <div class="nav-wrapper orange darken-2">
      <ul id="nav-mobile" class="center-align hide-on-med-and-down">
        <li><router-link to="/">Home</router-link> </li>
        <li><router-link to="/upload">Data Upload</router-link></li>
        <li><router-link to="/search">Data Search</router-link></li>
        <li><router-link to="/eutaxonomies">EU Taxonomy Data</router-link></li>
      </ul>
    </div>
  </nav>
</template>

<script>
export default {
  name: "NavbarTop"
}
</script>