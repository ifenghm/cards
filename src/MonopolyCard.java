import java.lang.reflect.Array;
import java.util.ArrayList;

import processing.core.PApplet;

public class MonopolyCard extends Card {
    boolean glowing = false; // for highlighting cards that can be played
    float scaleMax = .5f; //
    float scale = 1.0f; //
    float dScale = 0.01f; // how much to increase scale by each frame when glowing
    // all monopoly cards are money cards, so we can just return the value as an int

    public MonopolyCard(String value, String suit) {
        super(value, suit);
    }

    // Money, Property, Action.
    public int getMoneyNum() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0; // Non-money cards have a value of 0
        }
    } // every card has a monetary value

    @Override
    public void drawFront(PApplet sketch) {

        sketch.push();
        if (glowing) {
            sketch.translate(x + width / 2, y + height / 2);
            sketch.scale(scale);
            sketch.translate(-x - width / 2, -y - height / 2);
            scale += dScale;
            if (scale >= 1.0f + scaleMax || scale <= 1.0f) {
                dScale = -dScale; // reverse direction when reaching max or min scale
            }
        }
        super.drawFront(sketch);
        // set card color based on suit
        switch (suit) {
            case "Money":
                sketch.fill(255, 215, 0); // gold color for money cards
                break;
            case "Property":
                sketch.fill(255); // white for property cards
                break;
            case "Action":
                sketch.fill(255, 182, 193); // light pink for action cards
                break;
            default:
                sketch.fill(200);
                break;
        }
        sketch.rect(x, y, width, height);

        // amount in the upper left corner for all cards
        sketch.fill(0);
        sketch.textSize(14);
        sketch.text("$" + value, x + 10, y + 20);

        if (suit == "Money") {
            // draw a dollar sign in the center
            sketch.textSize(Math.max(1.0f, width / 2.0f));
            sketch.text("$" + value, x + width / 2 - 12, y + height / 2 + 16);
            sketch.textSize(14);
        }
        sketch.pop();
    }

    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }
}

class PropertyCard extends MonopolyCard {
    // property cards also have a baserent value and a color
    boolean inCompleteSet = false;
    int baseRent;
    String color;

    public PropertyCard(String value, int baseRent, String color) {
        super(value, "Property");
        this.baseRent = baseRent;
        this.color = color;
    }

    public boolean isInCompleteSet() {
        return inCompleteSet;
    }

    @Override
    public void drawFront(PApplet sketch) {
        sketch.push();
        super.drawFront(sketch);
        // draw the color bar at the top of the card
        switch (color) {
            case MonopolyFields.BROWN:
                sketch.fill(150, 75, 0);
                break;
            case MonopolyFields.LIGHT_BLUE:
                sketch.fill(173, 216, 230);
                break;
            case MonopolyFields.PINK:
                sketch.fill(255, 182, 193);
                break;
            case MonopolyFields.ORANGE:
                sketch.fill(255, 165, 0);
                break;
            case MonopolyFields.RED:
                sketch.fill(255, 0, 0);
                break;
            case MonopolyFields.YELLOW:
                sketch.fill(255, 255, 0);
                break;
            case MonopolyFields.GREEN:
                sketch.fill(0, 128, 0);
                break;
            case MonopolyFields.BLUE:
                sketch.fill(0, 0, 139);
                break;
            case MonopolyFields.RAILROAD:
                sketch.fill(128, 128, 128);
                break;
            case MonopolyFields.UTILITY:
                // light green
                sketch.fill(144, 238, 144);
                break;
            default:
                sketch.fill(200);
                break;
        }
        sketch.rect(x, y + 30, width, 20);

        sketch.textSize(20);
        sketch.textAlign(sketch.LEFT, sketch.CENTER);
        sketch.text("prop: " + color, x, y + height / 2 + 16);
        sketch.textSize(14);
        // if the property is in a complete set, draw a checkmark in the upper right
        // corner
        if (inCompleteSet) {
            sketch.fill(0);
            sketch.textSize(16);
            sketch.text("✓", x + width - 20, y + 20);
        }
        sketch.pop();
    }
}

class ActionCard extends MonopolyCard {
    MonopolyDeal game;
    public String action;
    ArrayList<MonopolyCard> stealableCards = new ArrayList<>(); // cards that can be stolen with this action card, set when the card is
                                            // played

    public ActionCard(String value, String action, MonopolyDeal game) {
        super(value, "Action");
        this.action = action;
        this.game = game;
    }

    @Override
    public void drawFront(PApplet sketch) {
        super.drawFront(sketch);
        sketch.textSize(20);
        sketch.textAlign(sketch.LEFT, sketch.CENTER);
        sketch.text(action, x, y + height / 2 + 16);
    }

    public boolean requiresStealingChoice() {
        return false;
    }

    public String getAction() {
        return action;
    }

    public boolean canPlay() {
        return true;
    }

    public ArrayList<MonopolyCard> getStealableCards() {
        return stealableCards;
    }

    public void setStealableCards() {
        // we can glow any just say no
        if (action.equals(MonopolyFields.JUST_SAY_NO)) {
            for (Card card : game.opponentHand().getCards()) {
                if (card instanceof ActionCard && ((ActionCard) card).action.equals(MonopolyFields.JUST_SAY_NO)) {
                    stealableCards.add((MonopolyCard) card);
                    ((MonopolyCard) card).glowing = true;
                }
            }
        }
    }

    public void performAction() {
    }

    /** Handle a card click during stealing/selection. Returns true when done. */
    public boolean handleStealingChoice(Card clickedCard) {
        game.stolenCards.add((MonopolyCard) clickedCard);
        return true;
    }
}

class PassGoCard extends ActionCard {
    public PassGoCard(String value, MonopolyDeal game) {
        super(value, MonopolyFields.PASS_GO, game);
    }

    @Override
    public void performAction() {
        if (game.deck.isEmpty()) {
            return;
        }
        game.currentHand().addCard(game.deck.remove(0));
        if (!game.currentHand().getCards().isEmpty()) {
            game.currentHand().addCard(game.deck.remove(0));
        }
    }
}

class JustSayNoCard extends ActionCard {
    public JustSayNoCard(String value, MonopolyDeal game) {
        super(value, MonopolyFields.JUST_SAY_NO, game);
    }
}

class SlyDealCard extends ActionCard {
    public SlyDealCard(String value, MonopolyDeal game) {
        super(value, MonopolyFields.SLY_DEAL, game);
    }

    @Override
    public boolean requiresStealingChoice() {
        return true;
    }

    @Override
    public boolean canPlay() {
        return !MonopolyComputer.calculateNonSetProperties(game.opponentHand()).isEmpty();
    }

    @Override
    public void setStealableCards() {
        for (Card card : MonopolyComputer.calculateNonSetProperties(game.opponentHand())) {
            ((MonopolyCard) card).glowing = true;
        }
    }

    @Override
    public void performAction() {
        game.transferCards(game.stolenCards, game.opponentHand(), game.currentHand());
    }
}

class DealBreakerCard extends ActionCard {
    public DealBreakerCard(String value, MonopolyDeal game) {
        super(value, MonopolyFields.DEAL_BREAKER, game);
    }

    @Override
    public boolean requiresStealingChoice() {
        return true;
    }

    @Override
    public boolean canPlay() {
        return MonopolyComputer.calculateNumSets(game.opponentHand()) > 0;
    }

    @Override
    public void setStealableCards() {
        for (String color : MonopolyComputer.calculateSets(game.opponentHand())) {
            for (Card card : game.opponentHand().propertyPile.getCards()) {
                if (((PropertyCard) card).color.equals(color)) {
                    ((MonopolyCard) card).glowing = true;
                }
            }
        }
    }

    @Override
    public boolean handleStealingChoice(Card clickedCard) {
        if (!getStealableCards().contains(clickedCard)) {
            return false;
        }
        String colorToSteal = ((PropertyCard) clickedCard).color;
        for (Card card : game.opponentHand().propertyPile.getCards()) {
            if (((PropertyCard) card).color.equals(colorToSteal)) {
                game.stolenCards.add((MonopolyCard) card);
            }
        }
        return true;
    }

    @Override
    public void performAction() {
        game.transferCards(game.stolenCards, game.opponentHand(), game.currentHand());
    }
}

class ForcedDealCard extends ActionCard {
    public ForcedDealCard(String value, MonopolyDeal game) {
        super(value, MonopolyFields.FORCED_DEAL, game);
    }

    @Override
    public boolean requiresStealingChoice() {
        return true;
    }

    @Override
    public boolean canPlay() {
        return !MonopolyComputer.calculateNonSetProperties(game.opponentHand()).isEmpty()
                && game.currentHand().propertyPile.getSize() > 0;
    }

    @Override
    public void setStealableCards() {
        if (game.stolenCards.isEmpty()) {
            for (Card card : MonopolyComputer.calculateNonSetProperties(game.opponentHand())) {
                ((MonopolyCard) card).glowing = true;
            }
        } else {
            game.stolenCards.get(0).glowing = true;
        }
        if (game.tradeProperty != null) {
            game.tradeProperty.glowing = true;
        } else {
            for (Card card : MonopolyComputer.calculateNonSetProperties(game.currentHand())) {
                ((MonopolyCard) card).glowing = true;
            }
        }
    }

    @Override
    public boolean handleStealingChoice(Card clickedCard) {
        // if (getSt)ealableCards().contains(clickedCard)) {
            if (game.stolenCards.isEmpty() && game.opponentHand().propertyPile.getCards().contains(clickedCard)) {
                game.stolenCards.add((MonopolyCard) clickedCard);
            }
        if (game.currentHand().propertyPile.getCards().contains(clickedCard)) {
            game.tradeProperty = (PropertyCard) clickedCard;
        }

        return !game.stolenCards.isEmpty() && game.tradeProperty != null;
    }

    @Override
    public void performAction() {
        if (!game.stolenCards.isEmpty() && game.tradeProperty != null) {
            MonopolyCard stolen = game.stolenCards.get(0);
            game.opponentHand().propertyPile.removeCard(stolen);
            game.currentHand().propertyPile.addCard(stolen);
            game.currentHand().propertyPile.removeCard(game.tradeProperty);
            game.opponentHand().propertyPile.addCard(game.tradeProperty);
            game.tradeProperty = null;
        }
    }
}

/** Covers both Debt Collector ($5) and Birthday ($2) */
class PaymentCard extends ActionCard {
    int amount;

    public PaymentCard(String value, String action, int amount, MonopolyDeal game) {
        super(value, action, game);
        this.amount = amount;
    }

    @Override
    public boolean requiresStealingChoice() {
        return true;
    }

    @Override
    public boolean canPlay() {
        return game.opponentHand().bankPile.getSize() > 0 || game.opponentHand().propertyPile.getSize() > 0;
    }

    @Override
    public void setStealableCards() {
        if (!game.playerOneTurn) {
            MonopolyHand playerHand = game.opponentHand();
            for (Card card : playerHand.bankPile.getCards()) {
                ((MonopolyCard) card).glowing = true;
            }
            for (Card card : playerHand.propertyPile.getCards()) {
                ((MonopolyCard) card).glowing = true;
            }
            game.neededMoneyFromOpponent = amount;
        }
    }

    @Override
    public void performAction() {
        game.transferCards(game.stolenCards, game.opponentHand(), game.currentHand());
    }
}

class RentCard extends MonopolyCard {
    // rent cards have a color and a rent value that depends on how many properties
    // that color the opponent has
    String[] colors; // usually 2
    int rentWithOneProperty;
    int rentWithTwoProperties;

    public RentCard(String value, String color1, String color2) {
        super(value, "Rent");
        this.colors = new String[] { color1, color2 };
    }

    public RentCard(String value) {
        super(value, "Rent");
        // wild rent card
        // this.colors =
    }

    @Override
    public void drawFront(PApplet sketch) {
        super.drawFront(sketch);
        sketch.textSize(20);
        sketch.textAlign(sketch.LEFT, sketch.CENTER);
        sketch.text("rent: " + colors[0] + ", " + colors[1], x, y + height / 2 + 16);
    }
}
