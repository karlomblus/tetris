package tetrispackage;

import chati_leiutis.Klient;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class PrivateChat {
    private TextArea chatWindow;
    private TextField writeArea;
    private Klient client;
    TextArea messagearea = new TextArea();
    private final int width = 140;
    VBox chatArea = new VBox();


    public PrivateChat(Klient client) {
        this.client = client;
        chatWindow = messagearea;
        //muudan aknad mitteklikitavaks
        messagearea.setEditable(false);
        messagearea.setWrapText(true);
        messagearea.setPrefSize(140, 300);
        messagearea.setPromptText("Messages...");
        messagearea.setMouseTransparent(true);
        messagearea.setFocusTraversable(false);

        TextField writearea = new TextField();
        this.writeArea = writearea;
        writearea.setPromptText("Type your message here...");
        writearea.setPrefWidth(200);
        writearea.setFocusTraversable(false);


        chatArea.getChildren().addAll(messagearea, writearea);
        writearea.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                try {
                    System.out.println("Trying to send message");
                    client.sendSomething(105);
                    chatWindow.appendText(client.getNimi() + ">> " + writearea.getText() + "\n");
                } catch (Exception error) {
                    messagearea.appendText("Socket kinni. Ei saanud saata.");
                }
                writearea.clear();
            }
        });
    }

    public void addNewMessage(String name, String message) {
        chatWindow.appendText(name + ">> " + message + "\n");
    }

    public String sendMessageandclearMP() {
        String toReturn = writeArea.getText();
        return toReturn;
    }

    public void opponentLeft() {
        chatWindow.appendText("--------------- " + "\n" + "Your opponent has left the game, closing after 5 seconds...");
        //todo sulgemine pärast ootamist?
    }

    public VBox getChatArea() {
        return chatArea;
    }

    public TextArea getMessagearea() {
        return messagearea;
    }

    public void keyPressSendingFailed() {
        messagearea.appendText("Socket closed. Keypress sending failed!");
    }

    public int getWidth() {
        return width;
    }
}

