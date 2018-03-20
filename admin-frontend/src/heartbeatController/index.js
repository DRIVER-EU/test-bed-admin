import Vue from 'vue'
import {store} from '../store'
import {_} from 'vue-underscore'

export function heartbeatController() {
  let prop =  store.state.socket
  prop.messageAccepted = false;
  prop.pingTimer = setInterval(function () {
    store.$socket.send('{"mutation":"HBREQUEST",' +
      '"requestId": "' + _.uniqueId() + '",' +
      '"sendTime":"' + new Date().toUTCString() + '"}');
  }, 5000);
 prop.pingTimerOutTimer = setInterval(function () {
    if (!store.state.socket.messageAccepted) {
      Vue.prototype.$socket.close()
    }
    else {
      prop.messageAccepted = false;
    }
  }, 10000);
}