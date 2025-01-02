package at.alirezamoh.whisperer_for_laravel.indexes.dataExternalizeres;

import at.alirezamoh.whisperer_for_laravel.indexes.dtos.ServiceProvider;
import com.intellij.util.io.DataExternalizer;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServiceProviderDataExternalizer implements DataExternalizer<ServiceProvider> {
    public static final DataExternalizer<ServiceProvider> INSTANCE = new ServiceProviderDataExternalizer();

    @Override
    public void save(@NotNull DataOutput dataOutput, ServiceProvider serviceProvider) throws IOException {
        Map<String, String> configKeys = serviceProvider.getConfigKeys();
        dataOutput.writeInt(configKeys.size());
        for (Map.Entry<String, String> entry : configKeys.entrySet()) {
            dataOutput.writeUTF(entry.getKey());
            dataOutput.writeUTF(entry.getValue());
        }

        Map<String, String> bladeFiles = serviceProvider.getBladeFiles();
        dataOutput.writeInt(bladeFiles.size());
        for (Map.Entry<String, String> entry : bladeFiles.entrySet()) {
            dataOutput.writeUTF(entry.getKey());
            dataOutput.writeUTF(entry.getValue());
        }
    }

    @Override
    public ServiceProvider read(@NotNull DataInput dataInput) throws IOException {
        int configKeysSize = dataInput.readInt();
        Map<String, String> configKeys = new HashMap<>(configKeysSize);
        for (int i = 0; i < configKeysSize; i++) {
            String key = dataInput.readUTF();
            String value = dataInput.readUTF();
            configKeys.put(key, value);
        }

        int bladeFilesSize = dataInput.readInt();
        Map<String, String> bladeFiles = new HashMap<>(bladeFilesSize);
        for (int i = 0; i < bladeFilesSize; i++) {
            String key = dataInput.readUTF();
            String value = dataInput.readUTF();
            bladeFiles.put(key, value);
        }

        return new ServiceProvider(configKeys, bladeFiles);
    }
}
