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
            v-model="managingType"
            :items="managingTypesItems"
            :rules="[v =>(!!v && v.length > 0) || 'At least one type is required']"
            multiple
            chips
            deletable-chips
            label="Select managing types"
            required
          ></v-select>
          <v-textarea
            v-model=description
            label="Description"
          ></v-textarea>
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
  import EventName from '../../constants/EventName'
  export default {
    name: "ConfigureGatewayForm",
    data: () => ({
      open: false,
      editedItem: null,
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
      managingType:[],
      description: ''
    }),
    created() {
      const me = this
      this.clear = this.clear.bind(this);
      eventBus.$on(EventName.OPEN_GATEWAY_FORM, (item) => {
        me.editedItem = item;
        me.clear();
        if (item) {
          me.clientId = item.clientId;
          me.name = item.name;
          me.managingType = item.managingType;
          me.description = item.description;
        }
        me.open = true;
      })
    },
    computed: {
      managingTypesItems: function () {
        return this.$store.getters.standardNames
      }
    },
    methods: {
      submit() {
        const me = this;
        if (me.$refs.form.validate()) {
          let gateway = {
            id: this.editedItem ? this.editedItem.id : null,
            clientId: me.clientId,
            name: me.name,
            managingType: me.managingType,
            description: me.description
          }

          console.log("### X", gateway);

          if (this.editedItem) {
            store.dispatch('updateGateway', gateway);
          } else {
            store.dispatch('addGateway', gateway);
          }
          me.clear()
          me.open = false
        }
      },
      clear() {
        const me = this;
        // me.$refs.form.reset(); // leads to empty v-radio selection
        this.$refs.form.resetValidation();
        me.clientId = "";
        me.name = "";
        me.managingType = [];
        me.description = "";
      }
    }
  }
</script>

<style scoped>

</style>
