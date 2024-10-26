import java.io.*;
import java.net.*;
import java.util.Scanner;

public class StudentClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 1234);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            Scanner scanner = new Scanner(System.in);
            String command;

            while (true) {
                System.out.println("Enter a command (add/show/average/exit): ");
                command = scanner.nextLine();

                if (command.equalsIgnoreCase("exit")) {
                    break;
                }

                out.println(command);
                String response;
                while ((response = in.readLine()) != null && !response.isEmpty()) {
                    System.out.println(response);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
