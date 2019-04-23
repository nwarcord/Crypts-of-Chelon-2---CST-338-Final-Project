import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ChelonTwoMain {

    public static final int windowSide = 1280;

    public static void main(String[] args) {

        // Magic happens here.
        GameModel gameModel = new GameModel();
        GameView gameView = new GameView(gameModel);
        gameView.getGameWindow().pack();
        gameView.getGameWindow().setVisible(true);
    }

}

class GameModel {
    // Game logic here
    private boolean isPlayerTurn;
}

class GameView {

    private GameModel gameModel;
    private GameWindow gameWindow;

    public GameView (GameModel gameModel){
        this.gameModel = gameModel;
        gameWindow = new GameWindow();
        gameWindow.add(new TitleScreen());

        SpriteGenerator.loadSprites();
    }

    public GameWindow getGameWindow() {
        return gameWindow;
    }

}

class GameController {



}

class EndScreen extends JPanel {

    private BufferedImage gameOver;
    private BufferedImage gameWin;

    public EndScreen(boolean ending) {
        pickEnding(ending);
    }

    private void pickEnding(boolean win) {
        if (win) {
            add(new JLabel(new ImageIcon(gameWin)));
        }
        else
            add(new JLabel(new ImageIcon(gameOver)));
    }

}

class TitleScreen extends JPanel {

    public TitleScreen() {

        initTitleScreen();

    }

    private void initTitleScreen() {
        setBackground(Color.GRAY);
    }

}

class GameWindow extends JFrame {

    public GameWindow () {
        initGameWindow();
    }

    private void initGameWindow () {

        try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
        catch(UnsupportedLookAndFeelException |
            ClassNotFoundException |
            InstantiationException |
            IllegalAccessException ignore){}
        setLayout(new BorderLayout());
        setSize(ChelonTwoMain.windowSide, ChelonTwoMain.windowSide);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

    public void switchToGrid() {
        setLayout(new GridLayout(8, 8));
    }

}

class GameBoard {

    public static int xDimension = 6;
    public static int yDimension = 6;
    private int monsters;

    public GameBoard(int monsters) {
        this.monsters = monsters;
    }



}

interface IMovementComponent {
    int getMoveSpeed();
    int[] getCoords();
    boolean move(int x, int y);
    boolean ableToMove();
}

interface IVitalityComponent {
    int getMaxHealth();
    int getHealth();
    void takeDamage(int damage);
    boolean isAlive();
}

interface ICombatComponent {
    int getAttackPower();
    int attack();
}

abstract class GameCharacter implements IMovementComponent, IVitalityComponent, ICombatComponent {

    private int maxHealth;
    private int health;
    private int moveSpeed;
    protected int moveRemaining;
    private int attackPower;
    protected int xPos;
    protected int yPos;
    private boolean alive;
    private BufferedImage sprite;

    public GameCharacter(int maxHealth, int moveSpeed, int attackPower, int xPos, int yPos, BufferedImage sprite) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.moveSpeed = moveSpeed;
        this.moveRemaining = moveSpeed;
        this.attackPower = attackPower;
        this.xPos = xPos;
        this.yPos = yPos;
        alive = true;
        this.sprite = sprite;
        // setSpriteImage();
    }

    public GameCharacter(GameCharacter gameCharacter) {
        maxHealth = gameCharacter.getMaxHealth();
        health = gameCharacter.getMaxHealth();
        moveSpeed = gameCharacter.getMoveSpeed();
        moveRemaining = gameCharacter.getMoveSpeed();
        attackPower = gameCharacter.getAttackPower();
        xPos = gameCharacter.getCoords()[0];
        yPos = gameCharacter.getCoords()[1];
        sprite = gameCharacter.getSpriteImage();
        alive = true;
    }

    public int getMaxHealth() {
        return maxHealth;
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

    public int getAttackPower() {
        return attackPower;
    }

    public boolean isAlive() {
        return alive;
    }

    private void dead() {
        alive = false;
    }

    private BufferedImage getSpriteImage() {
        return sprite;
    }

    public JLabel getSprite() {
        return new JLabel(new ImageIcon(sprite));
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0)
            dead();
    }

    public boolean ableToMove() {
        return moveRemaining > 0;
    }

    public abstract boolean move(int x, int y);

    public abstract int attack();

    // protected abstract void setSpriteImage();

}

class Player extends GameCharacter {

    public Player(int x, int y) {
        super(100, 3, 10, x, y, SpriteGenerator.spriteList.get("player"));
    }

    @Override
    public boolean move(int x, int y) {
        if (ableToMove()) {
            xPos = x;
            yPos = y;
            moveRemaining--;
            return true;
        }
        return false;
    }

    @Override
    public int attack() {
        return getAttackPower();
    }

    /*@Override
    protected void setSpriteImage() {
        try {
            sprite = ImageIO.read(new File("sprites/wight_sprite.png"));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }*/
}

class Wight extends GameCharacter {

    public Wight(int x, int y) {
        super(30, 2, 10, x, y, SpriteGenerator.spriteList.get("wight"));
    }

    @Override
    public boolean move(int x, int y) {
        return false;
    }

    @Override
    public int attack() {
        return 0;
    }
}

class Moth extends GameCharacter {

    public Moth(int x, int y) {
        super(10, 4, 5, x, y, SpriteGenerator.spriteList.get("moth"));
    }

    @Override
    public boolean move(int x, int y) {
        return false;
    }

    @Override
    public int attack() {
        return 0;
    }
}

class SpriteGenerator {

    public static Map<String, BufferedImage> spriteList = new HashMap<>();
    private static String[] spriteLabels = new String[] {
        "player",
        "wight",
        "moth"
    };
    private static boolean spritesLoaded = false;

    public static void loadSprites() {
        if (spritesLoaded)
            return;
        for (String label : spriteLabels) {
            BufferedImage current;
            try {
                current = ImageIO.read(new File(label + ".png"));
                spriteList.put(label, current);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        spritesLoaded = true;
    }

}