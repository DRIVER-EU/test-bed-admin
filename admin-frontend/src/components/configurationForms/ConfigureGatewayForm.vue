<template>
  <v-dialog v-model="open" max-width="700px" persistent>
    <v-card>
      <v-card-title class="primary--text">Configure gateway
      </v-card-title>
      <v-card-text>
        <v-form ref="form" lazy-validation v-model="valid">
          <v-text-field
            v-model=clientId
            :rules=clientIdRules
            label="ID"
            :counter="25"
            required
          ></v-text-field>
          <v-text-field
            v-model=name
            :rules=nameRules
            label="Name"
            :counter="25"
            required
          ></v-text-field>
          <v-select
            v-model=managingTypes
            :items="managingTypesItems"
            :rules="[v =>(!!v && v.length > 0) || 'At least one type is required']"
            multiple
            chips
            deletable-chips
            label="Select managing types"
            required
          ></v-select>
          <v-text-field
            v-model=description
            label="Description"
            multi-line
          ></v-text-field>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="open=false">Close</v-btn>
        <v-btn @click="submit" class="primary--text">Submit</v-btn>
      </v-card-actions >
    </v-card>

  </v-dialog>
</template>

<script>
  import {eventBus} from "../../main";
  import {store} from '../../store'
  export default {
    name: "ConfigureGatewayForm",
    data: () => ({
      open: false,
      valid: true,
      clientId: '',
      clientIdRules: [
        v => !!v || 'ID is required',
        v => (v && v.length <= 25) || 'Max. 25 characters allowed.'
      ],
      name: '',
      nameRules: [
        v => !!v || 'Name is required',
        v => (v && v.length <= 25) || 'Max. 25 characters allowed.'
      ],
      managingTypes:[],
      description: ''
    }),
    created() {

      eventBus.$on('openConfigureGatewayForm', () => {
        this.open = true
      })
    },
    computed: {
      managingTypesItems: function () {
        return this.$store.getters.standardNames
      }
    },
    methods: {
      submit() {
        const self = this;
        if (self.$refs.form.validate()) {
          let gateway = {
            clientId: self.clientId,
            name: self.name,
            managingTypes: self.managingTypes,
            description: self.description
          }
          store.dispatch('addGateway', gateway)
          self.clear()
          self.open = false
        }
      },
      clear() {
        this.$refs.form.reset()
        //this.valid = true
      }
    }
  }
</script>

<style scoped>

</style>
