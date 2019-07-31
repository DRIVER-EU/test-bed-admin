<template>
  <v-dialog v-model="open" max-width="700px" persistent>
    <v-card>
      <v-card-title class="primary--text">Configure solution
      </v-card-title>
      <v-card-text>
        <v-form ref="form" v-model="valid">
          <v-text-field
            v-model="clientId"
            :rules="clientIdRules"
            label="ID"
            :counter="25"
            required
          ></v-text-field>
          <v-text-field
            v-model="name"
            :rules="nameRules"
            label="Name"
            :counter="25"
            required
          ></v-text-field>
          <v-radio-group row v-model="isService"  mandatory label="Testbed service:">
            <v-spacer></v-spacer>
            <v-radio
              label="Yes"
              :value="true"
            ></v-radio>
            <v-radio
              label="No"
              :value="false"
            ></v-radio>
          </v-radio-group>
          <v-textarea
            v-model="description"
            label="Description"
          ></v-textarea>
        </v-form>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn @click="open=false">Close</v-btn>
        <v-btn @click="submit" class="primary--text">Submit</v-btn>
      </v-card-actions>
    </v-card>

  </v-dialog>
</template>

<script>
  import {eventBus} from "../../main";
  import {store} from '../../store'
  export default {
    name: "ConfigureSolutionForm",
    data: () => ({
      open: false,
      valid: false,
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
      isService: true,
      description: ''
    }),
    created() {
      eventBus.$on('openConfigureSolutionForm', () => {
        this.open = true
      });
    },
    methods: {
      submit() {
        const self = this;
        if (self.$refs.form.validate()) {
          let solution = {
            clientId: self.clientId,
              name: self.name,
            isAdmin: false,
            isService: self.isService,
            description: self.description
          }
          store.dispatch('addSolution', solution)
          self.clear()
          self.open = false
        }
      },
      clear() {
        const self = this;
        self.$refs.form.reset()
         self.isService = true
      }
    }
  }
</script>

<style scoped>

</style>
