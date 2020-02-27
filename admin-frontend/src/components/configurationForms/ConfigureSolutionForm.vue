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
            :counter="255"
            required
          ></v-text-field>
          <v-text-field
            v-model="name"
            :rules="nameRules"
            label="Name"
            :counter="255"
            required
          ></v-text-field>
          <v-radio-group row v-model="isService" mandatory label="Testbed service:">
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
          <v-select
            v-model="organisation"
            :items="organisations"
            item-text="orgName"
            label="Organisation Name"
            return-object
          ></v-select>
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
  import {eventBus} from '../../main'
  import {store} from '../../store'
  import EventName from '../../constants/EventName'

  export default {
    name: 'ConfigureSolutionForm',
    data: () => ({
      open: false,
      editedItem: null,
      valid: false,
      clientId: '',
      clientIdRules: [
        v => !!v || 'ID is required',
        v => (v && v.length <= 255) || 'Max. 255 characters allowed.'
      ],
      name: '',
      nameRules: [
        v => !!v || 'Name is required',
        v => (v && v.length <= 255) || 'Max. 255 characters allowed.'
      ],
      isService: true,
      isAdmin: false,
      state: false,
      description: '',
      organisation: null,
    }),
    computed: {
      organisations () {
        return this.$store.state.organisations;
      }
    },
    created () {
      const me = this
      this.clear = this.clear.bind(this);
      eventBus.$on(EventName.OPEN_SOLUTION_FORM, (item) => {
        me.editedItem = item;
        me.clear();
        if (item) {
          me.clientId = item.clientId;
          me.name = item.name;
          me.isService = item.isService;
          me.isAdmin = item.isAdmin;
          me.state = item.state;
          me.description = item.description;
          me.organisation = item.organisation;
        }
        me.open = true;
      });
    },
    methods: {
      submit () {
        const me = this
        if (me.$refs.form.validate()) {
          let solution = {
            id: this.editedItem ? this.editedItem.id : null,
            clientId: me.clientId,
            name: me.name,
            isService: me.isService,
            isAdmin: me.isAdmin,
            state: me.state,
            description: me.description,
            organisation: me.organisation,
          }
          if (this.editedItem) {
            store.dispatch('updateSolution', solution);
          } else {
            store.dispatch('addSolution', solution);
          }
          me.clear();
          me.open = false;
        }
      },
      clear () {
        const me = this;
        // me.$refs.form.reset(); // leads to empty v-radio selection
        this.$refs.form.resetValidation();
        me.clientId = "";
        me.name = "";
        me.isService = true;
        me.isAdmin = false;
        me.state = false;
        me.description = "";
        me.organisation = null;
      }
    },
  }
</script>

<style scoped>

</style>
