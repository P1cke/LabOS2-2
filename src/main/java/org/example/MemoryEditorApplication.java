package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.util.LinkedList;

public class MemoryEditorApplication extends Application {
    private MyMemory myMemory;
    private Label speedLabel;
    private TextField speedText;
    private LinkedList<Long> accessTimestamps = new LinkedList<>();
    private final long ONE_MINUTE = 60000; // 1 минута в миллисекундах

    @Override
    public void start(Stage stage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        Label pidLabel = new Label("PID:");
        GridPane.setConstraints(pidLabel, 0, 0);
        TextField pidInput = new TextField();
        pidInput.setPromptText("Enter PID");
        GridPane.setConstraints(pidInput, 1, 0);

        Label addressLabel = new Label("Address:");
        GridPane.setConstraints(addressLabel, 0, 1);
        TextField addressInput = new TextField();
        addressInput.setPromptText("Enter Address");
        GridPane.setConstraints(addressInput, 1, 1);

        Label valueLabel = new Label("Value:");
        GridPane.setConstraints(valueLabel, 0, 2);
        TextField valueInput = new TextField();
        valueInput.setPromptText("Enter Value");
        GridPane.setConstraints(valueInput, 1, 2);

        speedLabel = new Label("Average Access Speed (last minute):");
        GridPane.setConstraints(speedLabel, 0, 3);
        speedText = new TextField();
        speedText.setEditable(false);
        speedText.setPromptText("Speed");
        GridPane.setConstraints(speedText, 1, 3);

        Button submitButton = new Button("Submit");
        GridPane.setConstraints(submitButton, 1, 4);
        submitButton.setOnAction(e -> {
            try {
                int pid = Integer.parseInt(pidInput.getText());
                String addressStr = addressInput.getText();
                int valueToWrite = Integer.parseInt(valueInput.getText());

                if (myMemory == null || myMemory.getPid() != pid || !myMemory.getAddressStr().equals(addressStr)) {
                    myMemory = new MyMemory(pid, addressStr);
                }

                int readValue = myMemory.read();
                System.out.println("Read value from address " + addressStr + ": " + readValue);

                // Запись значения в адрес
                myMemory.write(valueToWrite);
                System.out.println("Wrote value " + valueToWrite + " to address " + addressStr);

                // Обновление информации о скорости доступа
                updateAccessSpeed();
            } catch (NumberFormatException ex) {
                System.err.println("Invalid input. Please enter valid integer values.");
            }
        });

        grid.getChildren().addAll(
                pidLabel, pidInput, addressLabel,
                addressInput, valueLabel, valueInput,
                speedLabel, speedText, submitButton);

        Scene scene = new Scene(grid, 400, 250);
        stage.setScene(scene);
        stage.setTitle("Memory Editor");
        stage.show();
    }

    private void updateAccessSpeed() {
        // Проверяем, что у нас есть как минимум две временные метки для рассчета скорости
        if (accessTimestamps.size() < 2) {
            speedText.setText("N/A"); // Если данных недостаточно, выводим N/A
            return;
        }

        // Удаляем все временные метки, которые старше одной минуты
        long currentTime = System.currentTimeMillis();
        while (!accessTimestamps.isEmpty() && currentTime - accessTimestamps.getFirst() > ONE_MINUTE) {
            accessTimestamps.removeFirst();
        }

        // Если после удаления осталось меньше двух меток, опять выводим N/A
        if (accessTimestamps.size() < 2) {
            speedText.setText("N/A");
            return;
        }


        // Рассчитываем среднюю скорость доступа за последнюю минуту
        long firstTimestamp = accessTimestamps.getFirst();
        long lastTimestamp = accessTimestamps.getLast();
        double speed = (double) (accessTimestamps.size() - 1) / ((lastTimestamp - firstTimestamp) / 1000.0);

        // Форматируем скорость доступа в строку
        String formattedSpeed = String.format("%.2f operations/s", speed);

        // Обновляем значение текстового поля с полученной скоростью доступа
        speedText.setText(formattedSpeed);
    }

    // Добавление временной метки операции чтения/записи в список
    private void addAccessTimestamp() {
        accessTimestamps.addLast(System.currentTimeMillis());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
