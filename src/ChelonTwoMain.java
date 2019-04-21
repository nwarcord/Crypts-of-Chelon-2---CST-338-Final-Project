import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ChelonTwoMain {

    public static final int windowSide = 1280;

    public static void main(String[] args) {

        // Magic happens here.
        GameModel gameModel = new GameModel();
        GameView gameView = new GameView(gameModel);
        gameView.getGameWindow().setVisible(true);
    }

}

class GameModel {

}

class GameView {

    private GameModel gameModel;
    private GameWindow gameWindow;

    public GameView (GameModel gameModel){
        this.gameModel = gameModel;
        gameWindow = new GameWindow();
        gameWindow.add(new TitleScreen());
    }

    public GameWindow getGameWindow() {
        return gameWindow;
    }

}

class GameController {

}

class EndScreen extends JPanel {

    private Image gameOver;
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

class GameCharacter {

    private int maxHealth;
    private int health;
    private int moveSpeed;
    private int attackPower;
    private int xPos;
    private int yPos;
    private boolean alive;
    private BufferedImage sprite;

    public GameCharacter(int health, int moveSpeed, int attackPower,
                         int xPos, int yPos, int maxHealth, BufferedImage sprite) {
        this.health = health;
        this.moveSpeed = moveSpeed;
        this.attackPower = attackPower;
        this.xPos = xPos;
        this.yPos = yPos;
        this.maxHealth = maxHealth;
        this.sprite = sprite;
        alive = true;
    }

    public GameCharacter(GameCharacter gameCharacter) {
        this.health = gameCharacter.getMaxHealth();
        this.maxHealth = gameCharacter.getMaxHealth();
        this.moveSpeed = getMoveSpeed();
        this.attackPower = attack();
        this.xPos = getCoords()[0];
        this.yPos = getCoords()[1];
        this.sprite = getSpriteImage();
        alive = true;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
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

    private BufferedImage getSpriteImage() {
        return sprite;
    }

    public int attack() {
        return attackPower;
    }

    public JLabel getSprite() {
        return new JLabel(new ImageIcon(sprite));
    }

}
/*
class Wight extends GameCharacter {

    BufferedImage sprite;

    public Wight() {
        super(100,2, 5, 0, 2, 100);
    }

    @Override
    public JLabel getSprite() {
        return new JLabel(new ImageIcon(sprite));
    }
}
*/

class CharacterSpawner {

    private GameCharacter player;
    private BufferedImage playerSprite;
    private GameCharacter wight;
    private BufferedImage wightSprite;
    private GameCharacter moth;
    private BufferedImage mothSprite;

    public CharacterSpawner() {
        setWightSprite();
        player = new GameCharacter(100, 3, 10, 7, 4, 100, playerSprite);
        wight = new GameCharacter(30, 2, 10, 4, 1, 30, wightSprite);
        moth = new GameCharacter(10, 5, 5, 2, 1, 10, mothSprite);
    }

    private void setCharacterSprite() {

    }

    private void setWightSprite() {
        // Image sprite = new ImageIcon("sprites/wight_sprite.png").getImage();
        try {
            wightSprite = ImageIO.read(new File("sprites/wight_sprite.png"));
        }
        catch (IOException ignore){}
    }

    public GameCharacter getPlayer() {
        return player;
    }

    public GameCharacter spawnWight() {
        return new GameCharacter(wight);
    }

    public GameCharacter spawnMoth() {
        return new GameCharacter(moth);
    }

}