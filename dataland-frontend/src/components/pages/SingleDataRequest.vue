<script setup lang="ts"></script>

<template>
  <div class="col-9">
    <div data-test="provideContactDetails" class="bg-white radius-1 p-4">
      <h4 class="p-0">Provide Contact Details</h4>
      <label for="Email" class="label-with-optional"> Email <span class="optional-text">Optional</span> </label>
      <FormKit type="text" name="Email" />
      <p class="gray-text font-italic" style="text-align: left">
        By specifying a contact person here, your data request will be directed accordingly.<br />
        this increases the chances of expediting the fulfillment of your request.
      </p>
      <p class="gray-text font-italic" style="text-align: left">
        If you don't have a specific contact person, no worries.<br />
        We are committed to fulfilling your request to the best of our ability.
      </p>
      <label for="Message" class="label-with-optional"> Message <span class="optional-text">Optional</span> </label>
      <FormKit type="textarea" name="Message" />
      <p class="gray-text font-italic" style="text-align: left">
        Let your contact know what exactly your are looking for.
      </p>
    </div>
  </div>
</template>

<style scoped lang="scss">
.label-with-optional {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.optional-text {
  font-style: italic;
  color: var(--primary-orange);
  margin-left: 8px;
}
</style>
