import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class Server {
    private static final String DB_URL = "jdbc:mysql://it.vshp.online:3306/db_cc3839";
    private static final String DB_USER = "st_cc3839";
    private static final String DB_PASSWORD = "a03c78d94926";

    static int net = 0;

    private static JTextArea chatArea;
    private static JTextField chatInput;

    public static void main(String[] args) {

        JFrame frame = new JFrame("Автосалон");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(30, 30, 30));
        tabbedPane.setForeground(Color.WHITE);


        JPanel addCarPanel = new JPanel(new GridBagLayout());
        addCarPanel.setBackground(new Color(40, 40, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel carLabel = new JLabel("Название машины:");
        JTextField carField = new JTextField(15);
        JLabel countLabel = new JLabel("Количество:");
        JTextField countField = new JTextField(5);
        JButton addButton = new JButton("Добавить машину");


        setLabelStyle(carLabel);
        setLabelStyle(countLabel);
        setFieldStyle(carField);
        setFieldStyle(countField);
        setButtonStyle(addButton);

        gbc.gridx = 0; gbc.gridy = 0;
        addCarPanel.add(carLabel, gbc);
        gbc.gridx = 1;
        addCarPanel.add(carField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        addCarPanel.add(countLabel, gbc);
        gbc.gridx = 1;
        addCarPanel.add(countField, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        addCarPanel.add(addButton, gbc);

        addButton.addActionListener(e -> {
            String carName = carField.getText();
            int carCount = Integer.parseInt(countField.getText());
            addCarToDatabase(carName, carCount);
            JOptionPane.showMessageDialog(frame, "Машина добавлена на продажу.");
        });


        JPanel carListPanel = new JPanel(new BorderLayout());
        carListPanel.setBackground(new Color(40, 40, 40));

        JTextArea carListArea = new JTextArea();
        carListArea.setEditable(false);
        carListArea.setBackground(new Color(30, 30, 30));
        carListArea.setForeground(Color.WHITE);
        carListArea.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton refreshButton = new JButton("Обновить список");
        setButtonStyle(refreshButton);

        carListPanel.add(new JScrollPane(carListArea), BorderLayout.CENTER);
        carListPanel.add(refreshButton, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> {
            carListArea.setText(getCarListFromDatabase());
        });


        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(new Color(40, 40, 40));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(30, 30, 30));
        chatArea.setForeground(Color.WHITE);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));

        chatInput = new JTextField();
        chatInput.setBackground(new Color(30, 30, 30));
        chatInput.setForeground(Color.WHITE);
        chatInput.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton sendButton = new JButton("Отправить");
        setButtonStyle(sendButton);

        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatPanel.add(chatInput, BorderLayout.SOUTH);
        chatPanel.add(sendButton, BorderLayout.EAST);

        sendButton.addActionListener(e -> {
            String message = chatInput.getText();
            if (!message.isEmpty()) {
                chatArea.append("Сервер: " + message + "\n");
                sendMessageToClient(message);
                chatInput.setText("");
            }
        });


        tabbedPane.addTab("Добавить машину", addCarPanel);
        tabbedPane.addTab("Список машин", carListPanel);
        tabbedPane.addTab("Чат с клиентом", chatPanel);


        frame.add(tabbedPane);
        frame.setVisible(true);


        new Thread(() -> {
            startServer();
        }).start();
    }


    private static void setLabelStyle(JLabel label) {
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));
    }


    private static void setFieldStyle(JTextField field) {
        field.setBackground(new Color(30, 30, 30));
        field.setForeground(Color.WHITE);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
    }


    private static void setButtonStyle(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(70, 130, 180));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50)));
    }

    private static void addCarToDatabase(String carName, int carCount) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO autosalon (name, count) VALUES (?, ?)")) {

            statement.setString(1, carName);
            statement.setInt(2, carCount);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(null, "Машина успешно добавлена.");
            } else {
                JOptionPane.showMessageDialog(null, "Не удалось добавить машину.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ошибка базы данных: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    private static String getCarListFromDatabase() {
        StringBuilder carList = new StringBuilder();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement("SELECT name, count FROM autosalon")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                int count = resultSet.getInt("count");
                carList.append(name).append(": ").append(count).append(" машин\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return carList.toString();
    }

    private static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            chatArea.append("Сервер запущен. Ожидание подключения клиента...\n");

            Socket clientSocket = serverSocket.accept();
            chatArea.append("Клиент подключился.\n");

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            new Thread(() -> {
                try {
                    String clientMessage;
                    while ((clientMessage = in.readLine()) != null) {
                        chatArea.append("Клиент: " + clientMessage + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendMessageToClient(String message) {
    }
}
