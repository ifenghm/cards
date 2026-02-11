import processing.core.PApplet;

public class App extends PApplet {

    Uno unoGame = new Uno();
    private int timer;

    public static void main(String[] args) {
        PApplet.main("App");
    }
    @Override
    public void settings() {
        size(600, 600);
        
    }
    @Override
    public void setup() {
        
    }

    @Override
    public void draw() {
        background(255);
        // Draw player hands
        for (int i = 0; i < unoGame.playerOneHand.getSize(); i++) {
            Card card = unoGame.playerOneHand.getCard(i);
            if (card != null) {
                card.draw(this);
            }
        }
        // Draw computer hand
        for (int i = 0; i < unoGame.playerTwoHand.getSize(); i++) {
            Card card = unoGame.playerTwoHand.getCard(i);
            if (card != null) {
                card.draw(this);
            }
        }
        
        // Draw draw button
        fill(200);
        unoGame.drawButton.draw(this);
        fill(0);
        textAlign(CENTER, CENTER);
        text("Draw", unoGame.drawButton.x + unoGame.drawButton.width / 2, unoGame.drawButton.y + unoGame.drawButton.height / 2);

        // Display current player
        fill(0);
        textSize(16);
        text("Current Player: " + unoGame.getCurrentPlayer(), width / 2, 20);

        // Display deck size
        text("Deck Size: " + unoGame.getDeckSize(), width / 2,
                height - 20);
        // Display last played card
        if (unoGame.getLastPlayedCard() != null) {
            unoGame.getLastPlayedCard().setPosition(width / 2 - 40, height / 2 - 60, 80, 120);
            unoGame.getLastPlayedCard().draw(this);
        }
        if (unoGame.getCurrentPlayer() == "Player Two") {
            fill(0);
            textSize(16);
            text("Computer is thinking...", width / 2, height / 2 + 80);
            timer++;
            if (timer == 100) {
                unoGame.handleComputerTurn();
                timer = 0;
            }
        }

        unoGame.drawWildChooser(this);
    }

    
    @Override
    public void mousePressed() {
        unoGame.handleDrawButtonClick(mouseX, mouseY);
        unoGame.handleCardClick(mouseX, mouseY);
    }

}
