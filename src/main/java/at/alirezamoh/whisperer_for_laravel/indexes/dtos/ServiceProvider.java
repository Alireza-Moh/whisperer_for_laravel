package at.alirezamoh.whisperer_for_laravel.indexes.dtos;

import java.util.Map;

public class ServiceProvider {
    private Map<String, String> configKeys;

    private Map<String, String> bladeFiles;

    public ServiceProvider(Map<String, String> configKeys, Map<String, String> bladeFiles) {
        this.configKeys = configKeys;
        this.bladeFiles = bladeFiles;
    }

    public Map<String, String> getConfigKeys() {
        return configKeys;
    }

    public Map<String, String> getBladeFiles() {
        return bladeFiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceProvider that = (ServiceProvider) o;

        if (!configKeys.equals(that.configKeys)) return false;
        return bladeFiles.equals(that.bladeFiles);
    }

    @Override
    public int hashCode() {
        int result = configKeys != null ? configKeys.hashCode() : 0;
        result = 31 * result + (bladeFiles != null ? bladeFiles.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ServiceProvider{" +
            "configKeys=" + configKeys +
            ", bladeFiles=" + bladeFiles +
            '}';
    }
}
