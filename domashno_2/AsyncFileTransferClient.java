import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncFileTransferClient {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java AsyncFileTransferClient <file-path>");
            return;
        }
        String filePath = args[0];

        try (AsynchronousSocketChannel clientChannel = AsynchronousSocketChannel.open()) {
            clientChannel.connect(new java.net.InetSocketAddress(SERVER_ADDRESS, PORT)).get(5, TimeUnit.SECONDS);

            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            byte[] fileData = Files.readAllBytes(Paths.get(filePath));

            buffer.put(fileData);
            buffer.flip();

            clientChannel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer buffer) {
                    if (buffer.hasRemaining()) {
                        clientChannel.write(buffer, buffer, this);
                    } else {
                        System.out.println("File sent successfully.");
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer buffer) {
                    System.err.println("Failed to send file to server.");
                    exc.printStackTrace();
                }
            });

        } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
