import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.net.*;

public class Client {

    private static List<String> cart = new ArrayList<>(); // Корзина
    private static JTextArea cartArea; // Область для отображения автомобилей в корзине

    // Подключение к вашей базе данных
    private static final String DB_URL = "jdbc:mysql://it.vshp.online:3306/db_cc3839";
    private static final String DB_USER = "st_cc3839";
    private static final String DB_PASSWORD = "a03c78d94926";

    public static void main(String[] args) {
        JFrame frame = new JFrame("Автосалон");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Создание вкладок (JTabbedPane)
        JTabbedPane tabbedPane = new JTabbedPane();

        // Первая вкладка - "Купить"
        JPanel buyPanel = new JPanel();
        buyPanel.setLayout(new BorderLayout());

        // Панель для ввода автосалона
        JPanel salonPanel = new JPanel();
        salonPanel.add(new JLabel("Выберите ближайший автосалон:"));
        String[] salons = {"Салон 1", "Салон 2", "Салон 3"};
        JComboBox<String> salonComboBox = new JComboBox<>(salons);
        salonPanel.add(salonComboBox);
        buyPanel.add(salonPanel, BorderLayout.NORTH);

        // Панель для карточек с автомобилями
        JPanel carPanel = new JPanel();
        carPanel.setLayout(new FlowLayout()); // Автомобили будут в ряд

        // Получение данных об автомобилях из базы данных
        loadCarsFromDatabase(carPanel);

        buyPanel.add(carPanel, BorderLayout.CENTER);

        // Создание панели для отображения корзины и кнопок
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Создание панели для корзины
        JPanel cartPanel = new JPanel();
        cartArea = new JTextArea(5, 40); // Определяем размеры текстовой области
        cartArea.setEditable(false);

        JScrollPane cartScrollPane = new JScrollPane(cartArea);
        cartScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        cartPanel.add(cartScrollPane);
        centerPanel.add(cartPanel);

        // Кнопки "Купить" и "Очистить корзину"
        JPanel buttonPanel = new JPanel();
        JButton buyButton = new JButton("Купить");
        JButton clearButton = new JButton("Очистить корзину");

        // Настройка кнопок
        buyButton.setPreferredSize(new Dimension(120, 30));
        clearButton.setPreferredSize(new Dimension(120, 30));

        buyButton.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Корзина пуста! Добавьте автомобили перед покупкой.");
            } else {
                String selectedSalon = (String) salonComboBox.getSelectedItem();
                String message = "Покупка в салоне: " + selectedSalon + "\nАвтомобили: " + String.join(", ", cart);

                // Отправляем данные на сервер
                sendToServer(message);

                // Выводим информацию о покупке
                JOptionPane.showMessageDialog(null, "Вы купили: \n" + String.join("\n", cart));
                cart.clear(); // Очищаем корзину после покупки

                updateCartDisplay(); // Обновляем отображение корзины
            }
        });

        clearButton.addActionListener(e -> {
            cart.clear(); // Очищаем корзину
            updateCartDisplay(); // Обновляем отображение корзины
            JOptionPane.showMessageDialog(null, "Корзина очищена.");
        });

        // Добавление кнопок в buttonPanel
        buttonPanel.add(buyButton);
        buttonPanel.add(clearButton);

        // Добавление buttonPanel в centerPanel
        centerPanel.add(buttonPanel);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // 10 пикселей отступа сверху и снизу

        // Добавление centerPanel в buyPanel
        buyPanel.add(centerPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Купить", buyPanel);

        // Вторую вкладку можно добавить позже
        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private static void loadCarsFromDatabase(JPanel carPanel) {
        // Загрузка автомобилей из базы данных
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM autosalon")) {

            while (rs.next()) {
                String carName = rs.getString("name");
                JButton carButton = new JButton(carName);
                carButton.addActionListener(e -> {
                    cart.add(carName);
                    updateCartDisplay();
                });
                carPanel.add(carButton);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateCartDisplay() {
        // Обновление отображения корзины в текстовой области
        cartArea.setText(String.join("\n", cart));
    }

    private static void sendToServer(String message) {
        // Отправка данных о покупке на сервер
        try (Socket socket = new Socket("localhost", 8080);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(message); // Отправка сообщения на сервер
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Не удалось подключиться к серверу: " + e.getMessage());
        }
    }
}