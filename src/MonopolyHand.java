import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import processing.core.PApplet;

public class MonopolyHand extends Hand {
    // Bank Pile and Property Pile
    Hand bankPile = new Hand(); // display in row
    Hand propertyPile = new Hand(); // display in grid
    int playerNum, x, y;
    int width = 900;
    int height = 300;

    MonopolyHand(int playerNum) {
        this.playerNum = playerNum;
        switch (playerNum) {
            case 1: // player one hand is at the bottom
                x = 50;
                y = MonopolyDeal.Y_START - height;
                break;
            case 2: // player two hand is at the top
                x = 50;
                y = 30;
                break;
            default:
                x = 50;
                y = 400;
        }
    }

    @Override
    public void draw(PApplet sketch) {
        sketch.push();
        super.draw(sketch);

        // position bank pile in a row away from hand
        sketch.noFill();
        sketch.stroke(0, 255, 0);
        sketch.strokeWeight(3);
        sketch.rect(x - 10, y + height / 2 - 10, width / 3, height / 2, 10);
        sketch.text("Bank", x, y + height / 2 + 10);
        positionBankPile();
        bankPile.draw(sketch);

        // position property pile in a grid away from hand
        // maybe can find a way to mirror the layout instead of hardcoding for
        // player one and player two
        sketch.noFill();
        sketch.stroke(255, 0, 0);
        sketch.rect(x + width / 3 + 10, y + height / 2 - 10, width / 2, height / 2, 10);
        sketch.text("Properties", x + width / 3 + 20, y + height / 2 + 10);
        propertyPile.positionCardsInGrid(x + width / 3 + 20, y + height / 2 + 10, 40, 60, 40, 10);
        propertyPile.draw(sketch);
        sketch.pop();
    }

    void positionBankPile() {
        int cardW = 40;
        int cardH = 60;
        int colGap = 10;
        int rowGap = 20;
        int gridX = x;
        int gridY = y + height / 2 + 10;

        HashMap<String, int[]> valueSlots = new HashMap<>();
        valueSlots.put("10", new int[] { 0, 0 });
        valueSlots.put("5", new int[] { 1, 0 });
        valueSlots.put("4", new int[] { 2, 0 });
        valueSlots.put("3", new int[] { 0, 1 });
        valueSlots.put("2", new int[] { 1, 1 });
        valueSlots.put("1", new int[] { 2, 1 });

        HashMap<String, Integer> valueStackCounts = new HashMap<>();
        for (Card card : bankPile.getCards()) {
            int[] slot = valueSlots.getOrDefault(card.value, new int[] { 0, 1 });
            int stackIndex = valueStackCounts.getOrDefault(card.value, 0);
            int cardX = gridX + slot[0] * (cardW + colGap) + stackIndex * 4;
            int cardY = gridY + slot[1] * (cardH + rowGap) - stackIndex * 4;
            card.setPosition(cardX, cardY, cardW, cardH);
            card.setSize(cardW, cardH);
            card.setClickableWidth(cardW);
            valueStackCounts.put(card.value, stackIndex + 1);
        }
    }

    public void clearGlowingCards() {
        ArrayList<Card> allCards = new ArrayList<>();
        allCards.addAll(getCards());
        allCards.addAll(bankPile.getCards());
        allCards.addAll(propertyPile.getCards());
        for (Card card : allCards) {
            ((MonopolyCard) card).glowing = false;
        }
    }

    public int getMoney() {
        int total = 0;
        for (Card card : bankPile.getCards()) {
            total += Integer.parseInt(card.value);
        }
        return total;
    }
}
