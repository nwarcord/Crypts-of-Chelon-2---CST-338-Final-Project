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
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Main class. Instantiates the Model, View, and Controller
 * classes. Loads the sprite images for game characters.
 * Global variable for window size.
 */
public class ChelonTwoMain {

    public static final int windowSide = 1280;

    public static void main(String[] args) {

        SpriteGenerator.loadSprites();

        GameModel gameModel = new GameModel();
        GameView gameView = new GameView(gameModel);
        GameController gameController = new GameController(gameView, gameModel);
        gameView.getGameWindow().setVisible(true);
    }

}

/**
 * Model class that performs the game logic and holds
 * the current state of the game.
 * Initializes the level creator and holds an instance to
 * the game board.
 */
class GameModel {

    // maxLevel is number of levels generated until game is won.
    public static final int maxLevel = 6;
    private boolean playerHasAttacked;
    private GameBoard gameBoard;
    private int currentLevel;
    private Player player;

    /**
     * Default constructor
     */
    public GameModel() {
        playerHasAttacked = false;
        currentLevel = 1;
        gameBoard = new GameBoard(LevelCreator.generateLevel(currentLevel));
        player = new Player();
    }

    public Player getPlayer() {
        return player;
    }

    // Returns current monsters on the board as an array.
    public ArrayList<GameCharacter> getMonsters() {
        return new ArrayList<>(gameBoard.getBoard().values());
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    // Advances the level by generating a new one.
    // Game board is reset and player turn is refreshed.
    public void advanceLevel() {
        currentLevel++;
        gameBoard = new GameBoard(LevelCreator.generateLevel(currentLevel));
        player.newTurn();
        playerHasAttacked = false;
    }

    // Determines if the character is adjacent to the tile at the
    // given coordinates.
    private boolean isAdjacentTile(int x, int y, GameCharacter character) {
        int xDifference = Math.abs(x - character.getCoords()[0]);
        int yDifference = Math.abs(y - character.getCoords()[1]);
        return (xDifference < 2 && yDifference < 2 && (xDifference + yDifference) != 0);
    }

    // Checks if there is an entity in an adjacent tile to attack
    public boolean checkAttack(int x, int y, GameCharacter character) {
        return (isAdjacentTile(x, y, character) && gameBoard.isOccupied(x, y));
    }

    /*
     Advances the actions of each monster on the board.
     Attacking is tried first, then each will try to move closer
     to the player. If unable to move closer, they will move in a
     random direction.
    */
    public void monsterTurn() {
        for (GameCharacter monster : gameBoard.getBoard().values()) {
            // Attack if player is adjacent.
            if (isAdjacentTile(player.getCoords()[0], player.getCoords()[1], monster)) {
                player.takeDamage(monster.attack());
                SoundSystem.getPlayerHit().start();
                // If attack kills player, end game
                if (!(player.isAlive()))
                    gameOver();
            }
            else {
                int[] checkMove = moveCloserToPlayer(monster.getCoords()[0], monster.getCoords()[1]);
                // Move closer to player if space is unoccupied.
                if (gameBoard.isOpen(checkMove[0], checkMove[1]))
                    monster.move(checkMove[0], checkMove[1]);
                else {
                    // Random move if nothing else.
                    int[] randomMove = generateRandomMove(monster.getCoords()[0], monster.getCoords()[1]);
                    if (gameBoard.isOpen(randomMove[0], randomMove[1]))
                        monster.move(randomMove[0], randomMove[1]);
                }
            }
            gameBoard.resetBoard();
        }
        player.newTurn();
    }

    // Generates random x and y within bounds of game board.
    // Returned as an array of ints.
    private int[] generateRandomMove(int x, int y) {
        Random random = new Random();
        int randomX = random.nextInt(3);
        int randomY = random.nextInt(3);
        randomX += (x - 1);
        if (randomX == 0 || randomX == 7)
            randomX = x;
        randomY += (y - 1);
        if (randomY == 0 || randomY == 7)
            randomY = y;
        return new int[] {randomX, randomY};
    }


    // Finds the closest tile to the player.
    // Bounds checking is not needed since player
    // cannot move out of bounds.
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

    // If player can attack this turn, deal damage.
    public void playerAttack(int x, int y) {
        if (playerHasAttacked)
            return;
        gameBoard.getCharacterAtCoords(x, y).takeDamage(player.attack());
        SoundSystem.getEnemyHit().start();
        if (!(gameBoard.getCharacterAtCoords(x, y).isAlive())) {
            gameBoard.removeMonster(x, y);
            gameBoard.resetBoard();
        }
        playerHasAttacked = true;
    }

    // If within bounds and unoccupied, moves player to tile.
    public void playerMove(int x, int y) {
        if (x == 0 || x == 7 || y == 0 || y == 7)
            return;
        if (isAdjacentTile(x, y, player) && !(gameBoard.isOccupied(x, y)) && player.ableToMove()) {
            player.move(x, y);
            gameBoard.resetBoard();
        }
    }

    // If player has no more actions to take, end turn.
    public boolean playerTurnOver() {
        return (playerHasAttacked || playerAlone()) && !(player.ableToMove());
    }

    public void resetPlayerTurn() {
        playerHasAttacked = false;
    }

    // Checks if there are any monsters around the player.
    private boolean playerAlone() {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (gameBoard.isOccupied(player.getXPos() + i, player.getYPos() + j))
                    return false;
            }
        }
        return true;
    }

    // Returns true if there is not a monster on the tile
    // AND if the player is not on the tile.
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

/**
 * The game view handles the GUI of the game:
 * Loading screens and updating sprites as
 * the game progresses.
 * During the game proper, creates an array of JButtons
 * that serve as "tiles" where the sprite icons are placed.
 */
class GameView extends JFrame {

    private static final int tilesPerRow = 8;
    private GameModel gameModel;
    private GameWindow gameWindow;
    private JButton[][] gameTiles;

    /**
     * Constructor that initializes a game window object
     * and the game tiles array.
     * @param gameModel holds an instance to the model.
     */
    public GameView (GameModel gameModel) {
        this.gameModel = gameModel;
        gameWindow = new GameWindow();

        gameTiles = new JButton[tilesPerRow][tilesPerRow];
        setGameTiles();
    }

    public GameWindow getGameWindow() {
        return gameWindow;
    }

    // For each tile in the array, initialize a new
    // JButton and set its attributes so that it will
    // be transparent expect for the icon placed on it.
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

    // Removes the title screen and adds a the instructions as a new
    // JButton with a listener.
    public void switchToInstructions(ActionListener actionListener) {
        gameWindow.remove(gameWindow.getButtonScreen());
        JButton instructions = gameWindow.setButtonScreen(ImageLoader.imageToAdd(ImageLoader.instructionScreenImage));
        gameWindow.getButtonScreen().addActionListener(actionListener);
        gameWindow.add(instructions, BorderLayout.CENTER);
        gameWindow.revalidate();
        gameWindow.repaint();

    }

    // Removes instructions and adds the game JPanel to the frame.
    // Adds the game tiles to the JPanel that was just created.
    public void switchToGame() {
        gameWindow.remove(gameWindow.getButtonScreen());
        JPanel game = gameWindow.setGameScreen(ImageLoader.getImageAsBuffered(ImageLoader.gameScreenImage));
        gameWindow.add(game, BorderLayout.CENTER);
        addGameTilesToGame(gameWindow.getGameScreen());
        gameWindow.revalidate();
        gameWindow.repaint();
    }

    // Each game tile JButton is added to the JPanel passed
    // in as a parameter.
    private void addGameTilesToGame (JPanel gameScreen) {
        for (int i = 0; i < tilesPerRow; i++) {
            for (int j = 0; j < tilesPerRow; j++) {
                gameScreen.add(gameTiles[i][j]);
            }
        }
    }

    // Listener methods
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

    // Adds player sprite icon to JButton
    public void drawPlayer(Player player) {
        gameTiles[player.getXPos()][player.getYPos()].setIcon(player.getSprite());
    }

    // Adds monster sprite icons to JButtons
    public void drawMonsters(ArrayList<GameCharacter> monsters) {
        for (GameCharacter monster : monsters) {
            gameTiles[monster.getCoords()[0]][monster.getCoords()[1]].setIcon(monster.getSprite());
        }
    }

    // Resets game tile icons to null if there is no character there.
    // Calls the methods to draw player and monsters at their current positions.
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

    // Following methods reset the JFrame and sets up a new screen
    // depending on game outcome. Screens are JButtons that have
    // listeners for on-click actions.
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

    // Updates the heart sprites in the top-left corner.
    // Actually two instances of the same sprite, first half
    // showing on first tile and second half on the second.
    public void updatePlayerHealth() {
        // Guard block - player out of health.
        if (gameModel.getPlayer().getHealth() < 1)
            return;
        // Align the icons. Left-side for the left and vice-versa.
        // Creates borders so the two halves align to look like a whole.
        gameTiles[0][0].setHorizontalAlignment(JLabel.LEFT);
        gameTiles[0][0].setBorder(new EmptyBorder(0, 11, 75, 17));
        gameTiles[0][1].setHorizontalAlignment(JLabel.RIGHT);
        gameTiles[0][1].setBorder(new EmptyBorder(0, 0, 75, 6));
        gameTiles[0][0].setIcon(ImageLoader.getPlayerHeartsIcon(gameModel.getPlayer().getHealth()));
        gameTiles[0][1].setIcon(ImageLoader.getPlayerHeartsIcon(gameModel.getPlayer().getHealth()));
    }

}

/**
 * Game controller that acts as an observer between the
 * view and the model.
 * Calls methods on each based on the user input and
 * state of the game.
 */
class GameController {

    private GameView gameView;
    private GameModel gameModel;
    private Clip currentTheme;

    /**
     * Constructor gets model and view references, gives view
     * listeners, and starts the intro theme music.
     * @param view reference to the game view
     * @param model reference to the game model
     */
    public GameController(GameView view, GameModel model) {
        gameView = view;
        gameModel = model;
        gameView.receiveTitleSwitchListener(new TitleSwitchListener());
        gameView.receiveGameTileListener(new GameTileListener());
        currentTheme = SoundSystem.getIntro();
        currentTheme.start();
    }

    // Inner class that notifies view to switch from the title screen.
    class TitleSwitchListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            gameView.switchToInstructions(new InstructionSwitchListener());
        }
    }

    // Inner class that notifies view to switch to the game screen.
    // Tells the view to setup game elements and switches music.
    class InstructionSwitchListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            gameView.switchToGame();
            gameView.drawMonsters(gameModel.getMonsters());
            gameView.drawPlayer(gameModel.getPlayer());
            gameView.updatePlayerHealth();
            currentTheme.stop();
            currentTheme = SoundSystem.getGameTheme();
            currentTheme.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    //Inner class that detects user input during game,
    // notifies view and model of updates in the game state.
    class GameTileListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Get tile user clicked and see if it was an enemy or open space.
            // If occupied, attack. Otherwise, move.
            int[] coords = gameModel.getGameBoard().coordsAsInts(e.getActionCommand());
            if (gameModel.checkAttack(coords[0], coords[1], gameModel.getPlayer())) {
                gameModel.playerAttack(coords[0], coords[1]);
                // If all monsters defeated, detect if game won or just level.
                if (gameModel.levelWon()) {
                    if (gameModel.getCurrentLevel() == GameModel.maxLevel) {
                        gameView.switchToGameWin(new GameWinListener());
                        currentTheme.stop();
                        currentTheme = SoundSystem.getGameWin();
                        currentTheme.loop(Clip.LOOP_CONTINUOUSLY);
                    }
                    else
                        gameModel.advanceLevel();
                }
            }
            else
                gameModel.playerMove(coords[0], coords[1]);

            gameView.updateBoard();

            // If the player is out of actions, model processes monster turns.
            if (gameModel.playerTurnOver()) {
                gameModel.monsterTurn();
                // If player out of health, game over.
                if (gameModel.gameOver()) {
                    gameView.switchToGameOver(new GameOverListener());
                    currentTheme.stop();
                    SoundSystem.getNecroLaugh().start();
                }
                gameView.updatePlayerHealth();
                gameModel.resetPlayerTurn();
                gameView.updateBoard();
            }
        }
    }

    // Exit the program when user clicks on game over screen.
    class GameOverListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    // Notify the view to update to the game win screen.
    class GameWinListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            gameView.switchToGameWinTwo(new GameWinTwoListener());
        }
    }

    // Exit the program when user clicks on final game win screen.
    class GameWinTwoListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

}

/**
 * Main JFrame that houses the game GUI.
 * The game view has a reference to it and
 * uses it to represent the game state.
 */
class GameWindow extends JFrame {

    private JButton buttonScreen;
    private JPanel gameScreen;

    /**
     * Default constructor
     */
    public GameWindow () {
        initGameWindow();
    }

    // Called by the constructor to setup the initial frame state.
    private void initGameWindow () {
        // Change to look and feel of local machine.
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

    // Creates a JButton that will serve as title and ending screens.
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

    // Creates a JPanel for the game screen where play takes place.
    // The game background is painted directly onto the panel.
    public JPanel setGameScreen(BufferedImage image) {
        if (image == null)
            System.out.println("Failed to load Game Screen");
        gameScreen = new JPanel() {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, ChelonTwoMain.windowSide, ChelonTwoMain.windowSide, this);
            }
        };
        // Setup layout for the game tiles.
        gameScreen.setLayout(new GridLayout(8, 8));
        return gameScreen;
    }

    public JPanel getGameScreen() {
        return gameScreen;
    }

}

/**
 * Class that holds the positioning of all
 * monsters on the game board. Player is not included.
 */
class GameBoard {

    // Hash map - Key is the character coordinates as a string,
    // value is the character itself.
    private Map<String, GameCharacter> characters = new HashMap<>();
    private int numberOfMonsters;

    /**
     * Constructor that takes an array and calls the fill method.
     * @param startSpawns an array of starting monsters
     */
    public GameBoard (ArrayList<GameCharacter> startSpawns) {
        fillCharacters(startSpawns);
        numberOfMonsters = characters.size();
    }

    // Fills a hash map with the characters from the given array.
    private void fillCharacters(ArrayList<GameCharacter> startSpawns) {
        for (GameCharacter character : startSpawns) {
            characters.put(coordsAsString(character.getCoords()), character);
        }
    }

    // Methods that format coordinates between an int array and
    // and a string with the numbers concatenated.
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

    // Get the character at a given location.
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

    // Updates the character keys (coordinates) in the hash map
    // based on their current coordinates (if they have moved).
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

/**
 * Class that generates the levels of gameplay.
 * Spawn points are predetermined and shuffled to allow
 * for variance in level creation.
 */
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

    // Generates a level based on a given integer value.
    public static ArrayList<GameCharacter> generateLevel(int currentLevel) {
        // Shuffle spawn locations
        resetSpawnLocations();
        // Calculates the number of monsters to spawn based on level.
        int numberOfMonsters = startingMonsters + (monsterIncrease * (currentLevel - 1));
        if (numberOfMonsters > maxMonsters)
            numberOfMonsters = maxMonsters;
        ArrayList<GameCharacter> createdLevel = new ArrayList<>();
        Random random = new Random();
        // Spawns monsters based on stated frequency
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

/**
 * Component that dictates motion methods
 * for the object it is attached to.
 */
interface IMovementComponent {
    int getMoveSpeed();
    int[] getCoords();
    void move(int x, int y);
    boolean ableToMove();
}

/**
 * Methods for health and taking damage.
 */
interface IVitalityComponent {
    int getMaxHealth();
    int getHealth();
    void takeDamage(int damage);
    boolean isAlive();
}

/**
 * Combat methods - dealing damage.
 */
interface ICombatComponent {
    int getAttackPower();
    int attack();
}

/**
 * Abstract class that represents a character in the game world.
 * Uses the movement, combat, and vitality interfaces to dictate
 * the functionality a character will have.
 */
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

    /**
     * Main constructor for characters.
     * @param maxHealth max health of character.
     * @param moveSpeed amount of spaces of movement per turn.
     * @param attackPower damage dealt with single attack.
     * @param xPos x-coordinate.
     * @param yPos y-coordinate.
     * @param sprite sprite icon of character.
     */
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

    /**
     * Copy constructor. Can be used in the future for a generic
     * character spawn factory object, using the passed in
     * character as a prototype.
     * @param gameCharacter character to copy.
     */
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

    // Abstract methods that allow for characters
    // to have unique movement and attack behaviors.
    public abstract void move(int x, int y);

    public abstract int attack();

}

/**
 * Class for player object that is controlled by user.
 * Simple implementation, allowing for 3 space movement and
 * dealing damage equal to attack power.
 */
class Player extends GameCharacter {

    /**
     * Constructor that allows for spawning player at given coordinates.
     * @param x x-coordinate for spawn.
     * @param y y-coordinate for spawn.
     */
    public Player(int x, int y) {
        super(10, 3, 1, x, y, SpriteGenerator.getSprite("player"));
    }

    /**
     * Default constructor
     */
    public Player() {
        super(10, 3, 1, 3, 5, SpriteGenerator.getSprite("player"));
    }

    // Updates player location and decrements their movement counter.
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

/**
 * Monster: Wight (Sentient undead being)
 * Simple implementation with an attack of
 * TWO damage and THREE health.
 */
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

/**
 * Monster: Grave Moth (corrupted insects)
 * Simple implementation with ONE health
 * and attack power of ONE.
 */
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

/**
 * Class that loads the sprites from file and houses
 * them in a hash map for retrieval.
 */
class SpriteGenerator {

    public static Map<String, ImageIcon> spriteList = new HashMap<>();
    private static String[] spriteLabels = new String[] {
        "player",
        "wight",
        "moth"
    };
    private static boolean spritesLoaded = false;

    // Loads the sprites into hash map if they
    // haven't been loaded already.
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

/**
 * Class that loads and houses the images of the game that
 * are not character sprites.
 */
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

    // Returns the image from file in the form of a JLabel
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

    // Returns image as a buffered image
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

/**
 * Class that handles the loading and initialization of
 * sound clips used in the game.
 */
class SoundSystem {

    private static String soundFile = "src/sounds/";
    private static File playerHitFile = new File(soundFile + "player_hit.wav");
    private static File enemyHitFile = new File(soundFile + "enemy_hit.wav");
    private static File necroLaughFile = new File(soundFile + "necro_laugh.wav");
    private static File introFile = new File(soundFile + "intro.wav");
    private static File gameThemeFile = new File(soundFile + "game_theme.wav");
    private static File gameWinFile = new File(soundFile + "game_win.wav");
    private static Clip playerHit, enemyHit, necroLaugh, intro, gameTheme, gameWin;

    // Methods to return the clips that are generated by the makeClip method.
    public static Clip getPlayerHit() {
        return makeSoundEffect(playerHitFile, playerHit);
    }

    public static Clip getEnemyHit() {
        return makeSoundEffect(enemyHitFile, enemyHit);
    }

    public static Clip getNecroLaugh() {
        return makeClip(necroLaughFile, necroLaugh);
    }

    public static Clip getIntro() {
        return makeClip(introFile, intro);
    }

    public static Clip getGameTheme() {
        return makeClip(gameThemeFile, gameTheme);
    }

    public static Clip getGameWin() {
        return makeClip(gameWinFile, gameWin);
    }

    // Opens an audio input stream, audio input file, and creates the clip
    // object with the connection to the stream.
    private static Clip makeClip(File audioFile, Clip clip) {
        try {
            AudioInputStream input = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(input);
        }
        catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
        return clip;
    }

    // Same as above, but this method is used for the sound effects.
    // It uses a different audio stream so that the sound effects can play while the
    // clip using the separate stream is playing.
    private static Clip makeSoundEffect(File audioFile, Clip clip) {
        try {
            AudioInputStream effectInput = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(effectInput);
        }
        catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
        return clip;
    }

}