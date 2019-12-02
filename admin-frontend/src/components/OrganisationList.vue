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
          <v-btn icon @click.native="editOrganisation(props.item.id)">
            <v-icon>text_format</v-icon>
          </v-btn>
          <v-btn icon @click.native="deleteOrganisation(props.item.id)">
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
      editOrganisation: function(id) {
        if (this.onEdit) {
          this.onEdit(id);
        }
      },
      deleteOrganisation: function(id) {
        if (this.onDelete) {
          this.onDelete(id);
        }
      }
    },
    created () {
    }
  };
</script>
