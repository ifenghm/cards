import java.util.HashMap;

import processing.core.PApplet;

public class MonopolyDeal extends CardGame {
    // i need to be able to steal sets rather than just one property card
    // so we need to be able to select a "set" based on the selected card.
    static final int HAND_SPACING = 80;
    static final int X_START = 30;
    static final int Y_START = 650;
    static final int buttonsX = 700;

    int playsCount = 0; // keeps track of the number of plays by the current player

    boolean choosingAction = false; // whether the player is currently choosing which action to play
    boolean playingAsAction = false; // whether the player is currently choosing how to play an action card
    Button playActionButton = new Button(App.gameWidth / 2 - 80, Y_START - 50, 80, drawButtonHeight, "Play Action");
    Button bankActionButton = new Button(App.gameWidth / 2 + 10, Y_START - 50, 80, drawButtonHeight, "Bank");
    // counts of each property types
    HashMap<String, Integer> propertyCounts;
    ClickableRectangle endTurnButton = new ClickableRectangle(buttonsX, Y_START + drawButtonHeight + 15, 80,
            drawButtonHeight, "End");

    // action

    MonopolyDeal() {

        initializeGame();
        drawButton = new Button(buttonsX, Y_START, 80, drawButtonHeight, "Draw");
        playerOneHand = new MonopolyHand(1);
        playerTwoHand = new MonopolyHand(2);
        dealCards(5);
        // position cards
        positionCards();
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
        propertyCounts.put(MonopolyFields.BLUE, 2);
        propertyCounts.put(MonopolyFields.BROWN, 2);
        propertyCounts.put(MonopolyFields.UTILITY, 2);
        propertyCounts.put(MonopolyFields.RAILROAD, 4);
        String[] properties = { MonopolyFields.GREEN, MonopolyFields.RED, MonopolyFields.ORANGE,
                MonopolyFields.LIGHT_BLUE,
                MonopolyFields.PINK, MonopolyFields.YELLOW };
        for (String prop : properties) {
            propertyCounts.put(prop, 3);
        }
        HashMap<String, Integer> propertyRents = new HashMap<>();
        propertyRents.put(MonopolyFields.UTILITY, 1);
        propertyRents.put(MonopolyFields.RAILROAD, 1);
        propertyRents.put(MonopolyFields.LIGHT_BLUE, 1);
        propertyRents.put(MonopolyFields.BROWN, 1);
        propertyRents.put(MonopolyFields.ORANGE, 2);
        propertyRents.put(MonopolyFields.PINK, 2);
        propertyRents.put(MonopolyFields.YELLOW, 2);
        propertyRents.put(MonopolyFields.RED, 3);
        propertyRents.put(MonopolyFields.GREEN, 3);
        propertyRents.put(MonopolyFields.BLUE, 4);
        for (String prop : propertyCounts.keySet()) {
            int count = propertyCounts.get(prop);
            for (int i = 0; i < count; i++) {
                // the selling value seem arbitrary, but the utilities are worth a bit more
                deck.get(i).setClickableWidth(deck.get(i).width);
                deck.add(new PropertyCard(
                        String.valueOf(prop == MonopolyFields.UTILITY || prop == MonopolyFields.RAILROAD ? 2
                                : propertyRents.get(prop)),
                        propertyRents.get(prop), prop));
            }
        }
        // Add action cards (simplified, not all actions)
        String[] actions = { MonopolyFields.PASS_GO, MonopolyFields.DEAL_BREAKER, MonopolyFields.JUST_SAY_NO,
                MonopolyFields.SLY_DEAL, MonopolyFields.FORCED_DEAL, MonopolyFields.DEBT_COLLECTOR,
                MonopolyFields.BIRTHDAY };
        String[] actionValues = { "1", "5", "4", "3", "3", "3", "2" };
        int[] actionCounts = { 10, 2, 3, 3, 3, 3, 3 }; // number of each action card
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
            ((Button) drawButton).setDisabled(true); // disable draw button after drawing
            positionCards();
        }
        // also handle end turn
        if (endTurnButton.isClicked(mouseX, mouseY) && playerOneTurn) {
            switchTurns();
            playsCount = 0; // reset play count for new turn
            ((Button) drawButton).setDisabled(false); // enable draw button for new turn
        }
    }

    @Override
    public boolean playCard(Card card, Hand hand) {
        if (!isValidPlay(card)) {
            return false;
        }
        // If Money card, add to bank pile
        if (card.suit.equals("Money")) {
            ((MonopolyHand) hand).bankPile.addCard(card);
        } else if (card.suit.equals("Property")) {
            ((MonopolyHand) hand).propertyPile.addCard(card);
        } else if (card.suit.equals("Action")) {
            return true;
        }
        // Remove card from hand
        hand.removeCard(card);
        playsCount++;
        return true;
    }

    @Override
    protected boolean isValidPlay(Card card) {
        if (!((Button) drawButton).isDisabled()) {
            System.out.println("You must draw before playing cards!");
            return false;
        }
        if (playsCount >= 3) {
            System.out.println("You have already played 3 cards this turn!");
            return false;
        }
        if (card.suit.equals("Action")) {
            if (card.value.equals(MonopolyFields.FORCED_DEAL) &&
                    ((MonopolyHand) getCurrentPlayerHand()).propertyPile.getSize() == 0) {
                // Sly Deal is only valid if opponent has properties to steal
                System.out.println("No properties to steal with Forced Deal!");
                return false;
            }
        }
        return true;
    }

    @Override
    public void handleCardClick(int mouseX, int mouseY) {
        // Handle action card choices first if we're already choosing
        if (choosingAction) {
            if (isValidPlay(selectedCard)) {
                if (playActionButton.isClicked(mouseX, mouseY)) {
                    handleActionCards();
                } else if (bankActionButton.isClicked(mouseX, mouseY)) {
                    // add to bank
                    selectedCard.suit = "Money";
                    playCard(selectedCard, getCurrentPlayerHand());
                }
            }
            choosingAction = false;
            selectedCard.setSelected(false, selectedCardRaiseAmount);
            selectedCard = null;
            return;
        }

        // Use parent's selection logic of playing other cards
        super.handleCardClick(mouseX, mouseY);

        // If an action card was just selected, draw action choices
        if (selectedCard != null && selectedCard.suit.equals("Action")) {
            choosingAction = true;
        }
    }

    private void handleActionCards() {
        ((ActionCard) selectedCard).performAction();
        getCurrentPlayerHand().removeCard(selectedCard);
        playsCount++;
        positionCards();
    }

    @Override
    public void drawChoices(PApplet sketch) {
        endTurnButton.draw(sketch);
        // if playing an action card, draw choices for that action (e.g. which property
        // to steal with sly deal)
        if (choosingAction) {
            sketch.fill(255, 0, 0);
            // put it in the middle of the screen
            sketch.rect(App.gameWidth / 2 - 100, Y_START - 100, 200, 100);
            sketch.textSize(16);
            sketch.fill(255);
            sketch.text("Play Action or Bank?", App.gameWidth / 2, Y_START - 80);
            playActionButton.draw(sketch);
            bankActionButton.draw(sketch);
        } else if (playingAsAction) {
            // for now just draw a placeholder, but ideally should draw different choices
            // based on the action card being played
            sketch.fill(255, 0, 0);
            sketch.rect(App.gameWidth / 2 - 100, Y_START - 100, 200, 100);
            sketch.textSize(16);
            sketch.fill(255);
            sketch.text("Choose how to play action", App.gameWidth / 2, Y_START - 80);
        }

    }

    @Override
    public void switchTurns() {
        playerOneTurn = !playerOneTurn;
    }

    private void positionCards() {
        playerOneHand.positionCards(X_START, Y_START, HAND_SPACING, 120, HAND_SPACING);
        playerTwoHand.positionCards(X_START, 30, HAND_SPACING, 120, HAND_SPACING);
    }

    @Override
    public void handleComputerTurn() {
        // TODO: implement actual computer logic
        drawCard(playerTwoHand);
        drawCard(playerTwoHand);
        positionCards();
        switchTurns();
    }
}