<template>
  <v-form ref="form" lazy-validation v-model="valid" style="padding: 18px;">
    <v-text-field
      v-model=orgName
      :rules=orgNameRules
      label="Name"
      :counter="25"
      required
    ></v-text-field>
  </v-form>
</template>
<script>
  export default {
    name: "OrganisationForm",
    data: () => ({
      valid: false,
      orgName: '',
      orgNameRules: [
        v => !!v || 'Name is required',
        v => (v && v.length <= 25) || 'Max. 25 characters allowed.'
      ],
    }),
    computed: {
    },
    created() {
      const me = this;
      this.clear = this.clear.bind(this);
    },
    methods: {
      clear() {
        const me = this;
        // me.$refs.form.reset(); // leads to empty v-radio selection
        this.$refs.form.resetValidation();
        me.orgName = "";
      },
      save() {
        console.log("### FORM SAVE");
        return true;
      }
    },
    watch: {
    }
  };
</script>
