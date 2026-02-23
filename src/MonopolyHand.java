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
        bankPile.positionCards(x, y + height/2+10, 80, 120, 20);
        bankPile.draw(sketch);

        // position property pile in a grid away from hand
        // maybe can find a way to mirror the layout instead of hardcoding for
        // player one and player two
        sketch.noFill();
        sketch.stroke(255, 0, 0);
        sketch.rect(x + width / 3 + 10, y + height / 2 - 10, width / 2, height / 2, 10);
        sketch.text("Properties", x + width / 3 + 20, y + height / 2 + 10);
        propertyPile.positionCards(x + width / 3 + 20, y + height / 2 + 10, 80, 120, 60);
        propertyPile.draw(sketch);
        sketch.pop();
    }

    public int getMoney() {
        int total = 0;
        for (Card card : bankPile.getCards()) {
            total += Integer.parseInt(card.value);
        }
        return total;
    }
}
