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

    Button playActionButton = new Button(App.gameWidth / 2 - 80, Y_START - 50, 80, drawButtonHeight, "Play Action");
    Button bankActionButton = new Button(App.gameWidth / 2 + 10, Y_START - 50, 80, drawButtonHeight, "Bank");
    // counts of each property types
    static HashMap<String, Integer> propertyCounts;
    ClickableRectangle endTurnButton = new ClickableRectangle(buttonsX, Y_START + drawButtonHeight + 15, 80,
            drawButtonHeight, "End");

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

    private Card handleStealingChoice(Card clickedCard) {
        if (clickedCard == null || !((MonopolyCard) clickedCard).glowing) {
            return null; // Ignore clicks on non-glowing cards or empty space
        }

        // Add the clicked card or set to stolen cards
        if (actionCardBeingPlayed.getAction().equals(MonopolyFields.FORCED_DEAL)) {
            MonopolyHand currentHand = (MonopolyHand) getCurrentPlayerHand();
            MonopolyHand opponentHand = playerOneTurn ? (MonopolyHand) playerTwoHand : (MonopolyHand) playerOneHand;
            // Select opponent property to steal
            if (stolenCards.isEmpty() && opponentHand.propertyPile.getCards().contains(clickedCard)) {
                stolenCards.add((MonopolyCard) clickedCard);
            }
            // Select property to trade from current player
            if (currentHand.propertyPile.getCards().contains(clickedCard)) {
                tradeProperty = (PropertyCard) clickedCard;
            }
            // If both selected, proceed
            if (!stolenCards.isEmpty() && tradeProperty != null) {
                ((MonopolyHand) playerOneHand).clearGlowingCards();
                ((MonopolyHand) playerTwoHand).clearGlowingCards();
                handleActionCard(actionCardBeingPlayed);
                actionCardBeingPlayed = null;
                isStealing = false;
            }
            // we return early for forced deals executed by the player to make sure we have both cards chosen
            return clickedCard;
        } else if (actionCardBeingPlayed.getAction().equals(MonopolyFields.DEAL_BREAKER)) {
            // If it's a Deal Breaker, add the whole set of the clicked card
            String colorToSteal = ((PropertyCard) clickedCard).color;
            MonopolyHand opponentHand = playerOneTurn ? (MonopolyHand) playerTwoHand : (MonopolyHand) playerOneHand;
            for (Card card : opponentHand.propertyPile.getCards()) {
                if (((PropertyCard) card).color.equals(colorToSteal)) {
                    addToStolenCards((MonopolyCard) card);
                }
            }
        } else {
            stolenCards.add((MonopolyCard) clickedCard);
        }
        if (playerOneTurn) {
            // Player is stealing from computer (Sly Deal, etc.)
            isStealing = false;
            ((MonopolyHand) playerOneHand).clearGlowingCards();
            ((MonopolyHand) playerTwoHand).clearGlowingCards();
            handleActionCard((ActionCard) actionCardBeingPlayed);
            actionCardBeingPlayed = null;
        } else {
            // Computer is collecting from player (Debt Collector, Birthday)
            System.out.println("Selected card to give up: " + clickedCard.value + " of " + clickedCard.suit);
            int cardValue = Integer.parseInt(clickedCard.value);
            neededMoneyFromOpponent -= cardValue;

            // Check if player has paid enough or they are broke (no more cards to give)
            MonopolyHand opponentHand = playerOneTurn ? (MonopolyHand) playerTwoHand : (MonopolyHand) playerOneHand;

            // also check if the opponentHand is empty after this card is selected as
            // payment, in which case they are broke and the action can be resolved
            ArrayList<Card> remainingWealth = new ArrayList<>(opponentHand.bankPile.getCards());
            remainingWealth.addAll(opponentHand.propertyPile.getCards());
            remainingWealth.removeAll(stolenCards);

            if (neededMoneyFromOpponent <= 0 || remainingWealth.isEmpty()) {
                isStealing = false;
                ((MonopolyHand) playerOneHand).clearGlowingCards();
                ((MonopolyHand) playerTwoHand).clearGlowingCards();
                handleActionCard((ActionCard) actionCardBeingPlayed);
                actionCardBeingPlayed = null;
                neededMoneyFromOpponent = 0;
            }
        }
        return clickedCard;
    }

    @Override
    public void handleCardClick(int mouseX, int mouseY) {
        // Handle stealing choices if in stealing mode
        if (isStealing) {
            Card clickedCard = getClickedCardFromAllPiles(mouseX, mouseY);
            handleStealingChoice(clickedCard);
            return;
        }

        // Handle action card choices first if we're already choosing
        if (choosingAction) {
            if (isValidPlay(selectedCard)) {
                if (playActionButton.isClicked(mouseX, mouseY)) {
                    handleActionCard((ActionCard) selectedCard);
                    return;
                } else if (bankActionButton.isClicked(mouseX, mouseY)) {
                    // add to bank
                    selectedCard.suit = "Money";
                    playCard(selectedCard, getCurrentPlayerHand());
                    return;
                }
                // deselect the card if we click outside the buttons while choosing action
                choosingAction = false;
                selectedCard.setSelected(false, selectedCardRaiseAmount);
                selectedCard = null;
            }
            // If we're choosing action but didn't click a button, just return
            return;
        }

        // Use parent's selection logic of playing other cards
        super.handleCardClick(mouseX, mouseY);

        // If an action card was just selected, draw action choices
        if (selectedCard != null && selectedCard.suit.equals("Action")) {
            choosingAction = true;
        }
    }

    public void handleActionCard(ActionCard actionCard) {
        String action = actionCard.getAction();
        System.out.println("Starting action card: " + action);
        boolean isDebtOrBirthday = MonopolyFields.DEBT_COLLECTOR.equals(action)
                || MonopolyFields.BIRTHDAY.equals(action);

        // For Debt Collector and Birthday, check if we need player to select cards to
        // pay
        if (isDebtOrBirthday && !playerOneTurn && stolenCards.isEmpty()) {
            // Computer is playing on player - player needs to choose cards to pay
            isStealing = true;
            actionCardBeingPlayed = actionCard;
            setGlowingCardsForAction(actionCard);
            return;
        }

        // For other stealing actions, check if we need to select what to steal
        if (actionCard.requiresStealingChoice() && !isDebtOrBirthday && stolenCards.isEmpty()) {
            isStealing = true; // will trigger stealing choice UI
            actionCardBeingPlayed = actionCard;
            setGlowingCardsForAction(actionCard);
            return;
        }

        // If player is playing Debt Collector/Birthday on computer, computer
        // automatically selects cards
        if (isDebtOrBirthday && playerOneTurn && stolenCards.isEmpty()) {
            MonopolyHand opponentHand = (MonopolyHand) playerTwoHand;
            int amount = MonopolyFields.DEBT_COLLECTOR.equals(action) ? 5 : 2;
            MonopolyComputer.selectCardsToGiveUp(opponentHand, amount, stolenCards);
        }

        // assuming all choices have been made at this point and isStealing is false

        System.out.println("Finishing action card: " + action);
        actionCard.performAction();
        getCurrentPlayerHand().removeCard(actionCard);
        choosingAction = false;
        playsCount++;
        ((MonopolyHand) playerOneHand).clearGlowingCards();
        ((MonopolyHand) playerTwoHand).clearGlowingCards();
        positionCards();
    }

    private void setGlowingCardsForAction(ActionCard actionCard) {

        MonopolyHand opponentHand = playerOneTurn ? (MonopolyHand) playerTwoHand : (MonopolyHand) playerOneHand;
        String action = actionCard.getAction();

        if (MonopolyFields.SLY_DEAL.equals(action)) {
            // Glow non-set properties from opponent
            for (Card card : MonopolyComputer.calculateNonSetProperties(opponentHand)) {
                ((MonopolyCard) card).glowing = true;
            }
        } else if (MonopolyFields.FORCED_DEAL.equals(action)) {
            // Glow non-set properties from both players
            for (Card card : MonopolyComputer.calculateNonSetProperties(opponentHand)) {
                ((MonopolyCard) card).glowing = true;
            }
            MonopolyHand currentHand = (MonopolyHand) getCurrentPlayerHand();
            for (Card card : MonopolyComputer.calculateNonSetProperties(currentHand)) {
                ((MonopolyCard) card).glowing = true;
            }
        } else if (MonopolyFields.DEAL_BREAKER.equals(action)) {
            // Glow complete sets from opponent
            for (String color : MonopolyComputer.calculateSets(opponentHand)) {
                for (Card card : opponentHand.propertyPile.getCards()) {
                    if (((PropertyCard) card).color.equals(color)) {
                        ((MonopolyCard) card).glowing = true;
                    }
                }
            }
        } else if (MonopolyFields.DEBT_COLLECTOR.equals(action) || MonopolyFields.BIRTHDAY.equals(action)) {
            // Only glow cards if computer is playing on player (player needs to choose what
            // to give)
            if (!playerOneTurn) {
                // Computer is playing on player one - player must choose what to give
                for (Card card : opponentHand.bankPile.getCards()) {
                    ((MonopolyCard) card).glowing = true;
                }
                for (Card card : opponentHand.propertyPile.getCards()) {
                    ((MonopolyCard) card).glowing = true;
                }
                neededMoneyFromOpponent = MonopolyFields.DEBT_COLLECTOR.equals(action) ? 5 : 2;
            }
            // Note: If player is playing on computer, the computer's card selection
            // is handled in handleActionCard() before performAction() is called
        }
    }

    private Card getClickedCardFromAllPiles(int mouseX, int mouseY) {
        // Check all possible locations for cards
        Card clicked = getClickedCard(mouseX, mouseY);
        if (clicked != null)
            return clicked;

        // Check bank piles
        for (Card card : ((MonopolyHand) playerOneHand).bankPile.getCards()) {
            if (card.isClicked(mouseX, mouseY))
                return card;
        }
        for (Card card : ((MonopolyHand) playerTwoHand).bankPile.getCards()) {
            if (card.isClicked(mouseX, mouseY))
                return card;
        }

        // Check property piles
        for (Card card : ((MonopolyHand) playerOneHand).propertyPile.getCards()) {
            if (card.isClicked(mouseX, mouseY))
                return card;
        }
        for (Card card : ((MonopolyHand) playerTwoHand).propertyPile.getCards()) {
            if (card.isClicked(mouseX, mouseY))
                return card;
        }

        return null;
    }

    private void addToStolenCards(MonopolyCard opponentCard) {
        stolenCards.add(opponentCard);
    }

    private boolean canPlayActionCard(MonopolyCard card) {
        if (!card.suit.equals("Action")) {
            return false;
        }
        boolean opponentHasProperties = playerOneTurn ? ((MonopolyHand) playerTwoHand).propertyPile.getSize() > 0
                : ((MonopolyHand) playerOneHand).propertyPile.getSize() > 0;
        boolean opponentHasMoney = playerOneTurn ? ((MonopolyHand) playerTwoHand).bankPile.getSize() > 0
                : ((MonopolyHand) playerOneHand).bankPile.getSize() > 0;
        boolean opponentHasStealableProperties = !(playerOneTurn
                ? MonopolyComputer.calculateNonSetProperties((MonopolyHand) playerTwoHand).isEmpty()
                : MonopolyComputer.calculateNonSetProperties((MonopolyHand) playerOneHand).isEmpty());
        switch (((ActionCard) card).getAction()) {
            case MonopolyFields.SLY_DEAL:
                return opponentHasStealableProperties;
            case MonopolyFields.FORCED_DEAL:
                boolean youHaveProperties = ((MonopolyHand) getCurrentPlayerHand()).propertyPile.getSize() > 0;
                // Forced Deal is only valid if opponent has properties to steal
                if (!opponentHasStealableProperties) {
                    System.out.println("No properties to steal with Forced Deal!");
                    return false;
                }
                return youHaveProperties;
            case MonopolyFields.DEAL_BREAKER:
                // can only play Deal Breaker if opponent has a complete set to steal
                return playerOneTurn ? MonopolyComputer.calculateNumSets((MonopolyHand) playerTwoHand) > 0
                        : MonopolyComputer.calculateNumSets((MonopolyHand) playerOneHand) > 0;
            case MonopolyFields.DEBT_COLLECTOR, MonopolyFields.BIRTHDAY:
                // can only play Debt Collector if opponent has money in bank or properties to
                return opponentHasMoney || opponentHasProperties;
            default:
                return true; // other action cards don't have specific play conditions
        }
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

    }

    @Override
    public void switchTurns() {
        playerOneTurn = !playerOneTurn;
        playsCount = 0;
    }

    public boolean isWaitingForPlayerChoice() {
        return choosingAction || isStealing;
    }

    private void positionCards() {
        playerOneHand.positionCards(X_START, Y_START, HAND_SPACING, 120, HAND_SPACING);
        playerTwoHand.positionCards(X_START, 30, HAND_SPACING, 120, HAND_SPACING);
    }

    @Override
    public void handleComputerTurn() {
        // Don't execute computer turn if player is still making choices
        if (isWaitingForPlayerChoice()) {
            return;
        }
        drawCard(playerTwoHand);
        drawCard(playerTwoHand);
        while (playsCount < 3 && !isWaitingForPlayerChoice() && MonopolyComputer.playCard(this)) {
            // Keep playing cards until 3 have been played or no more cards can be played
            // Also break if we're waiting for player choice
        }
        if (!isWaitingForPlayerChoice()) {
            positionCards();
            switchTurns();
        }
    }
}