import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        GameController gameController = new GameController(gameView, gameModel);
        // gameView.getGameWindow().pack();
        gameView.getGameWindow().setVisible(true);
    }

}

class GameModel {
    // Game logic here
    private boolean isPlayerTurn;
    private boolean gameStarted;

    public GameModel() {
        gameStarted = false;
    }

    public void gameHasStarted() {
        gameStarted = true;
    }

    public boolean getGameStarted() {
        return gameStarted;
    }
}

class GameView extends JFrame {

    private static final int tilesPerRow = 8;
    private GameModel gameModel;
    private GameWindow gameWindow;
    private JButton[][] gameTiles;

    public GameView (GameModel gameModel) {
        this.gameModel = gameModel;
        gameWindow = new GameWindow();

        SpriteGenerator.loadSprites();

        gameTiles = new JButton[tilesPerRow][tilesPerRow];
        setGameTiles();
    }

    public GameWindow getGameWindow() {
        return gameWindow;
    }

    private void setGameTiles() {
        for (int i = 0; i < tilesPerRow; i++) {
            for (int j = 0; j < tilesPerRow; j++) {
                gameTiles[i][j] = new JButton();
                gameTiles[i][j].setOpaque(false);
                gameTiles[i][j].setBorder(BorderFactory.createEmptyBorder());
                gameTiles[i][j].setContentAreaFilled(false);
                gameTiles[i][j].setBorderPainted(false);
                gameTiles[i][j].setFocusPainted(false);
            }
        }
    }

    public void switchToInstructions(ActionListener actionListener) {
        gameWindow.remove(gameWindow.getButtonScreen());
        JButton instructions = gameWindow.setButtonScreen(ImageLoader.imageToAdd(ImageLoader.instructionScreenImage));
        gameWindow.getButtonScreen().addActionListener(actionListener);
        gameWindow.add(instructions, BorderLayout.CENTER);
        gameWindow.revalidate();
        gameWindow.repaint();

    }

    public void switchToGame() {
        gameWindow.remove(gameWindow.getButtonScreen());
        JPanel game = gameWindow.setGameScreen(ImageLoader.getImageAsBuffered(ImageLoader.gameScreenImage));
        gameWindow.add(game, BorderLayout.CENTER);
        addGameTilesToGame(gameWindow.getGameScreen());
        gameWindow.revalidate();
        gameWindow.repaint();
    }

    private void addGameTilesToGame (JPanel gameScreen) {
        for (int i = 0; i < tilesPerRow; i++) {
            for (int j = 0; j < tilesPerRow; j++) {
                gameScreen.add(gameTiles[i][j]);
            }
        }
    }

    public void receiveTitleSwitchListener(ActionListener actionListener) {
        gameWindow.getButtonScreen().addActionListener(actionListener);
    }

    public void receiveGameTileListener(ActionListener actionListener) {
        for (int i = 0; i < tilesPerRow; i++) {
            for (int j = 0; j < tilesPerRow; j++) {
                gameTiles[i][j].addActionListener(actionListener);
                gameTiles[i][j].setActionCommand(Integer.toString(i) + Integer.toString(j));
            }
        }
    }

    public void paintPlayer(Player player, int x, int y) {
        gameTiles[x][y].setIcon(new ImageIcon(ImageLoader.playerSprite));
    }

    public void removePlayer(int x, int y) {
        gameTiles[x][y].setIcon(null);
    }

}

class GameController {

    private GameView gameView;
    private GameModel gameModel;
    private int tempX, tempY = 0;

    public GameController(GameView view, GameModel model) {
        gameView = view;
        gameModel = model;
        gameView.receiveTitleSwitchListener(new TitleSwitchListener());
        gameView.receiveGameTileListener(new GameTileListener());
    }

    class TitleSwitchListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            gameView.switchToInstructions(new InstructionSwitchListener());
        }
    }

    class InstructionSwitchListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            gameView.switchToGame();
            gameModel.gameHasStarted();
        }
    }

    class GameTileListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // System.out.println(e.getActionCommand());
            if (tempX != 0) {
                gameView.removePlayer(tempX, tempY);
            }
            String temp = e.getActionCommand();
            char x = temp.charAt(0);
            char y = temp.charAt(1);
            tempX = Character.getNumericValue(x);
            tempY = Character.getNumericValue(y);
            Player player = new Player(Character.getNumericValue(x), Character.getNumericValue(y));
            gameView.paintPlayer(player, Character.getNumericValue(x), Character.getNumericValue(y));
        }
    }

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

class GameWindow extends JFrame {

    private JButton buttonScreen;
    private JPanel gameScreen;

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
        setIconImage(new ImageIcon(ImageLoader.frameIconImage).getImage());
        setTitle("Nocturne of Dusk");
        buttonScreen = new JButton();
        buttonScreen = setButtonScreen(ImageLoader.imageToAdd(ImageLoader.titleScreenImage));
        add(buttonScreen);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

    public void switchToGrid() {
        setLayout(new GridLayout(8, 8));
    }

    public JButton setButtonScreen(JLabel image) {
        buttonScreen = new JButton();
        buttonScreen.setBorder(BorderFactory.createEmptyBorder());
        buttonScreen.setOpaque(false);
        buttonScreen.setContentAreaFilled(false);
        buttonScreen.setBorderPainted(false);
        buttonScreen.setFocusPainted(false);
        buttonScreen.add(image, BorderLayout.CENTER);
        return buttonScreen;
    }

    public JButton getButtonScreen() {
        return buttonScreen;
    }

    public JPanel setGameScreen(BufferedImage image) {
        if (image == null)
            System.out.println("Failed to load Game Screen");
        gameScreen = new JPanel() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, ChelonTwoMain.windowSide, ChelonTwoMain.windowSide, this);
            }
        };
        gameScreen.setLayout(new GridLayout(8, 8));
        return gameScreen;
    }

    public JPanel getGameScreen() {
        return gameScreen;
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

    public BufferedImage getSpriteImage() {
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
            ImageLoader.imageToAdd("src/sprites/" + label + ".png");
        }
        spritesLoaded = true;
    }

}

class ImageLoader {

    private static String screenFile = "src/screen_images/";
    public static String frameIconImage = screenFile + "frame_icon.png";
    public static String titleScreenImage = screenFile + "title_screen.png";
    public static String instructionScreenImage = screenFile + "instructions.png";
    public static String gameScreenImage = screenFile + "game_screen.png";
    public static String gameOverScreenImage = "";
    public static String gameWinScreenImage = "";
    public static String playerSprite = "src/sprites/player.png";

    public static JLabel imageToAdd(String filepath) {
        try {
            BufferedImage image = ImageIO.read(new File(filepath));
            return new JLabel(new ImageIcon(image));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return new JLabel();
    }

    public static BufferedImage getImageAsBuffered(String filepath) {
        try {
            BufferedImage image = ImageIO.read(new File(filepath));
            return image;
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}