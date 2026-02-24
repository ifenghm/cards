import processing.core.PApplet;

public class App extends PApplet {

    CardGame cardGame = new MonopolyDeal();
    static int gameWidth = 1000;
    private int timer;

    public static void main(String[] args) {
        PApplet.main("App");
    }
    @Override
    public void settings() {
        size(gameWidth, 800);   
    }

    @Override
    public void draw() {
        background(255);
        // Draw player hands
        cardGame.playerOneHand.draw(this);
        // Draw computer hand
        cardGame.playerTwoHand.draw(this);
        
        // Draw draw button
        cardGame.drawButton.draw(this);

        // Display current player
        fill(0);
        textSize(16);
        text("Current Player: " + cardGame.getCurrentPlayer(), width / 2, 20);

        // Display deck size
        text("Deck Size: " + cardGame.getDeckSize(), width / 2,
                height - 20);
        // Display last played card
        if (cardGame.getLastPlayedCard() != null) {
            cardGame.getLastPlayedCard().setPosition(width / 2 - 40, height / 2 - 60, 80, 120);
            cardGame.getLastPlayedCard().draw(this);
        }
        if (cardGame.getCurrentPlayer() == "Player Two") {
            fill(0);
            textSize(16);
            text("Computer is thinking...", width / 2, height / 2 + 80);
            timer++;
            if (timer == 100) {
                cardGame.handleComputerTurn();
                timer = 0;
            }
        }

        cardGame.drawChoices(this);
        cardGame.drawPlayAgain(this);
    }

    
    @Override
    public void mousePressed() {
        cardGame.handleDrawButtonClick(mouseX, mouseY);
        cardGame.handleCardClick(mouseX, mouseY);
        cardGame.handlePlayAgainClick(mouseX, mouseY);
    }

}
