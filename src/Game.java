import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;



public class Game extends JPanel {
    private static final int MAX_PLAYERS = 2;
    private static final int GRID_SIZE = 12;
    private static final int CELL_SIZE = 60;
    private static final int MARKETS = 5;
    private static final int LOST_ITEMS = 13;
    private static final int TREASURES = 8;
    private static final int WALL_HOUSES = 5;
    private static final int TRAPS = 5;
    private static final int CASTLE = 1;
    private int currentPlayer = 0;
    private final int[] playerXPositions;
    private final int[] playerYPositions;
    private final int[] movesLeft;
    private boolean canMove = true;
    private final Color[] playerColors = {new Color(255, 102, 102), new Color(255, 102, 255)};
    private final JLabel currentPlayerLabel;
    private final Random random = new Random();
    private final Set<Point> marketSquares;
    private final Set<Point> lostItemSquares;
    private final Set<Point> treasureSquares;
    private final Set<Point> wallHouseSquares;
    private final Set<Point> trapSquares;
    private final Set<Point> castleSquares;
    private Map<Point, String> treasureLocations = new HashMap<>();
    private static final Map<String, Integer> treasures = new HashMap<>();
    static {
        treasures.put("Diamond Ring", 1000);
        treasures.put("Jewel-encrusted Sword", 800);
        treasures.put("Golden Goblet", 600);
        treasures.put("Crystal Goblets", 500);
        treasures.put("Wooden Bow", 400);
        treasures.put("Paladin’s Shield", 700);
        treasures.put("Golden Key", 300);
        treasures.put("Ancient Amulet", 900);
        treasures.put("Dragon's Scroll", 200);
    }

    private List<Integer> playersWithTreasures;
    private Set<Point> treasureCollectionPositions;

    private JPanel messagePanel;
    // Constructor
    public Game() {
        playerXPositions = new int[MAX_PLAYERS];
        playerYPositions = new int[MAX_PLAYERS];
        movesLeft = new int[MAX_PLAYERS];
        marketSquares = new HashSet<>();
        lostItemSquares = new HashSet<>();
        treasureSquares = new HashSet<>();
        wallHouseSquares = new HashSet<>();
        trapSquares = new HashSet<>();
        castleSquares = new HashSet<>();
        setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE));
        JPanel topPanel = new JPanel();
        add(topPanel, BorderLayout.NORTH);
        currentPlayerLabel = new JLabel("Player " + (currentPlayer + 1) + " | Moves Left: " + getRandomNumber());
        topPanel.add(currentPlayerLabel);
        setFocusable(true);

        messagePanel = new JPanel();
        messagePanel.setLayout(new FlowLayout());
        JLabel messageLabel = new JLabel("Validation passed!");
        messagePanel.add(messageLabel);

        playersWithTreasures = new ArrayList<>();
        treasureCollectionPositions = new HashSet<>();

        initializePlayers();
        placeMarkets();
        placeLostItems();
        placeTreasures();
        placeTreasuresin();
        placeWallHouses();
        placeTraps();
        placeCastle();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
    }
    private void initializePlayers() {
        for (int i = 0; i < MAX_PLAYERS; i++) {
            playerXPositions[i] = 0;
            playerYPositions[i] = 1;
            movesLeft[i] = getRandomNumber(); // Set initial random moves left
        }
    }
    private int getRandomNumber() {
        // Generates a random number between 1 and 6 like a dice roll
        return random.nextInt(6) + 1;
    }

    private void placeTreasuresin() {
        placeUniqueTreasures();
    }

    private void placeUniqueTreasures() {
        Set<String> uniqueTreasures = new HashSet<>(treasures.keySet());
        for (Point treasureSquare : treasureSquares) {
            if (!uniqueTreasures.isEmpty()) {
                String treasure = getRandomUniqueTreasure(uniqueTreasures);
                treasureLocations.put(treasureSquare, treasure);
                uniqueTreasures.remove(treasure);
            }
        }
    }

    private String getRandomUniqueTreasure(Set<String> uniqueTreasures) {
        int randomIndex = random.nextInt(uniqueTreasures.size());
        int i = 0;
        for (String treasure : uniqueTreasures) {
            if (i == randomIndex) {
                return treasure;
            }
            i++;
        }
        return null;
    }

    private void movePlayer(int playerIndex, int dx, int dy) {
        int newX = playerXPositions[playerIndex] + dx;
        int newY = playerYPositions[playerIndex] + dy;
        if ((playersWithTreasures.contains(playerIndex) || isCastleSquare(playerXPositions[playerIndex], playerYPositions[playerIndex])) || (!playersWithTreasures.contains(playerIndex) && isValidMove(newX, newY))) {
            Point newPosition = new Point(newX, newY);
            if (isValidMove(newX, newY)) {
                if (castleSquares.contains(newPosition)) {
                    validateTreasureLocation(newX, newY, treasureLocations.get(newPosition));
                    displayTreasureInfo(treasureLocations.get(newPosition));
                    return; // Exit early after validating treasure location
                } else if (treasureSquares.contains(newPosition)) {
                    handleTreasurePickup(newX, newY);
                    return;
                }
                playerXPositions[playerIndex] = newX;
                playerYPositions[playerIndex] = newY;
                movesLeft[playerIndex]--;
                canMove = checkCanMove();
                updatePlayerLabel();
                repaint();
            }
        }
    }

    private boolean isCastleSquare(int x, int y) {
        return x == GRID_SIZE / 2 && y == GRID_SIZE / 2;
    }
    private Point findTreasureSquare(int x, int y) {
        for (Point square : treasureSquares) {
            if (square.x == x && square.y == y) {
                return square;
            }
        }
        return null;
    }

    private void handleTreasurePickup(int x, int y) {
        if (treasureLocations.containsKey(new Point(x, y))) {
            String treasureName = treasureLocations.get(new Point(x, y));
            if (!playersWithTreasures.contains(currentPlayer)) {
                displayTreasureInfo(treasureName);
                playersWithTreasures.add(currentPlayer);
            }
            if (playersWithTreasures.contains(currentPlayer) && isCastleSquare(x, y)) {
                validateTreasureLocation(x, y, treasureName);
            } else if (!isCastleSquare(x, y)) {
                treasureCollectionPositions.add(new Point(x, y));
                treasureSquares.remove(new Point(x, y));
                treasureLocations.remove(new Point(x, y));
                canMove = true; // Allow the player to move
                repaint();
            }
        }
    }


    private void validateTreasureLocation(int x, int y, String treasureName) {
        if (playersWithTreasures.contains(currentPlayer)) {
            JOptionPane.showMessageDialog(messagePanel, "Validation passed!", "Treasure Validated", JOptionPane.INFORMATION_MESSAGE);
            displayTreasureInfo(treasureName);
            updatePlayerAccountBalance(treasures.get(treasureName));
            increaseTreasureDiscoveryScore();
            playersWithTreasures.remove((Integer) currentPlayer);
            treasureCollectionPositions.remove(new Point(x, y));
            repaint();
        }
    }

    private void updatePlayerAccountBalance(int amount) {

    }

    private void increaseTreasureDiscoveryScore() {
        // Logic to increase the player's treasure discovery score
        // Increase player's treasure discovery score by one unit
        // Update player's treasure discovery score here
    }
    private void displayTreasureInfo(String treasureName) {
        JOptionPane.showMessageDialog(this, "You found a treasure: " + treasureName
                        + ". Move towards the castle to verify the treasure location with the castle authorities.",
                "Treasure Found!", JOptionPane.INFORMATION_MESSAGE);
    }
    // Place different types of squares on the board
    private void placeMarkets() {
        placeSquares(marketSquares, MARKETS);
    }

    private void placeLostItems() {
        placeSquares(lostItemSquares, LOST_ITEMS);
    }

    private void placeTreasures() {
        placeSquares(treasureSquares, TREASURES);
    }

    private void placeWallHouses() {
        placeSquares(wallHouseSquares, WALL_HOUSES);
    }

    private void placeTraps() {
        placeSquares(trapSquares, TRAPS);
    }

    private void placeCastle() {
        placeSquares(castleSquares, CASTLE, GRID_SIZE / 2, GRID_SIZE / 2);
    }

    // General method to place squares on the board
    private void placeSquares(Set<Point> squareSet, int numSquares) {
        for (int i = 0; i < numSquares; i++) {
            int squareX, squareY;
            do {
                // Randomly generate coordinates for the square within the board
                squareX = (int) (Math.random() * (GRID_SIZE - 2)) + 1;
                squareY = (int) (Math.random() * (GRID_SIZE - 2)) + 1;
            } while (isInvalidSquare(squareX, squareY) || squareSet.contains(new Point(squareX, squareY)));

            squareSet.add(new Point(squareX, squareY));
        }
    }

    // Overloaded method to place squares at fixed coordinates on the board
    private void placeSquares(Set<Point> squareSet, int numSquares, int fixedX, int fixedY) {
        for (int i = 0; i < numSquares; i++) {
            int squareX, squareY;
            do {
                // Use fixed coordinates for the square
                squareX = fixedX;
                squareY = fixedY;
            } while (isInvalidSquare(squareX, squareY) || squareSet.contains(new Point(squareX, squareY)));

            squareSet.add(new Point(squareX, squareY));
        }
    }
    // Check if a new square can be placed here
    private boolean isInvalidSquare(int x, int y) {
        return marketSquares.contains(new Point(x, y)) ||
                lostItemSquares.contains(new Point(x, y)) ||
                treasureSquares.contains(new Point(x, y)) ||
                wallHouseSquares.contains(new Point(x, y)) ||
                trapSquares.contains(new Point(x, y)) ||
                castleSquares.contains(new Point(x, y));
    }

    // Handle key presses for player movements
    private void handleKeyPress(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_SPACE) {
            if (movesLeft[currentPlayer] == 0) {
                switchPlayer();
            }
        } else if (canMove) {
            handleArrowKeyPress(keyCode);
        }
    }

    // Switch to the next player's turn
    private void switchPlayer() {
        currentPlayer = (currentPlayer + 1) % MAX_PLAYERS;
        movesLeft[currentPlayer] = getRandomNumber();
        canMove = true;
        updatePlayerLabel();
        repaint();
    }

    // Handle arrow key presses for player movement
    private void handleArrowKeyPress(int keyCode) {
        int dx = 0, dy = 0;

        switch (keyCode) {
            case KeyEvent.VK_UP:
                dy = -1;
                break;
            case KeyEvent.VK_DOWN:
                dy = 1;
                break;
            case KeyEvent.VK_LEFT:
                dx = -1;
                break;
            case KeyEvent.VK_RIGHT:
                dx = 1;
                break;
        }
        movePlayer(currentPlayer, dx, dy);
    }


    //Checks whether a move to the specified coordinates (x, y) is valid for the current player
    private boolean isValidMove(int x, int y) {
        return x >= 0 && x < GRID_SIZE && y >= 0 && y < GRID_SIZE && !isInsideRing(x, y) && movesLeft[currentPlayer] > 0;
    }
    // checks whether a given position is inside the 10x10 ring
    private boolean isInsideRing(int x, int y) {
        return x == 0 || y == 0 || x == GRID_SIZE - 1 || y == GRID_SIZE - 1;
    }
    // Check if any players can still move
    private boolean checkCanMove() {
        for (int moves : movesLeft) {
            if (moves > 0) {
                return true; // At least one player has moves left
            }
        }
        return false; // No players have moves left
    }
    // Update the player information label
    private void updatePlayerLabel() {
        int movesLeft = this.movesLeft[currentPlayer];
        currentPlayerLabel.setText("Player " + (currentPlayer + 1) + " | Moves Left: " + movesLeft);
        if (movesLeft == 0) {
            currentPlayerLabel.setText("Player " + (currentPlayer + 1) + " | Press Space to Switch Players");
        }
    }

    // Paint the game board and elements
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGrid(g);
        drawSquares(g, marketSquares, Color.ORANGE);
        drawSquares(g, lostItemSquares, Color.BLUE);
        drawSquares(g, treasureSquares, Color.GREEN);
        drawSquares(g, wallHouseSquares, Color.BLACK);
        drawSquares(g, trapSquares, Color.RED);
        drawSquares(g, castleSquares, Color.YELLOW);
        drawPlayers(g);
    }

    // Draw the game board grid
    private void drawGrid(Graphics g) {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int y = 0; y < GRID_SIZE; y++) {
                Color cellColor = determineCellColor(x, y);
                g.setColor(cellColor);
                g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g.setColor(Color.BLACK);
                g.drawRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                // Draw horizontal grid lines
                g.drawLine(x * CELL_SIZE, y * CELL_SIZE, (x + 1) * CELL_SIZE, y * CELL_SIZE);
                // Draw vertical grid lines
                g.drawLine(x * CELL_SIZE, y * CELL_SIZE, x * CELL_SIZE, (y + 1) * CELL_SIZE);
            }
        }
    }

    // Determine the color of each cell on the game board
    private Color determineCellColor(int x, int y) {
        if (x == 0 && y == 1) {
            return Color.LIGHT_GRAY;
        } else if (x > 0 && y > 0 && x < GRID_SIZE - 1 && y < GRID_SIZE - 1) {
            return new Color(139, 69, 19); // Brown for the 10x10 square
        } else {
            return new Color(173, 216, 230); // Light blue for the outer ring
        }
    }
    // Draw squares on the board
    private void drawSquares(Graphics g, Set<Point> squareSet, Color color) {
        g.setColor(color);
        for (Point square : squareSet) {
            g.fillRect(square.x * CELL_SIZE, square.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
    }
    // Draw players on the board
    private void drawPlayers(Graphics g) {
        g.setColor(getPlayerColor(currentPlayer));
        g.fillOval(playerXPositions[currentPlayer] * CELL_SIZE, playerYPositions[currentPlayer] * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }

    // Get the color of a player based on their index
    private Color getPlayerColor(int playerIndex) {
        return (playerIndex < playerColors.length) ? playerColors[playerIndex] : Color.BLACK;//assumes black if cant get
    }

    // Main method to start the game
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {//runs the GUI code and make sure it runs
            // Create a new JFrame
            JFrame frame = new JFrame("Game");
            // Set the default close operation to EXIT_ON_CLOSE, which exits the application when the frame is closed
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//when x is pressed
            Game game = new Game();
            frame.getContentPane().add(game);// Add the game panel to the content pane of the frame
            frame.pack();// Pack the frame, causing it to be sized to fit the preferred size of its components
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);// Set the frame to be visible
        });
    }
}