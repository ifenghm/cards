import java.util.HashMap;

import processing.core.PApplet;

public class MonopolyDeal extends CardGame {
    // i need to be able to steal sets rather than just one property card
    // so we need to be able to select a "set" based on the selected card.
    static final String RAILROAD = "Railroad";
    static final String UTILITY = "Utility";
    static final String BROWN = "Brown";
    static final String LIGHT_BLUE = "Light Blue";
    static final String PINK = "Pink";
    static final String ORANGE = "Orange";
    static final String YELLOW = "Yellow";
    static final String RED = "Red";
    static final String GREEN = "Green";
    static final String BLUE = "Blue";
    static final String PASS_GO = "Pass Go";
    static final String SLY_DEAL = "Sly Deal";
    static final String DEAL_BREAKER = "Deal Breaker";
    static final String JUST_SAY_NO = "Just Say No";
    static final String DEBT_COLLECTOR = "Debt Collector";
    static final String BIRTHDAY = "It's My Birthday";

    static final int HAND_SPACING = 80;
    static final int X_START = 30;
    static final int Y_START = 650;

    int playsCount = 0; // keeps track of the number of plays by the current player

    // counts of each property types
    HashMap<String, Integer> propertyCounts;
    static final int buttonsX = 700;
    ClickableRectangle endTurnButton = new ClickableRectangle(buttonsX, Y_START + drawButtonHeight + 15, 80,
            drawButtonHeight, "End");

    MonopolyDeal() {

        initializeGame();
        drawButton = new ClickableRectangle(buttonsX, Y_START, 80, drawButtonHeight, "Draw");
        playerOneHand = new MonopolyHand(1);
        playerTwoHand = new MonopolyHand(2);
        dealCards(5);
        // position cards
        playerOneHand.positionCards(X_START, Y_START, HAND_SPACING, 120, HAND_SPACING);
        playerTwoHand.positionCards(X_START, 30, HAND_SPACING, 120, HAND_SPACING);
    }

    public Hand getCurrentPlayerHand() {
        return playerOneTurn ? playerOneHand : playerTwoHand;
    }

    @Override
    protected void createDeck() {
        // maybe import a spreadsheet to do the cards exactly
        // this is fine for now.
        HashMap<Integer, Integer> moneyCards = new HashMap<>();
        moneyCards.put(1, 6); // 6 $1 cards
        moneyCards.put(2, 5); // 5 $2 cards
        moneyCards.put(3, 3); // 3 $3 cards
        moneyCards.put(4, 3); // 3 $4 cards
        moneyCards.put(5, 2); // 2 $5 cards
        moneyCards.put(10, 1); // 1 $10 card
        for (int value : moneyCards.keySet()) {
            int count = moneyCards.get(value);
            for (int i = 0; i < count; i++) {
                deck.add(new MonopolyCard(String.valueOf(value), "Money"));
            }
        }
        // Add property cards (simplified, not all properties or colors)
        propertyCounts = new HashMap<>();
        propertyCounts.put(BLUE, 2);
        propertyCounts.put(BROWN, 2);
        propertyCounts.put(UTILITY, 2);
        propertyCounts.put(RAILROAD, 4);
        String[] properties = { GREEN, RED, ORANGE, LIGHT_BLUE, PINK, YELLOW };
        for (String prop : properties) {
            propertyCounts.put(prop, 3);
        }
        HashMap<String, Integer> propertyRents = new HashMap<>();
        propertyRents.put(UTILITY, 1);
        propertyRents.put(RAILROAD, 1);
        propertyRents.put(LIGHT_BLUE, 1);
        propertyRents.put(BROWN, 1);
        propertyRents.put(ORANGE, 2);
        propertyRents.put(PINK, 2);
        propertyRents.put(YELLOW, 2);
        propertyRents.put(RED, 3);
        propertyRents.put(GREEN, 3);
        propertyRents.put(BLUE, 4);

        for (String prop : propertyCounts.keySet()) {
            int count = propertyCounts.get(prop);
            for (int i = 0; i < count; i++) {
                // the selling value seem arbitrary, but the utilities are worth a bit more
                deck.get(i).setClickableWidth(deck.get(i).width);
                deck.add(new PropertyCard(
                        String.valueOf(prop == UTILITY || prop == RAILROAD ? 2 : propertyRents.get(prop)),
                        propertyRents.get(prop), prop));
            }
        }
        // Add action cards (simplified, not all actions)
        String[] actions = { PASS_GO, DEAL_BREAKER, JUST_SAY_NO, SLY_DEAL, DEBT_COLLECTOR, BIRTHDAY };
        String[] actionValues = { "1", "5", "3", "4", "3", "2" };
        int[] actionCounts = { 10, 2, 3, 3, 3, 3 }; // number of each action card
        for (int i = 0; i < actions.length; i++) {
            for (int j = 0; j < actionCounts[i]; j++) {
                deck.add(new ActionCard(actionValues[i], actions[i], this));
            }
        }

        for (Card card : deck) {
            card.setClickableWidth(80); // set clickable width for all cards
        }
    }

    @Override
    public void handleDrawButtonClick(int mouseX, int mouseY) {
        if (drawButton.isClicked(mouseX, mouseY) && playerOneTurn) {
            drawCard(playerOneHand);
            drawCard(playerOneHand);
            playerOneHand.positionCards(X_START, Y_START, HAND_SPACING, 120, HAND_SPACING);
            playerTwoHand.positionCards(X_START, 30, HAND_SPACING, 120, HAND_SPACING);
        }
        // also handle end turn
        if (endTurnButton.isClicked(mouseX, mouseY) && playerOneTurn) {
            switchTurns();
            playsCount = 0; // reset play count for new turn
        }
    }

    @Override 
    public boolean playCard(Card card, Hand hand) {
        // If Money card, add to bank pile
        if (card.suit.equals("Money")) {
            ((MonopolyHand) hand).bankPile.addCard(card);
        } else if (card.suit.equals("Property")) {
            ((MonopolyHand) hand).propertyPile.addCard(card);
        }
        // Remove card from hand
        hand.removeCard(card);
        playsCount++;
        return true;
    }

    @Override
    public void drawChoices(PApplet sketch) {
        endTurnButton.draw(sketch);
    }

    @Override
    public void switchTurns() {
        playerOneTurn = !playerOneTurn;
    }

    @Override
    public void handleComputerTurn() {
        // TODO: implement actual computer logic
        drawCard(playerTwoHand);
        drawCard(playerTwoHand);
        playerOneHand.positionCards(X_START, Y_START, HAND_SPACING, 120, HAND_SPACING);
        playerTwoHand.positionCards(X_START, 30, HAND_SPACING, 120, HAND_SPACING);
        switchTurns();
    }
}