<template>
  <v-layout row justify-center>
    <!--<v-btn color="primary" dark @click.native.stop="dialog = true">Open Dialog</v-btn>-->
    <v-dialog v-model="dialog" max-width="600">
      <v-card class="organisationPopup" style="height: calc(100vh - 160px);">
        <v-card-title class="headline">
          Organisations
          <v-spacer></v-spacer>
          <v-btn flat="flat" @click="create">Create</v-btn>
        </v-card-title>
        <v-card-text>
          <div style="overflow-y:scroll;position:absolute;top:70px;bottom:60px;left:0px;right:0px;">
            <organisation-list :on-edit="this.edit" :on-delete="this.delete"/>
          </div>
        </v-card-text>
        <v-card-actions style="position:absolute;bottom:0px;height:60px;left:0px;right:0px;">
          <v-btn flat="flat" @click.native="dialog = false">Close</v-btn> <!-- color="green darken-1" -->
          <v-spacer></v-spacer>
          <v-btn v-if="this.isEditMode" flat="flat" @click="save">Save</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-layout>
</template>
<script>
  import {eventBus} from '../main';
  import EventName from '../constants/EventName';
  import OrganisationList from './OrganisationList';

  export default {
    components: {OrganisationList},
    data () {
      return {
        dialog: false,
        isEditMode: false,
      };
    },
    computed: {
    },
    methods: {
      create: function () {
        console.log("### CREATE ");
      },
      edit: function (id) {
        console.log("### EDIT ", id);
      },
      delete: function (id) {
        console.log("### DELETE ", id);
      },
      save: function () {
        console.log("### SAVE");
      },
  },
    created () {
      const vm = this;
      eventBus.$on(EventName.ORGANISATION_POPUP, function (value) {
        vm.dialog = value.open;
        vm.isEditMode = false;
      });
    }
  };
</script>
