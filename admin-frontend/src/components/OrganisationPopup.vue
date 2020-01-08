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
            <organisation-form v-if="this.isEditMode" :organisation="this.editedOrganisation" ref="form" />
            <organisation-list v-else :on-edit="this.edit" :on-delete="this.delete"/>
          </div>
        </v-card-text>
        <v-card-actions style="position:absolute;bottom:0px;height:60px;left:0px;right:0px;">
          <v-spacer></v-spacer>
          <v-btn v-if="this.isEditMode" flat="flat" @click="cancel">Cancel</v-btn>
          <v-btn v-if="this.isEditMode" flat="flat" @click="save">Save</v-btn>
          <v-btn v-if="!this.isEditMode" flat="flat" @click.native="dialog = false">Close</v-btn> <!-- color="green darken-1" -->
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-layout>
</template>
<script>
  import {eventBus} from '../main';
  import EventName from '../constants/EventName';
  import OrganisationList from './OrganisationList';
  import OrganisationForm from './OrganisationForm';
  import {store} from '../store';

  export default {
    components: {OrganisationList, OrganisationForm},
    data () {
      return {
        dialog: false,
        isEditMode: false,
        editedOrganisation: null,
      };
    },
    computed: {
    },
    methods: {
      create: function () {
        this.editedOrganisation = null;
        this.isEditMode = true;
      },
      edit: function (entity) {
        this.editedOrganisation = entity;
        this.isEditMode = true;
      },
      delete: function (entity) {
        store.dispatch('removeOrganisation', entity);
      },
      save: function () {
        if (this.$refs.form.save()) {
          this.isEditMode = false;
          this.editedOrganisation = null;
        }
      },
      cancel: function () {
        this.isEditMode = false;
        this.editedOrganisation = null;
      },
  },
    created () {
      const vm = this;
      eventBus.$on(EventName.ORGANISATION_POPUP, function (value) {
        vm.dialog = value.open;
        vm.isEditMode = false;
        vm.editedOrganisation = null;
      });
    }
  };
</script>
