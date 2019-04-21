import java.awt.*;
import javax.swing.*;

public class ChelonTwoMain {

    public static void main(String[] args) {

        // Magic happens here.
        GameWindow gameWindow = new GameWindow();
        gameWindow.setVisible(true);
    }

}


class GameWindow extends JFrame {

    public GameWindow () {
        /*
        try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
        catch(UnsupportedLookAndFeelException |
            ClassNotFoundException |
            InstantiationException |
            IllegalAccessException ignore){}
        JFrame GameWindow = new JFrame();
        GameWindow.setSize(1280, 1280);
        GameWindow.setLayout(new GridLayout(8, 8));
        GameWindow.setLocationRelativeTo(null);
        GameWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        */

        initGameWindow();

    }

    private void initGameWindow () {

        try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
        catch(UnsupportedLookAndFeelException |
            ClassNotFoundException |
            InstantiationException |
            IllegalAccessException ignore){}
        setSize(1280, 1280);
        setLayout(new GridLayout(8, 8));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

}

abstract class GameCharacter {

    private int maxHealth;
    private int health;
    private int moveSpeed;
    private int attackPower;
    private int xPos;
    private int yPos;
    private boolean alive;

    GameCharacter(int health, int moveSpeed, int attackPower, int xPos, int yPos, int maxHealth) {
        this.health = health;
        this.moveSpeed = moveSpeed;
        this.attackPower = attackPower;
        this.xPos = xPos;
        this.yPos = yPos;
        this.maxHealth = maxHealth;
        alive = true;
    }

    public int getHealth() {
        return health;
    }

    public int getMoveSpeed() {
        return moveSpeed;
    }

    public int[] getCoords() {
        return new int[] {xPos, yPos};
    }

    public boolean isAlive() {
        return alive;
    }

    private void setHealth(int difference) {
        if (difference > 0)
            health += difference;
        else
            health -= difference;
    }

    public void takeDamage(int damage) {
        setHealth(damage);
        if (health <= 0)
            dead();
    }

    public void healHealth(int heal) {
        setHealth(heal);
        if (health > maxHealth)
            health = maxHealth;
    }

    private void dead() {
        alive = false;
    }

    public int attack() {
        return attackPower;
    }

}

class Wight extends GameCharacter {

    public Wight() {
        super(100,2, 5, 0, 2, 100);
    }

}