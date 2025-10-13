<template>
  <Dialog v-model:visible="visible" header="Edit Data Point" :modal="true" :closable="false">
    <form @submit.prevent="save">
      <div class="form-group">
        <label for="value">Value</label>
        <InputText id="value" v-model="dataPoint.value" required />
      </div>
      <div class="form-group">
        <label for="document">Document</label>
        <Dropdown id="document" v-model="dataPoint.document" :options="documents" optionLabel="name" optionValue="id" required />
      </div>
      <div class="form-group">
        <label for="additionalField">Additional Field</label>
        <InputText id="additionalField" v-model="dataPoint.additionalField" />
      </div>
      <div class="modal-actions">
        <Button label="Cancel" class="p-button-text" @click="$emit('close')" />
        <Button label="Save" type="submit" class="p-button-primary" />
      </div>
    </form>
  </Dialog>
</template>

<script>
import { Dialog } from 'primevue/dialog';
import { InputText } from 'primevue/inputtext';
import { Dropdown } from 'primevue/dropdown';
import { Button } from 'primevue/button';

export default {
  name: 'EditDataPointModal',
  components: { Dialog, InputText, Dropdown, Button },
  props: {
    visible: {
      type: Boolean,
      required: true,
    },
    dataPoint: {
      type: Object,
      required: true,
    },
    documents: {
      type: Array,
      required: true,
    },
  },
  methods: {
    save() {
      this.$emit('save', this.dataPoint);
    },
  },
};
</script>

<style scoped>
.form-group {
  margin-bottom: 1rem;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
