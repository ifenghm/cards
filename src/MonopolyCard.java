import java.util.ArrayList;
import java.util.List;

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
            if (scale >= 1.0f + scaleMax || scale <= 1.0f - scaleMax) {
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
    MonopolyDeal game; // need to affect the game here
    // action cards have an action type, which is the value field.
    public String action;

    public ActionCard(String value, String action, MonopolyDeal game) {
        super(value, "Action");
        this.action = action;
        this.game = game;
    }

    public void drawFront(PApplet sketch) {
        super.drawFront(sketch);
        sketch.textSize(20);
        sketch.textAlign(sketch.LEFT, sketch.CENTER);
        sketch.text(action, x, y + height / 2 + 16);
    }

    public boolean requiresStealingChoice() {
        return !(MonopolyFields.PASS_GO.equals(action) || MonopolyFields.JUST_SAY_NO.equals(action));
    }

    public void performAction() {
        MonopolyHand opponentHand = game.playerOneTurn ? (MonopolyHand) game.playerTwoHand
                : (MonopolyHand) game.playerOneHand;
        List<MonopolyCard> stealable = game.stolenCards;
        // List<MonopolyCard> stealable = new ArrayList<>(game.stolenCards); --- IGNORE
        // ---
        if (MonopolyFields.PASS_GO.equals(action)) {
            // get 2 extra cards in hand
            game.getCurrentPlayerHand().addCard(game.deck.remove(0));
            game.getCurrentPlayerHand().addCard(game.deck.remove(0));
        } else if (MonopolyFields.SLY_DEAL.equals(action)) {
            // steal a property from opponent
            if (!stealable.isEmpty()) {
                MonopolyCard stolen = stealable.get(0);
                opponentHand.propertyPile.removeCard(stolen);
                ((MonopolyHand) game.getCurrentPlayerHand()).propertyPile.addCard(stolen);
            }
        } else if (MonopolyFields.DEAL_BREAKER.equals(action)) {
            // steal the complete set from opponent
            for (MonopolyCard c : stealable) {
                opponentHand.propertyPile.removeCard(c);
                ((MonopolyHand) game.getCurrentPlayerHand()).propertyPile.addCard(c);
            }
        } else if (MonopolyFields.FORCED_DEAL.equals(action)) {
            // trade one property with opponent
            if (!stealable.isEmpty() && game.tradeProperty != null) {
                MonopolyCard stolen = stealable.get(0);
                opponentHand.propertyPile.removeCard(stolen);
                ((MonopolyHand) game.getCurrentPlayerHand()).propertyPile.addCard(stolen);

                MonopolyHand currentHand = (MonopolyHand) game.getCurrentPlayerHand();
                currentHand.propertyPile.removeCard(game.tradeProperty);
                opponentHand.propertyPile.addCard(game.tradeProperty);
                game.tradeProperty = null;

            }
        } else if (MonopolyFields.JUST_SAY_NO.equals(action)) {
            // cancel an opponent's action

        } else if (MonopolyFields.DEBT_COLLECTOR.equals(action) || MonopolyFields.BIRTHDAY.equals(action)) {
            // force opponent to pay you $5
            // either put properties into property pile or money into bank
            for (MonopolyCard c : stealable) {
                if (c instanceof PropertyCard) {
                    opponentHand.propertyPile.removeCard(c);
                    ((MonopolyHand) game.getCurrentPlayerHand()).propertyPile.addCard(c);
                } else {
                    opponentHand.bankPile.removeCard(c);
                    ((MonopolyHand) game.getCurrentPlayerHand()).bankPile.addCard(c);
                }
            }
        }
        game.stolenCards.clear(); // clear stolen cards after performing action
        // Remove the action card from player's hand after performing action
        game.playCard(this, game.getCurrentPlayerHand());
    }

    public String getAction() {
        return action;
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
