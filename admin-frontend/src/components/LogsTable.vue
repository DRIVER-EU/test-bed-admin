<template>
  <v-flex>
    <v-card class="tablePanel" style="">
      <v-data-table :items=logEntries :headers="headers" hide-actions>
        <template slot="items" slot-scope="props">
          <tr
            :class="{logError: props.item.level === 'ERROR' || props.item.level === 'SEVERE' || props.item.level === 'CRITICAL', logWarning: props.item.level === 'WARNING'}">
            <td>{{props.item.id}}</td>
            <td>{{props.item.sendDate}}</td>
            <td>{{props.item.level}}</td>
            <td>{{props.item.clientId}}</td>
            <td>{{props.item.message}}</td>
          </tr>
        </template>
      </v-data-table>
    </v-card>
    <v-card class="tablePanel" style="">
      <div class="text-xs-center" style="padding: 10px 0px">
        <v-pagination v-model="pagination.page" :length=logsPageCount :total-visible="11" @input="switchPage"></v-pagination>
      </div>
    </v-card>
  </v-flex>
</template>


<script>
  export default {
    name: "LogsTable",
    data() {
      return {
        headers: [
          {
            text: 'LogID',
            align: 'left',
            sortable: false,
            value: 'id'
          },
          {
            text: 'Date/Time',
            align: 'left',
            sortable: false,
            value: 'date'
          },
          {
            text: 'Level',
            align: 'left',
            sortable: false,
            value: 'level'
          },
          {
            text: 'ClientID',
            align: 'left',
            sortable: false,
            value: 'clientID'
          },
          {
            text: 'Message',
            align: 'left',
            sortable: false,
            value: 'message'
          },
        ],
        pagination: {
          page: 1,
        }
      }
    },
    computed: {
      logEntries: function () {
        return this.$store.getters.logEntries
      },
      logsPageCount: function () {
        return this.$store.state.logsPageCount;
      },
    },
    methods: {
      switchPage (page) {
        this.reloadData();
      },
      reloadData() {
        this.$store.dispatch('getPageCount');
        this.$store.dispatch('getLogs', {page: this.pagination.page});
      },
    },
    created () {
      this.$store.dispatch('getLogs', {page: 1});
    }
  }
</script>
