import {store} from '../store';
import {fetchService} from './FetchService'

class ConfigurationService {
  static INSTANCE = new ConfigurationService();

  static getInstance () {
    return ConfigurationService.INSTANCE;
  }

  switchToMode(modeName, currentConfiguration, configurations) {
    const fallbackConfigName = configurations.length > 0 ? configurations[0].name : null;
    const newConfiguration = currentConfiguration || {};
    newConfiguration.testbedMode = modeName;
    newConfiguration.configName = currentConfiguration.configName || fallbackConfigName;
    console.log('Setting testbed mode to', modeName, newConfiguration);
    this.storeConfiguration(newConfiguration);
  }

  switchToConfiguration(configName, currentConfiguration, modes) {
    const fallbackMode = modes.length > 0 ? modes[0] : null;
    const newConfiguration = currentConfiguration || {};
    newConfiguration.testbedMode = currentConfiguration.testbedMode || fallbackMode;
    newConfiguration.configName = configName;
    console.log('Setting configuration to', configName, newConfiguration);
    this.storeConfiguration(newConfiguration);
  }

  storeConfiguration(configuration) {
    fetchService.performPost('setActTestbedConfig', configuration).then(() => {
      store.dispatch('getCurrentConfiguration');
      store.dispatch('isTestbedInitialized');
      store.dispatch('getSolutions');
      store.dispatch('getTopics');
      store.dispatch('getGateways');
    }).catch(ex => console.log(ex));
  }
}

const configurationService = ConfigurationService.getInstance();

export {configurationService};
