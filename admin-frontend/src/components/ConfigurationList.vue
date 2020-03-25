<template>
  <v-data-table :items="configurations" hide-actions class="configurationsTable">
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
        <td style="white-space: nowrap;text-overflow: ellipsis;max-width: 200px;overflow: hidden;">{{props.item.name}}
        </td>
        <td class="text-xs-center">
          <v-btn icon @click.native="editConfiguration(props.item)" :disabled="isEditAllowed()">
            <v-icon>text_format</v-icon>
          </v-btn>
          <v-btn icon @click.native="deleteConfiguration(props.item)" :disabled="isDeleteAllowed()">
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
      configurations () {
        return this.$store.state.configurations;
      },
    },
    methods: {
      isEditAllowed: function() {
        return !this.$store.getters.rightsMatrix.editConfiguration;
      },
      isDeleteAllowed: function() {
        return !this.$store.getters.rightsMatrix.removeConfiguration;
      },
      editConfiguration: function(entity) {
        if (this.onEdit) {
          this.onEdit(entity);
        }
      },
      deleteConfiguration: function(entity) {
        if (this.onDelete) {
          this.onDelete(entity);
        }
      }
    },
    created () {
    }
  };
</script>
