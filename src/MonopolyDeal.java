import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    List<MonopolyCard> stolenCards = new ArrayList<>(); // cards stolen by the current action, to be displayed as
    ActionCard actionCardBeingPlayed; // the action card that is currently being played, to be displayed as well
    PropertyCard tradeProperty; // for forced deal: property the current player will give
    boolean isStealing = false; // whether the stealing part of the action is happening
    int neededMoneyFromOpponent = 0; // for action cards that require payment, track how much is needed from opponent

    boolean canNope = false; // whether the current action can be noped
    boolean choosingNope = false; // whether player is choosing to just say no to an action
    boolean noped = false; // whether the current action has been noped

    Button playActionButton = new Button(App.gameWidth / 2 - 80, Y_START - 50, 80, drawButtonHeight, "Play Action");
    Button bankActionButton = new Button(App.gameWidth / 2 + 10, Y_START - 50, 80, drawButtonHeight, "Bank");
    // counts of each property types
    static HashMap<String, Integer> propertyCounts;
    ClickableRectangle endTurnButton = new ClickableRectangle(buttonsX, Y_START + drawButtonHeight + 15, 80,
            drawButtonHeight, "End");

    MonopolyDeal() {
        initializeGame();
    }

    @Override
    protected void initializeGame() {
        super.initializeGame();
        // initialize all the state variables that won't be reset in the constructor
        playsCount = 0;
        stolenCards = new ArrayList<>();
        actionCardBeingPlayed = null;
        tradeProperty = null;
        isStealing = false;
        neededMoneyFromOpponent = 0;
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

    MonopolyHand currentHand() {
        return (MonopolyHand) getCurrentPlayerHand();
    }

    MonopolyHand opponentHand() {
        return playerOneTurn ? (MonopolyHand) playerTwoHand : (MonopolyHand) playerOneHand;
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
        // Add action cards using specific subclasses for polymorphism
        for (int i = 0; i < 10; i++)
            deck.add(new PassGoCard("1", this));
        for (int i = 0; i < 2; i++)
            deck.add(new DealBreakerCard("5", this));
        for (int i = 0; i < 3; i++)
            deck.add(new JustSayNoCard("4", this));
        for (int i = 0; i < 3; i++)
            deck.add(new SlyDealCard("3", this));
        for (int i = 0; i < 3; i++)
            deck.add(new ForcedDealCard("3", this));
        for (int i = 0; i < 3; i++)
            deck.add(new PaymentCard("3", MonopolyFields.DEBT_COLLECTOR, 5, this));
        for (int i = 0; i < 3; i++)
            deck.add(new PaymentCard("2", MonopolyFields.BIRTHDAY, 2, this));

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
            deselectCard();
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
            return true; // handled later in handleActionCard()
        }
        // Remove card from hand
        deselectCard();
        hand.removeCard(card);
        playsCount++;
        return true;
    }

    @Override
    protected boolean isValidPlay(Card card) {
        if (!playerOneTurn) {
            return true; // computer can play any card , won't play invalid
        }
        // Allow other player to "play" card during stealing
        if (isStealing) {
            return true;
        }
        if (!ableToPlayNow()) {
            return false;
        }
        return selectedCard != null;
    }

    private Card handleStealingChoice(Card clickedCard) {
        if (clickedCard == null) {
            // change to cancel
            return null;
        }

        if (playerOneTurn) {
            // Player is selecting cards to steal
            boolean done = actionCardBeingPlayed.handleStealingChoice(clickedCard);
            System.out.println("handleStealingChoice result: done=" + done
                    + " | tradeProperty=" + (tradeProperty != null ? tradeProperty.color : "null")
                    + " | stolenCards=" + (stolenCards.isEmpty() ? "empty" : stolenCards.getFirst()));
            if (done) {
                finishStealingAction();
            }

        } else {
            // Computer is collecting from player (Debt Collector, Birthday)
            stolenCards.add((MonopolyCard) clickedCard);
            System.out.println("Selected card to give up: " + clickedCard.value + " of " + clickedCard.suit);
            int cardValue = Integer.parseInt(clickedCard.value);
            neededMoneyFromOpponent -= cardValue;

            // Transfer card immediately so player sees it leave
            transferCards(stolenCards, opponentHand(), currentHand());

            // Check if player has paid enough or they are broke
            ArrayList<Card> remainingWealth = new ArrayList<>(opponentHand().bankPile.getCards());
            remainingWealth.addAll(opponentHand().propertyPile.getCards());

            if (neededMoneyFromOpponent <= 0 || remainingWealth.isEmpty()) {
                finishStealingAction();
            }
        }
        return clickedCard;
    }

    private boolean ableToPlayNow() {
        if (!((Button) drawButton).isDisabled()) {
            System.out.println("You must draw before playing cards!");
            return false;
        }
        if (playsCount >= 3) {
            System.out.println("You have already played 3 cards this turn!");
            return false;
        }
        return true;
    }

    @Override
    public void handleCardClick(int mouseX, int mouseY) {
        // Handle stealing choices first if in stealing mode
        if (isStealing) {
            Card clickedCard = getClickedCardFromAllPiles(mouseX, mouseY);
            handleStealingChoice(clickedCard);
            return;
        }
        // Handle action card choices first if we're already choosing
        if (choosingAction) {
            if (playActionButton.isClicked(mouseX, mouseY)) {
                if (!ableToPlayNow()) {
                    deselectCard();
                    choosingAction = false;
                    return;
                }
                handleActionCard((ActionCard) selectedCard);
                return;
            } else if (bankActionButton.isClicked(mouseX, mouseY)) {
                // add to bank
                if (isValidPlay(selectedCard)) {
                    selectedCard.suit = "Money";
                    playCard(selectedCard, getCurrentPlayerHand());
                }
                return;
            }
            deselectCard();
            choosingAction = false;
            return;
        }

        // Use parent's selection logic of playing other cards
        super.handleCardClick(mouseX, mouseY);

        // If an action card was just selected, draw action choices
        if (selectedCard != null && selectedCard.suit.equals("Action")) {
            choosingAction = true;
        }
    }

    // called once card is selected, and after any required stolenCards are selected
    public void handleActionCard(ActionCard actionCard) {
        String action = actionCard.getAction();
        System.out.println("Starting action card: " + action);
        boolean isPayment = actionCard instanceof PaymentCard;

        // For action cards that require stealing, if no cards have been selected yet,
        // set up the stealing state
        if (actionCard.requiresStealingChoice() && stolenCards.isEmpty()) {
            if (isPayment && playerOneTurn) {
                // Player plays debt/birthday on computer — computer auto-selects
                int amount = ((PaymentCard) actionCard).amount;
                MonopolyComputer.selectCardsToGiveUp(opponentHand(), amount, stolenCards);
            } else if (playerOneTurn || isPayment) {
                // Player needs to choose cards to steal, or computer plays debt/birthday on
                // player
                isStealing = true;
                actionCardBeingPlayed = actionCard;
                actionCard.setStealableCards();
                System.out.println("Waiting for player to select cards for action: " + action);
                return; // won't finish, waiting
            }
        }
        // assuming all choices have been made at this point and isStealing is false
        System.out.println("Finishing action card: " + action);
        performActionCard(actionCard);
        checkWinCondition();
    }

    private void finishStealingAction() {
        isStealing = false;
        currentHand().clearGlowingCards();
        opponentHand().clearGlowingCards();
        handleActionCard(actionCardBeingPlayed);
        actionCardBeingPlayed = null;
        neededMoneyFromOpponent = 0;
    }

    public void performActionCard(ActionCard actionCard) {
        actionCard.performAction();

        // Clear stolen cards state
        for (MonopolyCard c : stolenCards) {
            c.setSelected(false, 0);
        }
        stolenCards.clear();

        // Remove the action card from player's hand
        currentHand().removeCard(actionCard);
        choosingAction = false;
        playsCount++;
        currentHand().clearGlowingCards();
        opponentHand().clearGlowingCards();
        positionCards();
    }

    void transferCards(List<MonopolyCard> cards, MonopolyHand fromHand, MonopolyHand toHand) {
        for (MonopolyCard card : cards) {
            if (card instanceof PropertyCard) {
                fromHand.propertyPile.removeCard(card);
                toHand.propertyPile.addCard(card);
            } else {
                fromHand.bankPile.removeCard(card);
                toHand.bankPile.addCard(card);
            }
            positionCards(); // Reposition after each card transfer for visual update
        }
    }

    // Check all possible locations for cards
    private Card getClickedCardFromAllPiles(int mouseX, int mouseY) {
        // Check player's hand first
        Card clicked = getClickedCard(mouseX, mouseY);
        if (clicked != null)
            return clicked;

        ArrayList<Card> allCards = new ArrayList<>();
        allCards.addAll(currentHand().bankPile.getCards());
        allCards.addAll(opponentHand().bankPile.getCards());
        allCards.addAll(currentHand().propertyPile.getCards());
        allCards.addAll(opponentHand().propertyPile.getCards());
        for (Card card : allCards) {
            if (card.isClicked(mouseX, mouseY)) {
                return card;
            }
        }
        return null;
    }

    private boolean canPlayActionCard(MonopolyCard card) {
        if (!(card instanceof ActionCard)) {
            return false;
        }
        return ((ActionCard) card).canPlay();
    }

    @Override
    public void drawChoices(PApplet sketch) {
        sketch.push();
        endTurnButton.draw(sketch);
        // track playCount
        sketch.textSize(16);
        sketch.textAlign(sketch.LEFT, sketch.TOP);
        sketch.text("Plays: " + playsCount + "/3", buttonsX, Y_START + drawButtonHeight * 2.5f);
        sketch.pop();
        // if playing an action card, draw choices for that action (e.g. which property
        // to steal with sly deal)
        if (choosingAction) {
            sketch.fill(255, 0, 0);
            // put it in the middle of the screen
            sketch.rect(App.gameWidth / 2 - 100, Y_START - 100, 200, 100);
            sketch.textSize(16);
            sketch.fill(255);
            sketch.text("Play Action or Bank?", App.gameWidth / 2, Y_START - 80);
            playActionButton.setDisabled(!canPlayActionCard((MonopolyCard) selectedCard));
            playActionButton.draw(sketch);
            bankActionButton.draw(sketch);
        }
        if (isStealing && !playerOneTurn) {
            // Computer is playing on player - player must pay
            sketch.fill(255, 150, 0);
            sketch.rect(App.gameWidth / 2 - 150, 400, 300, 150);
            sketch.textSize(16);
            sketch.fill(0);
            if (actionCardBeingPlayed != null) {
                String actionText = ((ActionCard) actionCardBeingPlayed).getAction();
                sketch.text("Computer plays: " + actionText, App.gameWidth / 2, 420);
                sketch.text("Select cards to pay: $" + neededMoneyFromOpponent, App.gameWidth / 2, 440);
            }
        }

        // Tooltip on hover during stealing
        if (isStealing) {
            Card hovered = getClickedCardFromAllPiles(sketch.mouseX, sketch.mouseY);
            if (hovered != null) {
                String label = "$" + hovered.value + " " + hovered.suit;
                if (hovered instanceof PropertyCard) {
                    label = ((PropertyCard) hovered).color + " property ($" + hovered.value + ")";
                } else if (hovered instanceof ActionCard) {
                    label = ((ActionCard) hovered).getAction();
                }
                sketch.push();
                sketch.fill(0, 200);
                float tw = sketch.textWidth(label) + 16;
                sketch.rect(sketch.mouseX + 10, sketch.mouseY - 25, tw, 24, 4);
                sketch.fill(255);
                sketch.textSize(13);
                sketch.textAlign(sketch.LEFT, sketch.TOP);
                sketch.text(label, sketch.mouseX + 18, sketch.mouseY - 22);
                sketch.pop();
            }
        }

    }

    @Override
    public void switchTurns() {
        playerOneTurn = !playerOneTurn;
        playsCount = 0;
        // Clear any active states before switching turns
        choosingAction = false;
        isStealing = false;
        actionCardBeingPlayed = null;
        deselectCard();
    }

    private void positionCards() {
        playerOneHand.positionCards(X_START, Y_START, HAND_SPACING, 120, HAND_SPACING);
        playerTwoHand.positionCards(X_START, 30, HAND_SPACING, 120, HAND_SPACING);
    }

    @Override
    public void handleComputerTurn() {
        // Don't execute computer turn if player is still making choices
        if (isStealing) {
            return;
        }
        drawCard(playerTwoHand);
        drawCard(playerTwoHand);
        while (playsCount < 3 && !isStealing && MonopolyComputer.playCard(this)) {
            // Keep playing cards until 3 have been played or no more cards can be played
            // Also break if we're waiting for player choice for a steal
        }
        if (!isStealing && gameActive) {
            positionCards();
            switchTurns();
        }
    }

    @Override
    public void checkWinCondition() {
        int playerOneSets = MonopolyComputer.calculateNumSets(currentHand());
        int playerTwoSets = MonopolyComputer.calculateNumSets(opponentHand());

        if (playerOneSets >= 3) {
            System.out.println("Player One wins with " + playerOneSets + " complete sets!");
            gameActive = false;
        } else if (playerTwoSets >= 3) {
            System.out.println("Computer wins with " + playerTwoSets + " complete sets!");
            gameActive = false;
        }
    }
}