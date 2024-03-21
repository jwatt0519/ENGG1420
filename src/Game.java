
     import javax.swing.*;
        import java.awt.*;
        import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.util.*;
import java.util.List;

class PlayerVisitedSquares {
    Set<Point> visitedSquares = new HashSet<>();

    void addVisitedSquare(Point square) {
        visitedSquares.add(square);
    }

    boolean hasVisited(Point square) {
        return visitedSquares.contains(square);
    }
}


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
    private int[] playerAccountBalances = new int[MAX_PLAYERS];
    private int[] playerDiscoveryPoints = new int[MAX_PLAYERS];
    private boolean canMove = true;
    private final Color[] playerColors = {new Color(255, 102, 102), new Color(255, 102, 255)};
    private final JLabel currentPlayerLabel;
    private final Random random = new Random();

    private PlayerVisitedSquares[] playerVisitedSquares = new PlayerVisitedSquares[MAX_PLAYERS];
    private final Set<Point> marketSquares;
    private final Set<Point> lostItemSquares;
    private final Set<Point> treasureSquares;
    private final Set<Point> wallHouseSquares;
    private final Set<Point> trapSquares;
    private final Set<Point> castleSquares;
    private final Map<Point, String> treasureLocations = new HashMap<>();
    private boolean[][] visitedSquares;
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

    // Inside the Game class
    private List<Map<String, Integer>> playerInventories = new ArrayList<>();
    private Map<String, Integer> castleTreasureInventory = new HashMap<>();
    private List<Integer> playersWithTreasures;
    private Set<Point> treasureCollectionPositions;
    private List<Point> unvalidatedTreasures;
    private JTextArea player1InventoryDisplay;
    private JTextArea player2InventoryDisplay;
    private JTextArea castleInventoryDisplay;
    private JScrollPane castleInventoryScrollPane;
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
        visitedSquares = new boolean[GRID_SIZE][GRID_SIZE];
        for (boolean[] row : visitedSquares) {
            Arrays.fill(row, false);
        }
        playerVisitedSquares = new PlayerVisitedSquares[MAX_PLAYERS];
        messagePanel = new JPanel();
        messagePanel.setLayout(new FlowLayout());
        JLabel messageLabel = new JLabel("Validation passed!");
        messagePanel.add(messageLabel);
        playersWithTreasures = new ArrayList<>();
        treasureCollectionPositions = new HashSet<>();
        unvalidatedTreasures = new ArrayList<>();


        setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE + 220, GRID_SIZE * CELL_SIZE)); // Adjust width for inventory
        setLayout(new BorderLayout()); // Use BorderLayout for the main panel

        initializeUIComponents();


        initializePlayers();
        placeMarkets();
        placeLostItems();
        placeTreasures();
        placeTreasuresin();
        placeWallHouses();
        placeTraps();
        placeCastle();
        updatePlayerInventoriesDisplay();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
    }

    private void updateCastleInventoryDisplay() {
        StringBuilder inventoryText = new StringBuilder("Castle Inventory:\n");
        castleTreasureInventory.forEach((treasureName, quantity) -> inventoryText.append(treasureName).append(": ").append(quantity).append("\n"));
        castleInventoryDisplay.setText(inventoryText.toString());
    }

    private void initializePlayers() {
        for (int i = 0; i < MAX_PLAYERS; i++) {
            playerXPositions[i] = 0; // Starting X position for both players
            playerYPositions[i] = 1; // Starting Y position for both players
            movesLeft[i] = getRandomNumber(); // Initialize moves
            playerVisitedSquares[i] = new PlayerVisitedSquares();
            playerAccountBalances[i] = 0;
            playerDiscoveryPoints[i] = 0; // Initialize discovery points
            // Mark the starting square as visited
            playerVisitedSquares[i].addVisitedSquare(new Point(playerXPositions[i], playerYPositions[i]));
            playerInventories.add(new HashMap<>());
        }
    }

    private void initializeUIComponents() {
        player1InventoryDisplay = new JTextArea(5, 20);
        player1InventoryDisplay.setEditable(false);
        JScrollPane player1Scroll = new JScrollPane(player1InventoryDisplay);
        player1Scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        player2InventoryDisplay = new JTextArea(5, 20);
        player2InventoryDisplay.setEditable(false);
        JScrollPane player2Scroll = new JScrollPane(player2InventoryDisplay);
        player2Scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        JPanel inventoryPanel = new JPanel(new GridLayout(1, 2)); // Arrange side by side
        inventoryPanel.add(player1Scroll);
        inventoryPanel.add(player2Scroll);

        // Adjust based on your layout preference
        add(inventoryPanel, BorderLayout.SOUTH);

        // Top panel with player information
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        currentPlayerLabel.setText("Player " + (currentPlayer + 1) + " | Moves Left: " + getRandomNumber());
        topPanel.add(currentPlayerLabel);
        add(topPanel, BorderLayout.NORTH);


        // Castle inventory display setup
        castleInventoryDisplay = new JTextArea(10, 20);
        castleInventoryDisplay.setEditable(false);
        castleInventoryScrollPane = new JScrollPane(castleInventoryDisplay);
        castleInventoryScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(castleInventoryScrollPane, BorderLayout.EAST); // Place the castle inventory on the right

        updateCastleInventoryDisplay(); // Update the display initially
    }


    private int getRandomNumber() {
        // Generates a random number between 1 and 6 like a dice roll
        return random.nextInt(6) + 1;
    }


    private Set<Point> surroundingPoints(Point centerPoint) {
        Set<Point> result = new HashSet<>();
        for (int offsetX = -1; offsetX <= 1; ++offsetX) {
            for (int offsetY = -1; offsetY <= 1; ++offsetY) {
                if (!(offsetX == 0 && offsetY == 0)) {
                    int testX = centerPoint.x + offsetX;
                    int testY = centerPoint.y + offsetY;
                    if (testX >= 0 && testX < GRID_SIZE && testY >= 0 && testY < GRID_SIZE) {
                        result.add(new Point(testX, testY));
                    }
                }
            }
        }
        return result;
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
        int oldCenterX = playerXPositions[playerIndex];
        int oldCenterY = playerYPositions[playerIndex];
        int newCenterX = Math.min(Math.max(0, oldCenterX + dx), GRID_SIZE - 1);
        int newCenterY = Math.min(Math.max(0, oldCenterY + dy), GRID_SIZE - 1);
        playerVisitedSquares[playerIndex].addVisitedSquare(new Point(newCenterX, newCenterY));
        if ((playersWithTreasures.contains(playerIndex) || isCastleSquare(oldCenterX, oldCenterY)) || (!playersWithTreasures.contains(playerIndex) && isValidMove(newCenterX, newCenterY))) {
            if (!landedOnSquareIsVisited(oldCenterX, oldCenterY, newCenterX, newCenterY)) {
                markNewlyLandedsSquaresAsVisited(oldCenterX, oldCenterY, newCenterX, newCenterY);
            }
            if (isValidMove(newCenterX, newCenterY)) {
                playerXPositions[playerIndex] = newCenterX;
                playerYPositions[playerIndex] = newCenterY;
                movesLeft[playerIndex]--;
                canMove = checkCanMove();
                updatePlayerLabel();
                if (castleSquares.contains(new Point(newCenterX, newCenterY))) {
                    validateTreasureLocation(newCenterX, newCenterY);
                    if (treasureLocations.containsKey(new Point(newCenterX, newCenterY))) {
                        displayTreasureInfo(treasureLocations.get(new Point(newCenterX, newCenterY)));
                    }
                } else if (treasureSquares.contains(new Point(newCenterX, newCenterY))) {
                    handleTreasurePickup(newCenterX, newCenterY);
                }
                repaint();
            }
        }
        if (playerXPositions[0] == playerXPositions[1] && playerYPositions[0] == playerYPositions[1]) {
            JOptionPane.showMessageDialog(this, "BATTLE!!!!", "Battle Encounter", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    private void markNewlyLandedsSquaresAsVisited(int oldCenterX, int oldCenterY, int newCenterX, int newCenterY) {
        visitedSquares[oldCenterX][oldCenterY] = true;
        visitedSquares[newCenterX][newCenterY] = true;
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

    private void transferTreasureToCastle(String treasureName) {
        if (playersWithTreasures.contains(currentPlayer)) {
            castleTreasureInventory.put(treasureName, treasures.get(treasureName)); // Add the treasure to the castle's inventory
            playersWithTreasures.remove((Integer) currentPlayer); // Remove the treasure from the player's inventory
            JOptionPane.showMessageDialog(messagePanel, "Treasure transferred to the castle: " + treasureName, "Treasure Transferred", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private void handleTreasurePickup(int x, int y) {
        if (treasureLocations.containsKey(new Point(x, y))) {
            String treasureName = treasureLocations.get(new Point(x, y));
            if (!playerInventories.get(currentPlayer).isEmpty()) {
                JOptionPane.showMessageDialog(this, "You cannot pick up more than one treasure. Please validate your current treasure at the castle first.", "Treasure Pickup Error", JOptionPane.ERROR_MESSAGE);
                return; // Exit the method to prevent picking up a new treasure
            }
            if (!playersWithTreasures.contains(currentPlayer)) {
                displayTreasureInfo(treasureName);
                playersWithTreasures.add(currentPlayer);

            }
            if (treasureLocations.containsKey(new Point(x, y))) {
                treasureName = treasureLocations.get(new Point(x, y));
                // Add the treasure to the player's inventory
                playerInventories.get(currentPlayer).put(treasureName, treasures.get(treasureName));
                updatePlayerInventoriesDisplay();
                updatePlayerInventoriesDisplay();
                playerDiscoveryPoints[currentPlayer]++; // Increment discovery points for finding a treasure
                updatePlayerInventoriesDisplay(); // Update the display including discovery points
                treasureSquares.remove(new Point(x, y));
                treasureLocations.remove(new Point(x, y));
                canMove = true; // Allow the player to move
                repaint();
            }
            if (isCastleSquare(x, y)) {
                if (playersWithTreasures.contains(currentPlayer)) {
                    if (unvalidatedTreasures.isEmpty()) {
                        JOptionPane.showMessageDialog(messagePanel, "You need to grab another treasure before validating at the castle.", "Treasure Validation Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        validateTreasureLocation(x, y);
                        unvalidatedTreasures.add(new Point(x, y));
                    }
                } else {
                    JOptionPane.showMessageDialog(messagePanel, "You don't have any treasures to validate at the castle.", "Treasure Validation Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                treasureCollectionPositions.add(new Point(x, y));
                treasureSquares.remove(new Point(x, y));
                treasureLocations.remove(new Point(x, y));
                canMove = true; // Allow the player to move
                repaint();
            }
        }
    }

    private void initializeCastleInventory() {
        castleTreasureInventory.put("Diamond Ring", 0);
        castleTreasureInventory.put("Jewel-encrusted Sword", 0);
        castleTreasureInventory.put("Golden Goblet", 0);
        castleTreasureInventory.put("Crystal Goblets", 0);
        castleTreasureInventory.put("Wooden Bow", 0);
        castleTreasureInventory.put("Paladin’s Shield", 0);
        castleTreasureInventory.put("Golden Key", 0);
        castleTreasureInventory.put("Ancient Amulet", 0);
        castleTreasureInventory.put("Dragon's Scroll", 0);
    }

    private void validateTreasureLocation(int x, int y) {
        Map<String, Integer> inventory = playerInventories.get(currentPlayer);
        if (!inventory.isEmpty()) {
            int totalValue = 0;
            for (String treasureName : inventory.keySet()) {
                Integer value = treasures.get(treasureName); // Get treasure value
                totalValue += value; // Sum up the treasure values
                castleTreasureInventory.merge(treasureName, value, Integer::sum); // Transfer to castle
            }
            playerAccountBalances[currentPlayer] += totalValue; // Update player account balance
            inventory.clear(); // Clear player inventory after validation
            JOptionPane.showMessageDialog(this, "Treasure validated. " + totalValue + " added to account.", "Treasure Validated", JOptionPane.INFORMATION_MESSAGE);

            // Update displays
            updatePlayerInventoriesDisplay();
            updateCastleInventoryDisplay();
        } else {
            JOptionPane.showMessageDialog(this, "No treasures to validate.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePlayerInventoriesDisplay() {
        // Resetting the StringBuilder for each player
        StringBuilder inventoryTextP1 = new StringBuilder("Player 1 Inventory:\n");
        StringBuilder inventoryTextP2 = new StringBuilder("Player 2 Inventory:\n");

        // Iterate over each player's inventory to append treasure names and values
        for (int i = 0; i < MAX_PLAYERS; i++) {
            Map<String, Integer> inventory = playerInventories.get(i);
            StringBuilder inventoryText = (i == 0) ? inventoryTextP1 : inventoryTextP2;

            // If the player's inventory is not empty, list the treasures
            if (!inventory.isEmpty()) {
                inventory.forEach((treasureName, treasureValue) -> {
                    inventoryText.append(treasureName).append(": ").append(treasureValue).append("\n");
                });
            } else {
                inventoryText.append("No treasures collected.\n");
            }

            // Append the player's account balance
            inventoryText.append("Account Balance: ").append(playerAccountBalances[i]).append("\n");
            inventoryText.append("Discovery Points: ").append(playerDiscoveryPoints[i]).append("\n\n");
        }

        // Set the text for each player's inventory display
        player1InventoryDisplay.setText(inventoryTextP1.toString());
        player2InventoryDisplay.setText(inventoryTextP2.toString());
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
        if (currentPlayer == 0) {
            playerVisitedSquares[0].visitedSquares.clear();
            playerVisitedSquares[0].addVisitedSquare(new Point(playerXPositions[0], playerYPositions[0])); // Add only the starting position
        }
    }

    private void placeTreasures() {
        placeSquares(treasureSquares, TREASURES);
        if (currentPlayer == 0) {
            playerVisitedSquares[0].visitedSquares.clear();
            playerVisitedSquares[0].addVisitedSquare(new Point(playerXPositions[0], playerYPositions[0])); // Add only the starting position
        }
    }

    private void placeWallHouses() {
        placeSquares(wallHouseSquares, WALL_HOUSES);
        if (currentPlayer == 0) {
            playerVisitedSquares[0].visitedSquares.clear();
            playerVisitedSquares[0].addVisitedSquare(new Point(playerXPositions[0], playerYPositions[0])); // Add only the starting position
        }
    }

    private void placeTraps() {
        placeSquares(trapSquares, TRAPS);
        if (currentPlayer == 0) {
            playerVisitedSquares[0].visitedSquares.clear();
            playerVisitedSquares[0].addVisitedSquare(new Point(playerXPositions[0], playerYPositions[0])); // Add only the starting position
        }
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
            playerVisitedSquares[currentPlayer].addVisitedSquare(new Point(squareX, squareY));
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
        markAdjacentSquaresAsVisited(playerXPositions[currentPlayer], playerYPositions[currentPlayer]);
        movesLeft[currentPlayer] = getRandomNumber();
        canMove = true;
        updatePlayerLabel();
        repaint();
    }

    private void markAdjacentSquaresAsVisited(int centerX, int centerY) {
        Set<Point> adjacentSquares = surroundingPoints(new Point(centerX, centerY));
        for (Point square : adjacentSquares) {
            visitedSquares[square.x][square.y] = true;
        }
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
        markAdjacentSquaresAsVisited(playerXPositions[currentPlayer], playerYPositions[currentPlayer]);
        repaint();
    }

    private boolean landedOnSquareIsVisited(int oldCenterX, int oldCenterY, int targetX, int targetY) {
        if (oldCenterX == targetX && oldCenterY == targetY) {
            return visitedSquares[targetX][targetY];
        }
        return false;
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
        // DEBUG ONLY: Reset Player 1's visibility to test drawing logic

        // Now calling drawSquares with the alwaysVisible argument
        drawSquares(g, marketSquares, Color.ORANGE, false); // Markets are always visible
        drawSquares(g, castleSquares, Color.YELLOW, true); // Castles are always visible

        // These squares depend on player visibility
        drawSquares(g, lostItemSquares, Color.BLUE, false);
        drawSquares(g, treasureSquares, Color.GREEN, false);
        drawSquares(g, wallHouseSquares, Color.BLACK, false);
        drawSquares(g, trapSquares, Color.RED, false);

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
    // Draw squares on the board
    // Updated drawSquares method to use currentPlayer's visibility
    private void drawSquares(Graphics g, Set<Point> squareSet, Color color, boolean alwaysVisible) {
        for (Point square : squareSet) {
            if (alwaysVisible || playerVisitedSquares[currentPlayer].hasVisited(square)) {
                g.setColor(color);
                g.fillRect(square.x * CELL_SIZE, square.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
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