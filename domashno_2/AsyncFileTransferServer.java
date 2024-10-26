import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncFileTransferServer {

    private static final int PORT = 12345;
    private static final String SAVE_DIR = "./";
    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(4);

        try (AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open()) {
            serverChannel.bind(new java.net.InetSocketAddress(PORT));

            System.out.println("Server is listening on port " + PORT);

            serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel clientChannel, Void attachment) {
                    serverChannel.accept(null, this);
                    handleClient(clientChannel);
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    System.err.println("Failed to accept a connection.");
                    exc.printStackTrace();
                }
            });

            System.in.read();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    private static void handleClient(AsynchronousSocketChannel clientChannel) {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        clientChannel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                if (result == -1) {
                    try {
                        clientChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                buffer.flip();
                File file = new File(SAVE_DIR + "received_file_" + System.currentTimeMillis());
                try (FileOutputStream fileOutputStream = new FileOutputStream(file, true)) {
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    fileOutputStream.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    buffer.clear();
                    clientChannel.read(buffer, buffer, this);  // Continue reading from client
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buffer) {
                System.err.println("Failed to read data from client.");
                exc.printStackTrace();
            }
        });
    }
}
