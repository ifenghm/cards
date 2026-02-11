import java.util.ArrayList;
import java.util.Collections;
import processing.core.PApplet;

public class Uno {
    // Player turns
    boolean playerOneTurn = true;

    // Decks and hands
    ArrayList<UnoCard> deck;
    Hand playerOneHand;
    Hand playerTwoHand;
    ArrayList<UnoCard> discardPile;

    // Game state
    UnoCard lastPlayedCard;
    boolean gameActive;
    UnoCard selectedCard;
    UnoComputer computerPlayer;
    boolean choosingWildColor = false;
    UnoCard pendingWildCard;
    ClickableRectangle[] wildColorButtons;
    String[] wildColors = { "Red", "Yellow", "Green", "Blue" };
    int wildButtonSize = 24;
    int wildCenterX = 300;
    int wildCenterY = 300;

    // UI
    ClickableRectangle drawButton;
    int drawButtonX = 250;
    int drawButtonY = 400;
    int drawButtonWidth = 100;
    int drawButtonHeight = 35;

    public Uno() {
        initializeGame();
    }

    public void initializeGame() {
        // Initialize decks and hands
        deck = new ArrayList<>();
        discardPile = new ArrayList<>();
        playerOneHand = new Hand();
        playerTwoHand = new Hand();
        computerPlayer = new UnoComputer();
        gameActive = true;

        // Create deck (Uno has 108 cards)
        String[] colors = { "Red", "Yellow", "Green", "Blue" };
        String[] values = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "Skip", "Reverse", "Draw Two" };

        // Create standard cards (2 of each color/value combination except 0)
        for (String color : colors) {
            deck.add(createCard(color, "0")); // One 0 card per color
            for (String value : values) {
                if (!value.equals("0")) {
                    deck.add(createCard(color, value));
                    deck.add(createCard(color, value)); // Two of each
                }
            }
        }

        // Add wild cards (4 of each type)
        for (int i = 0; i < 4; i++) {
            // suit, value
            deck.add(createCard("Wild", "Wild"));
            deck.add(createCard("Wild", "Draw Four"));
        }

        // Shuffle deck
        Collections.shuffle(deck);

        // Deal 7 cards to each player
        for (int i = 0; i < 7; i++) {
            playerOneHand.addCard(deck.remove(0));
            Card card = deck.remove(0);
            card.setTurned(true);
            playerTwoHand.addCard(card);
        }

        // Place first card on discard pile
        lastPlayedCard = deck.remove(0);
        if (lastPlayedCard.suit.equals("Wild")) {
            System.out.println("setting wild to a random color");
            // If first card is wild, set it to a random color
            lastPlayedCard.suit = colors[(int) (Math.random() * colors.length)];
        }
        discardPile.add(lastPlayedCard);

        // Initialize draw button
        drawButton = new ClickableRectangle();
        drawButton.x = drawButtonX;
        drawButton.y = drawButtonY;
        drawButton.width = drawButtonWidth;
        drawButton.height = drawButtonHeight;

        playerOneHand.positionCards(50, 450, 80, 120, 20);
        playerTwoHand.positionCards(50, 50, 80, 120, 20);
        initializeWildColorButtons();
    }

    private UnoCard createCard(String suit, String value) {
        UnoCard card = new UnoCard(suit, value); // Image loading can be added later
        card.suit = suit;
        card.value = value;
        return card;
    }

    public void switchTurns() {
        playerOneTurn = !playerOneTurn;
        playerOneHand.positionCards(50, 450, 80, 120, 20);
        playerTwoHand.positionCards(50, 50, 80, 120, 20);
    }

    public void drawCard(Hand hand) {
        if (!deck.isEmpty()) {
            hand.addCard(deck.remove(0));
        } else {
            // Reshuffle discard pile into deck if deck is empty
            if (discardPile.size() > 1) {
                // Keep last played card, shuffle rest back into deck
                lastPlayedCard = discardPile.remove(discardPile.size() - 1);
                deck.addAll(discardPile);
                discardPile.clear();
                discardPile.add(lastPlayedCard);
                Collections.shuffle(deck);

                if (!deck.isEmpty()) {
                    hand.addCard(deck.remove(0));
                }
            }
        }
    }

    public boolean playCard(UnoCard card, Hand hand) {
        // Check if card is valid to play
        if (isValidPlay(card)) {
            // Remove card from hand
            hand.removeCard(card);
            card.setTurned(false);
            // Add to discard pile
            discardPile.add(card);
            lastPlayedCard = card;
            if ("Reverse".equals(card.value)) {
                // gives another turn
                return true;
            }
            // handle special cards
            handleSpecialCards(card);
            // Switch turns
            switchTurns();
            return true;
        }
        System.out.println("Invalid play: " + card.value + " of " + card.suit);
        return false;
    }

    private void handleSpecialCards(UnoCard card) {
        if (card.value.equals("Skip")) {
            System.out.println("Skipping opponent's turn");
            switchTurns(); // Skip opponent's turn
        } else if (card.value.startsWith("Draw ")) {
            System.out.println("Skipping opponent's turn");
            int drawNum = "Draw Two".equals(card.value) ? 2 : 4;
            for (int i = 0; i < drawNum; i++) {
                drawCard(playerOneTurn ? playerTwoHand : playerOneHand);
            }
            switchTurns();
        }
    }

    private boolean isValidPlay(UnoCard card) {
        // Wild cards are always valid
        if (card.suit.equals("Wild")) {
            return true;
        }
        // Card must match suit or value of last played card
        return card.suit.equals(lastPlayedCard.suit) ||
                card.value.equals(lastPlayedCard.value);
    }

    public void handleDrawButtonClick(int mouseX, int mouseY) {
        if (choosingWildColor) {
            return;
        }
        if (drawButton.isClicked(mouseX, mouseY) && playerOneTurn) {
            drawCard(playerOneHand);
            // Switch turns after drawing
            switchTurns();
        }
    }

    public UnoCard getLastPlayedCard() {
        return lastPlayedCard;
    }

    public String getCurrentPlayer() {
        return playerOneTurn ? "Player One" : "Player Two";
    }

    public int getDeckSize() {
        return deck.size();
    }

    public void handleCardClick(int mouseX, int mouseY) {
        if (!playerOneTurn) {
            return;
        }
        if (choosingWildColor) {
            handleWildChooserClick(mouseX, mouseY);
            return;
        }
        UnoCard clickedCard = null;
        int raiseAmount = 15;

        for (int i = playerOneHand.getSize() - 1; i >= 0; i--) {
            Card card = playerOneHand.getCard(i);
            if (card != null && card.isClicked(mouseX, mouseY)) {
                clickedCard = (UnoCard) card;
                break;
            }
        }
        if (clickedCard == null) {
            return;
        }
        // this is for the first time
        if (selectedCard == null) {
            selectedCard = clickedCard;
            selectedCard.setSelected(true, raiseAmount);
            return;
        }
        // this is the second time
        if (clickedCard == selectedCard) {
            System.out.println("playing card: " + selectedCard.value + " of " + selectedCard.suit);
            if ("Wild".equals(selectedCard.suit)) {
                pendingWildCard = selectedCard;
                choosingWildColor = true;
                return;
            }
            if (playCard(selectedCard, playerOneHand)) {
                selectedCard.setSelected(false, raiseAmount);
                selectedCard = null;
            }
            return;
        }

        selectedCard.setSelected(false, raiseAmount);
        selectedCard = clickedCard;
        selectedCard.setSelected(true, raiseAmount);
    }

    public void drawWildChooser(PApplet app) {
        if (!choosingWildColor) {
            return;
        }
        app.push();
        app.fill(255, 255, 255, 230);
        app.noStroke();
        app.rect(wildCenterX - 50, wildCenterY - 50, 100, 100, 8);

        for (int i = 0; i < wildColorButtons.length; i++) {
            ClickableRectangle button = wildColorButtons[i];
            switch (wildColors[i]) {
                case "Red":
                    app.fill(255, 0, 0);
                    break;
                case "Yellow":
                    app.fill(255, 255, 0);
                    break;
                case "Green":
                    app.fill(0, 255, 0);
                    break;
                case "Blue":
                    app.fill(40, 40, 210);
                    break;
                default:
                    app.fill(200);
                    break;
            }
            app.rect(button.x, button.y, button.width, button.height, 4);
        }
        app.pop();
    }

    private void handleWildChooserClick(int mouseX, int mouseY) {
        int raiseAmount = 15;
        for (int i = 0; i < wildColorButtons.length; i++) {
            if (wildColorButtons[i].isClicked(mouseX, mouseY)) {
                if (playCard(pendingWildCard, playerOneHand)) {
                    // Set the wild card's suit to the chosen color AFTER it is validated
                    pendingWildCard.suit = wildColors[i];
                    pendingWildCard.setSelected(false, raiseAmount);
                    selectedCard = null;
                    pendingWildCard = null;
                    choosingWildColor = false;
                    playerOneHand.positionCards(50, 450, 80, 120, 20);
                    playerTwoHand.positionCards(50, 50, 80, 120, 20);
                }
                return;
            }
        }
    }

    private void initializeWildColorButtons() {
        wildColorButtons = new ClickableRectangle[4];
        int offset = 28;
        int half = wildButtonSize / 2;

        wildColorButtons[0] = createWildButton(wildCenterX - offset - half, wildCenterY - half);
        wildColorButtons[1] = createWildButton(wildCenterX + offset - half, wildCenterY - half);
        wildColorButtons[2] = createWildButton(wildCenterX - half, wildCenterY - offset - half);
        wildColorButtons[3] = createWildButton(wildCenterX - half, wildCenterY + offset - half);
    }

    private ClickableRectangle createWildButton(int x, int y) {
        ClickableRectangle button = new ClickableRectangle();
        button.x = x;
        button.y = y;
        button.width = wildButtonSize;
        button.height = wildButtonSize;
        return button;
    }

    public void handleComputerTurn() {
        UnoCard choice = computerPlayer.playCard(playerTwoHand, lastPlayedCard);
        if (choice == null) {
            drawCard(playerTwoHand);
            playerTwoHand.getCard(0).setTurned(true);
            System.out.println("player two draws");
            switchTurns();
            return;
        }
        if (playCard(choice, playerTwoHand)) {
            if ("Wild".equals(choice.suit)) {
                choice.suit = computerPlayer.chooseComputerWildColor(playerTwoHand);
            }
            playerOneHand.positionCards(50, 450, 80, 120, 20);
            playerTwoHand.positionCards(50, 50, 80, 120, 20);
        } else {
            System.out.println("ERROR, player two / computer chose an invalid play");
        }
    }
}
