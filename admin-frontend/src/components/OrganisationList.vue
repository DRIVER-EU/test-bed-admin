<template>
  <v-data-table :items="organisations" hide-actions class="organisationsTable">
    <template slot="headers" slot-scope="props">
      <tr>
        <th style="vertical-align: top;">
          <div class="primary--text" style="padding: 16px;">Name</div>
        </th>
        <th style="width:160px;">
          <div class="primary--text" style="padding: 16px;">Actions</div>
        </th>
      </tr>
    </template>
    <template slot="items" slot-scope="props" style="">
      <tr>
        <td style="white-space: nowrap;text-overflow: ellipsis;max-width: 200px;overflow: hidden;">{{props.item.orgName}}
        </td>
        <td class="text-xs-center">
          <v-btn icon @click.native="editOrganisation(props.item)" :disabled="isEditAllowed()">
            <v-icon>text_format</v-icon>
          </v-btn>
          <v-btn icon @click.native="deleteOrganisation(props.item)" :disabled="isDeleteAllowed()">
            <v-icon>delete_outline</v-icon>
          </v-btn>
        </td>
      </tr>
    </template>
  </v-data-table>
</template>
<script>
  export default {
    props: ['onEdit', 'onDelete'],
    data () {
      return {
        dialog: false,
      };
    },
    computed: {
      organisations () {
        return this.$store.state.organisations;
      },
    },
    methods: {
      isEditAllowed: function() {
        return !this.$store.getters.rightsMatrix.editOrganisation;
      },
      isDeleteAllowed: function() {
        return !this.$store.getters.rightsMatrix.removeOrganisation;
      },
      editOrganisation: function(entity) {
        if (this.onEdit) {
          this.onEdit(entity);
        }
      },
      deleteOrganisation: function(entity) {
        if (this.onDelete) {
          this.onDelete(entity);
        }
      }
    },
    created () {
    }
  };
</script>
