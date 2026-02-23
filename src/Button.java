import processing.core.PApplet;

public class Button extends ClickableRectangle {
    boolean isDisabled = false;
    public Button(int x, int y, int width, int height, String text) {
        super(x, y, width, height, text);
    }
    
    @Override
    public void draw(PApplet app) {
        if (isDisabled) {
            app.fill(150);
        } else {
            app.fill(0, 255, 122);
        }
        app.rect(x, y, width, height);
        app.fill(0);
        app.textAlign(app.CENTER, app.CENTER);
        app.text(text, x + width /2, y + height/2);
    }

    @Override
    boolean isClicked(int mouseX, int mouseY) {
        if (isDisabled) {
            return false;
        } else {
            return super.isClicked(mouseX, mouseY);
        }
    }

    public void setDisabled(boolean disabled) {
        this.isDisabled = disabled;
    }

    public boolean isDisabled() {
        return isDisabled;
    }
}
