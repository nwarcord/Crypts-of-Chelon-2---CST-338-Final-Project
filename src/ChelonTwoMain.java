/*--------------------------/
 Nathan Warren-Acord
 CST 338 - Final Project

   --Nocturne of Dusk--
 -The Crypts of Chelon II-

 A simple rogue-like game
 written in Java.
/--------------------------*/

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ChelonTwoMain {

    public static final int windowSide = 1280;

    public static void main(String[] args) {

        SpriteGenerator.loadSprites();

        // Magic happens here.
        GameModel gameModel = new GameModel();
        GameView gameView = new GameView(gameModel);
        GameController gameController = new GameController(gameView, gameModel);
        // gameView.getGameWindow().pack();
        gameView.getGameWindow().setVisible(true);
    }

}

class GameModel {

    public static final int maxLevel = 5;
    private boolean playerHasAttacked;
    private boolean gameStarted;
    private GameBoard gameBoard;
    private int currentLevel;
    private Player player;

    public GameModel() {
        // isPlayerTurn = true;
        playerHasAttacked = false;
        gameStarted = false;
        currentLevel = 1;
        gameBoard = new GameBoard(LevelCreator.generateLevel(currentLevel));
        player = new Player();
    }

    public void gameHasStarted() {
        gameStarted = true;
    }

    public boolean getGameStarted() {
        return gameStarted;
    }

    public Player getPlayer() {
        return player;
    }

    public ArrayList<GameCharacter> getMonsters() {
        return new ArrayList<>(gameBoard.getBoard().values());
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void advanceLevel() {
        currentLevel++;
        gameBoard = new GameBoard(LevelCreator.generateLevel(currentLevel));
        player.newTurn();
        playerHasAttacked = false;
    }

    public boolean isAdjacentTile(int x, int y, GameCharacter character) {
        int xDifference = Math.abs(x - character.getCoords()[0]);
        int yDifference = Math.abs(y - character.getCoords()[1]);
        return (xDifference < 2 && yDifference < 2 && (xDifference + yDifference) != 0);
    }

    public boolean checkAttack(int x, int y, GameCharacter character) {
        return (isAdjacentTile(x, y, character) && gameBoard.isOccupied(x, y));
    }

    public void monsterTurn() {
        for (GameCharacter monster : gameBoard.getBoard().values()) {
            if (isAdjacentTile(player.getCoords()[0], player.getCoords()[1], monster)) {
                player.takeDamage(monster.attack());
                if (!(player.isAlive()))
                    gameOver();
            }
            else {
                int[] checkMove = moveCloserToPlayer(monster.getCoords()[0], monster.getCoords()[1]);
                if (gameBoard.isOpen(checkMove[0], checkMove[1]))
                    monster.move(checkMove[0], checkMove[1]);
                else {
                    int[] randomMove = generateRandomMove(monster.getCoords()[0], monster.getCoords()[1]);
                    if (gameBoard.isOpen(randomMove[0], randomMove[1]))
                        monster.move(randomMove[0], randomMove[1]);
                }
            }
            gameBoard.resetBoard();
        }
        player.newTurn();
    }

    private int[] generateRandomMove(int x, int y) {
        Random random = new Random();
        int randomX = random.nextInt(3);
        int randomY = random.nextInt(3);
        randomX += (x - 1);
        if (randomX == 0 || randomX == 7)
            randomX = x;
        if (randomY == 0 || randomY == 7)
            randomY = y;
        return new int[] {randomX, randomY};
    }

    private int[] moveCloserToPlayer(int x, int y) {
        int playerX = player.getCoords()[0];
        int playerY = player.getCoords()[1];
        int closerX = x;
        int closerY = y;
        if (playerX > x)
            closerX = x + 1;
        else if (playerX < x)
            closerX = x - 1;
        if (playerY > y)
            closerY = y + 1;
        else if (playerY < y)
            closerY = y - 1;
        return new int[]{closerX, closerY};
    }

    public void playerAttack(int x, int y) {
        if (playerHasAttacked)
            return;
        gameBoard.getCharacterAtCoords(x, y).takeDamage(player.attack());
        if (!(gameBoard.getCharacterAtCoords(x, y).isAlive())) {
            gameBoard.removeMonster(x, y);
            gameBoard.resetBoard();
        }
        playerHasAttacked = true;
    }

    public void playerMove(int x, int y) {
        if (x == 0 || x == 7 || y == 0 || y == 7)
            return;
        if (isAdjacentTile(x, y, player) && !(gameBoard.isOccupied(x, y)) && player.ableToMove()) {
            player.move(x, y);
            gameBoard.resetBoard();
        }
    }

    public boolean playerTurnOver() {
        return (playerHasAttacked || playerAlone()) && !(player.ableToMove());
    }

    public void resetPlayerTurn() {
        // player.newTurn();
        playerHasAttacked = false;
    }

    private boolean playerAlone() {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (gameBoard.isOccupied(player.getXPos() + i, player.getYPos() + j))
                    return false;
            }
        }
        return true;
    }

    public boolean tileEmpty(int x, int y) {
        if (player.getXPos() == x && player.getYPos() == y)
            return false;
        return gameBoard.isOpen(x, y);
    }

    public boolean gameOver() {
        return !player.isAlive();
    }

    public boolean levelWon() {
        return gameBoard.getNumberOfMonsters() == 0;
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
                gameTiles[i][j].setActionCommand(Integer.toString(i) + j);
            }
        }
    }

    public void drawPlayer(Player player) {
        gameTiles[player.getXPos()][player.getYPos()].setIcon(player.getSprite());
    }

    public void drawMonsters(ArrayList<GameCharacter> monsters) {
        for (GameCharacter monster : monsters) {
            gameTiles[monster.getCoords()[0]][monster.getCoords()[1]].setIcon(monster.getSprite());
        }
    }

    public void updateBoard() {
        for (int i = 1; i < tilesPerRow; i++) {
            for (int j = 1; j < tilesPerRow; j++) {
                if (gameModel.tileEmpty(i, j)) {
                    gameTiles[i][j].setIcon(null);
                }
            }
        }
        drawPlayer(gameModel.getPlayer());
        drawMonsters(gameModel.getMonsters());
    }

    public void switchToGameOver(ActionListener actionListener) {
        gameWindow.getContentPane().removeAll();
        JButton gameOver = gameWindow.setButtonScreen(ImageLoader.imageToAdd(ImageLoader.gameOverScreenImage));
        gameWindow.getButtonScreen().addActionListener(actionListener);
        gameWindow.add(gameOver, BorderLayout.CENTER);
        gameWindow.revalidate();
        gameWindow.repaint();
    }

    public void switchToGameWin(ActionListener actionListener) {
        gameWindow.getContentPane().removeAll();
        JButton gameWin = gameWindow.setButtonScreen(ImageLoader.imageToAdd(ImageLoader.gameWinScreenOneImage));
        gameWindow.getButtonScreen().addActionListener(actionListener);
        gameWindow.add(gameWin, BorderLayout.CENTER);
        gameWindow.revalidate();
        gameWindow.repaint();
    }

    public void switchToGameWinTwo(ActionListener actionListener) {
        gameWindow.getContentPane().removeAll();
        JButton gameWin = gameWindow.setButtonScreen(ImageLoader.imageToAdd(ImageLoader.gameWinScreenTwoImage));
        gameWindow.getButtonScreen().addActionListener(actionListener);
        gameWindow.add(gameWin, BorderLayout.CENTER);
        gameWindow.revalidate();
        gameWindow.repaint();
    }

    public void updatePlayerHealth() {
        if (gameModel.getPlayer().getHealth() < 1)
            return;
        gameTiles[0][0].setHorizontalAlignment(JLabel.LEFT);
        gameTiles[0][0].setBorder(new EmptyBorder(0, 11, 75, 17));
        gameTiles[0][1].setHorizontalAlignment(JLabel.RIGHT);
        gameTiles[0][1].setBorder(new EmptyBorder(0, 0, 75, 6));
        gameTiles[0][0].setIcon(ImageLoader.getPlayerHeartsIcon(gameModel.getPlayer().getHealth()));
        gameTiles[0][1].setIcon(ImageLoader.getPlayerHeartsIcon(gameModel.getPlayer().getHealth()));
    }

}

class GameController {

    private GameView gameView;
    private GameModel gameModel;

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
            gameView.drawMonsters(gameModel.getMonsters());
            gameView.drawPlayer(gameModel.getPlayer());
            gameView.updatePlayerHealth();
        }
    }

    class GameTileListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] coords = gameModel.getGameBoard().coordsAsInts(e.getActionCommand());
            if (gameModel.checkAttack(coords[0], coords[1], gameModel.getPlayer())) {
                gameModel.playerAttack(coords[0], coords[1]);
                if (gameModel.levelWon()) {
                    if (gameModel.getCurrentLevel() == GameModel.maxLevel)
                        gameView.switchToGameWin(new GameWinListener());
                    else
                        gameModel.advanceLevel();
                }
            }
            else
                gameModel.playerMove(coords[0], coords[1]);

            gameView.updateBoard();

            if (gameModel.playerTurnOver()) {
                gameModel.monsterTurn();
                if (gameModel.gameOver())
                    gameView.switchToGameOver(new GameOverListener());
                gameView.updatePlayerHealth();
                gameModel.resetPlayerTurn();
                gameView.updateBoard();
            }
        }
    }

    class GameOverListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    class GameWinListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            gameView.switchToGameWinTwo(new GameWinTwoListener());
        }
    }

    class GameWinTwoListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
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

    private Map<String, GameCharacter> characters = new HashMap<>();
    private int numberOfMonsters;

    public GameBoard (ArrayList<GameCharacter> startSpawns) {
        fillCharacters(startSpawns);
        numberOfMonsters = characters.size();
    }

    private void fillCharacters(ArrayList<GameCharacter> startSpawns) {
        for (GameCharacter character : startSpawns) {
            characters.put(coordsAsString(character.getCoords()), character);
        }
    }

    public String coordsAsString (int[] coords) {
        if (coords.length != 2)
            throw new ArrayIndexOutOfBoundsException();
        String stringX = Integer.toString(coords[0]);
        String stringY = Integer.toString(coords[1]);
        return stringX + stringY;
    }

    public int[] coordsAsInts (String coords) {
        return new int[] {Character.getNumericValue(coords.charAt(0)),
            Character.getNumericValue(coords.charAt(1))};
    }

    public GameCharacter getCharacterAtCoords(int x, int y) {
        String coords = coordsAsString(new int[] {x, y});
        return characters.get(coords);
    }

    public boolean isOccupied(int x, int y) {
        return characters.containsKey(coordsAsString(new int[] {x, y}));
    }

    public boolean isOpen(int x, int y) {
        return !characters.containsKey(coordsAsString(new int[] {x, y}));
    }

    public int getNumberOfMonsters() {
        return numberOfMonsters;
    }

    public void removeMonster(int x, int y) {
        numberOfMonsters--;
        characters.remove(coordsAsString(new int[]{x, y}));
    }

    public void resetBoard() {
        Map<String, GameCharacter> updatedBoard = new HashMap<>();
        for (GameCharacter character : characters.values())
            updatedBoard.put(coordsAsString(character.getCoords()), character);
        characters = updatedBoard;
    }

    public Map<String, GameCharacter> getBoard() {
        return characters;
    }

}

class LevelCreator {

    private static int startingMonsters = 3;
    private static int monsterIncrease = 1;
    private static int maxMonsters = 8;
    private static int mothFrequency = 75;
    private static int wightFrequency = 100 - mothFrequency;
    private static ArrayList<int[]> spawnLocations = new ArrayList<int[]>() {{
        add(new int[]{1, 1});
        add(new int[]{3, 1});
        add(new int[]{4, 2});
        add(new int[]{6, 5});
        add(new int[]{2, 2});
        add(new int[]{1, 3});
        add(new int[]{5, 2});
        add(new int[]{6, 6});
    }};

    public static ArrayList<GameCharacter> generateLevel(int currentLevel) {
        resetSpawnLocations();
        int numberOfMonsters = startingMonsters + (monsterIncrease * (currentLevel - 1));
        if (numberOfMonsters > maxMonsters)
            numberOfMonsters = maxMonsters;
        ArrayList<GameCharacter> createdLevel = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < numberOfMonsters; i++) {
            int randomMonster = random.nextInt(101);
            if (randomMonster <= mothFrequency)
                createdLevel.add(new Moth(spawnLocations.get(i)[0], spawnLocations.get(i)[1]));
            else
                createdLevel.add(new Wight(spawnLocations.get(i)[0], spawnLocations.get(i)[1]));
        }
        return createdLevel;
    }

    private static void resetSpawnLocations() {
        Collections.shuffle(spawnLocations);
    }
}

interface IMovementComponent {
    int getMoveSpeed();
    int[] getCoords();
    void move(int x, int y);
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
    private ImageIcon sprite;

    public GameCharacter(int maxHealth, int moveSpeed, int attackPower, int xPos, int yPos, ImageIcon sprite) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.moveSpeed = moveSpeed;
        this.moveRemaining = moveSpeed;
        this.attackPower = attackPower;
        this.xPos = xPos;
        this.yPos = yPos;
        alive = true;
        this.sprite = sprite;
    }

    public GameCharacter(GameCharacter gameCharacter) {
        maxHealth = gameCharacter.getMaxHealth();
        health = gameCharacter.getMaxHealth();
        moveSpeed = gameCharacter.getMoveSpeed();
        moveRemaining = gameCharacter.getMoveSpeed();
        attackPower = gameCharacter.getAttackPower();
        xPos = gameCharacter.getCoords()[0];
        yPos = gameCharacter.getCoords()[1];
        sprite = gameCharacter.getSprite();
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

    public ImageIcon getSprite() {
        return sprite;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0)
            dead();
    }

    public boolean ableToMove() {
        return moveRemaining > 0;
    }

    public void newTurn() {
        moveRemaining = moveSpeed;
    }

    public abstract void move(int x, int y);

    public abstract int attack();

}

class Player extends GameCharacter {

    public Player(int x, int y) {
        super(10, 3, 1, x, y, SpriteGenerator.getSprite("player"));
    }

    public Player() {
        super(10, 3, 1, 3, 5, SpriteGenerator.getSprite("player"));
    }

    @Override
    public void move(int x, int y) {
        if (ableToMove()) {
            xPos = x;
            yPos = y;
            moveRemaining--;
        }
    }

    @Override
    public int attack() {
        return getAttackPower();
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }
}

class Wight extends GameCharacter {

    public Wight(int x, int y) {
        super(3, 1, 2, x, y, SpriteGenerator.spriteList.get("wight"));
    }

    @Override
    public void move(int x, int y) {
        xPos = x;
        yPos = y;
    }

    @Override
    public int attack() {
        return getAttackPower();
    }
}

class Moth extends GameCharacter {

    public Moth(int x, int y) {
        super(1, 1, 1, x, y, SpriteGenerator.spriteList.get("moth"));
    }

    @Override
    public void move(int x, int y) {
        xPos = x;
        yPos = y;
    }

    @Override
    public int attack() {
        return getAttackPower();
    }
}

class SpriteGenerator {

    public static Map<String, ImageIcon> spriteList = new HashMap<>();
    private static String[] spriteLabels = new String[] {
        "player",
        "wight",
        "moth"
    };
    private static boolean spritesLoaded = false;

    public static void loadSprites() {
        if (spritesLoaded)
            return;
        String spriteFile = "src/sprites/";
        for (String label : spriteLabels) {
            spriteList.put(label, new ImageIcon(spriteFile + label + ".png"));
        }
        spritesLoaded = true;
    }

    public static ImageIcon getSprite(String sprite) {
        return spriteList.get(sprite);
    }

}

class ImageLoader {

    private static String screenFile = "src/screen_images/";
    public static String frameIconImage = screenFile + "frame_icon.png";
    public static String titleScreenImage = screenFile + "title_screen.png";
    public static String instructionScreenImage = screenFile + "instructions.png";
    public static String gameScreenImage = screenFile + "game_screen.png";
    public static String gameOverScreenImage = screenFile + "game_over.png";
    public static String gameWinScreenOneImage = screenFile + "win_screen_one.png";
    public static String gameWinScreenTwoImage = screenFile + "win_screen_two.png";
    private static ArrayList<String> heartValues =
        new ArrayList<>(Arrays.asList(
            "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"));

    public static ImageIcon getPlayerHeartsIcon(int health) {
        return new ImageIcon(screenFile + heartValues.get(health - 1) + "_hearts.png");
    }

    public static JLabel imageToAdd(String filepath) {
        try {
            BufferedImage image = ImageIO.read(new File(filepath));
            return new JLabel(new ImageIcon(image));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return new JLabel();
    }

    public static BufferedImage getImageAsBuffered(String filepath) {
        try {
            return ImageIO.read(new File(filepath));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}