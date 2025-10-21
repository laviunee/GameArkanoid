import Engine.GameEngine;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting GAME...");
        Application.launch(GameEngine.class, args);
    }
}