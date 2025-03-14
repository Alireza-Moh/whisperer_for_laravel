package at.alirezamoh.whisperer_for_laravel.indexes.dataExternalizeres;

import com.intellij.util.io.DataInputOutputUtil;
import com.intellij.util.io.DataExternalizer;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListStringDataExternalizer implements DataExternalizer<List<String>> {
    @Override
    public void save(@NotNull DataOutput dataOutput, List<String> strings) throws IOException {
        DataInputOutputUtil.writeINT(dataOutput, strings.size());
        for (String s : strings) {
            dataOutput.writeUTF(s);
        }
    }

    @Override
    public List<String> read(@NotNull DataInput dataInput) throws IOException {
        int size = DataInputOutputUtil.readINT(dataInput);
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(dataInput.readUTF());
        }
        return list;
    }
}