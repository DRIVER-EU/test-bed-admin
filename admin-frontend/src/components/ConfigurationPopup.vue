<template>
  <v-layout row justify-center>
    <!--<v-btn color="primary" dark @click.native.stop="dialog = true">Open Dialog</v-btn>-->
    <v-dialog v-model="dialog" max-width="600">
      <v-card class="configurationPopup" style="height: calc(100vh - 160px);">
        <v-card-title class="upload_headline">
          Configurations
          <v-spacer></v-spacer>
          <v-btn v-if="!this.isEditMode" flat="flat" @click="create" :disabled="isCreateAllowed()">Create</v-btn>
        </v-card-title>
        <v-card-text>
          <div style="overflow-y:scroll;position:absolute;top:70px;bottom:60px;left:0px;right:0px;">
            <configuration-form v-if="this.isEditMode" :configuration="this.editedConfiguration" ref="form" />
            <configuration-list v-else :on-edit="this.edit" :on-delete="this.delete"/>
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
  import ConfigurationList from './ConfigurationList';
  import ConfigurationForm from './ConfigurationForm';
  import {store} from '../store';

  export default {
    components: {ConfigurationList, ConfigurationForm},
    data () {
      return {
        dialog: false,
        isEditMode: false,
        editedConfiguration: null,
      };
    },
    computed: {
    },
    methods: {
      isCreateAllowed: function() {
        return !this.$store.getters.rightsMatrix.createConfiguration;
      },
      create: function () {
        this.editedConfiguration = null;
        this.isEditMode = true;
      },
      edit: function (entity) {
        this.editedConfiguration = entity;
        this.isEditMode = true;
      },
      delete: function (entity) {
        store.dispatch('removeConfiguration', entity);
      },
      save: function () {
        if (this.$refs.form.save()) {
          this.isEditMode = false;
          this.editedConfiguration = null;
        }
      },
      cancel: function () {
        this.isEditMode = false;
        this.editedConfiguration = null;
      },
  },
    created () {
      const vm = this;
      eventBus.$on(EventName.CONFIGURATION_POPUP, function (value) {
        vm.dialog = value.open;
        vm.isEditMode = false;
        vm.editedConfiguration = null;
      });
    }
  };
</script>
