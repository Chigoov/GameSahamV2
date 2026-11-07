import com.android.tools.idea.io.grpc.ManagedChannelBuilder;
import com.google.gapid.proto.service.GapidGrpc;
import com.google.gapid.proto.service.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/** Local QA trace reader using the installed Android Studio profiler protocol. */
public final class ReadFrameTrace {
    public static void main(String[] args) throws Exception {
        var channel = ManagedChannelBuilder.forAddress("127.0.0.1", Integer.parseInt(args[0]))
                .usePlaintext().maxInboundMessageSize(64 * 1024 * 1024).build();
        try {
            var client = GapidGrpc.newBlockingStub(channel).withDeadlineAfter(60, TimeUnit.SECONDS);
            var loaded = client.loadCapture(Service.LoadCaptureRequest.newBuilder()
                    .setPath(Path.of(args[1]).toAbsolutePath().toString()).build());
            if (!loaded.hasCapture()) throw new IllegalStateException(loaded.getError().toString());
            String sql = Files.readString(Path.of(args[2]));
            var response = client.perfettoQuery(Service.PerfettoQueryRequest.newBuilder()
                    .setCapture(loaded.getCapture()).setQuery(sql).build());
            Files.writeString(Path.of(args[3] + ".proto.txt"), response.toString());
            if (!response.hasResult() || !response.getResult().getError().isEmpty())
                throw new IllegalStateException(response.toString());
            var result = response.getResult();
            var rows = new StringBuilder();
            for (var column : result.getColumnDescriptorsList()) rows.append(column.getName()).append('\t');
            rows.append('\n');
            for (int row = 0; row < result.getNumRecords(); row++) {
                for (int col = 0; col < result.getColumnDescriptorsCount(); col++) {
                    var values = result.getColumns(col);
                    if (!values.getIsNulls(row)) rows.append(switch (result.getColumnDescriptors(col).getType()) {
                        case LONG -> Long.toString(values.getLongValues(row));
                        case DOUBLE -> Double.toString(values.getDoubleValues(row));
                        case STRING -> values.getStringValues(row).replace('\t', ' ').replace('\n', ' ');
                        default -> throw new IllegalStateException("Unknown query value type");
                    });
                    rows.append('\t');
                }
                rows.append('\n');
            }
            Files.writeString(Path.of(args[3] + ".tsv"), rows.toString());
            System.out.println("Query records: " + result.getNumRecords());
        } finally { channel.shutdownNow(); }
    }
}
