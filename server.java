import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;



class StudentServer {
    private static final String FILE_PATH = "students.txt";
    private List<Student> students = new ArrayList<>();

    public StudentServer() {
        loadFromFile();
    }
    private void loadFromFile() {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            try {
                file.createNewFile();
                System.out.println("File students.txt created.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String name = parts[0];
                List<Double> grades = new ArrayList<>();
                for (int i = 1; i < parts.length; i++) {
                    grades.add(Double.parseDouble(parts[i]));
                }
                students.add(new Student(name, grades));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Student student : students) {
                bw.write(student.name);
                for (Double grade : student.grades) {
                    bw.write("," + grade);
                }
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized String addStudent(String name, List<Double> grades) {
        students.add(new Student(name, grades));
        saveToFile();
        return "Student added successfully!";
    }

    public synchronized String showAllGrades() {
        StringBuilder sb = new StringBuilder();
        for (Student student : students) {
            sb.append("Student: ").append(student.name).append(", Grades: ").append(student.grades).append("\n");
        }
        return sb.length() > 0 ? sb.toString() : "No students found.";
    }

    public synchronized String calculateAverage(String name) {
        for (Student student : students) {
            if (student.name.equalsIgnoreCase(name)) {
                return "Average grade for " + name + ": " + student.calculateAverage();
            }
        }
        return "Student not found.";
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        StudentServer server = new StudentServer();
        System.out.println("Server is running...");

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new ClientHandler(socket, server)).start();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private StudentServer server;

    public ClientHandler(Socket socket, StudentServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String request;
            while ((request = in.readLine()) != null) {
                String[] parts = request.split(" ");
                String command = parts[0];

                if (command.equalsIgnoreCase("add")) {
                    String name = parts[1];
                    List<Double> grades = new ArrayList<>();
                    for (int i = 2; i < parts.length; i++) {
                        grades.add(Double.parseDouble(parts[i]));
                    }
                    out.println(server.addStudent(name, grades));
                } else if (command.equalsIgnoreCase("show")) {
                    out.println(server.showAllGrades());
                } else if (command.equalsIgnoreCase("average")) {
                    String name = parts[1];
                    out.println(server.calculateAverage(name));
                } else {
                    out.println("Invalid command.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
