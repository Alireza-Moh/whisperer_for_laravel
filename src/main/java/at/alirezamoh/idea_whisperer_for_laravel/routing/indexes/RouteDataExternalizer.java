package at.alirezamoh.idea_whisperer_for_laravel.routing.indexes;

import com.intellij.util.io.DataExternalizer;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class RouteDataExternalizer implements DataExternalizer<RouteData> {
    @Override
    public void save(@NotNull DataOutput dataOutput, RouteData routeData) throws IOException {
        dataOutput.writeUTF(routeData.name() != null ? routeData.name() : "");
        dataOutput.writeUTF(routeData.uri() != null ? routeData.uri() : "");
        dataOutput.writeUTF(routeData.filePath() != null ? routeData.filePath() : "");
        dataOutput.writeInt(routeData.offset());
    }

    @Override
    public RouteData read(@NotNull DataInput dataInput) throws IOException {
        String name = dataInput.readUTF();
        String uri = dataInput.readUTF();
        String filePath = dataInput.readUTF();
        int offset = dataInput.readInt();

        return new RouteData(name, uri, filePath, offset);
    }
}
