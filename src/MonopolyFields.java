import java.util.HashMap;
import java.util.List;

public class MonopolyFields {
    static final String RAILROAD = "Railroad";
    static final String UTILITY = "Utility";
    static final String BROWN = "Brown";
    static final String LIGHT_BLUE = "Light Blue";
    static final String PINK = "Pink";
    static final String ORANGE = "Orange";
    static final String YELLOW = "Yellow";
    static final String RED = "Red";
    static final String GREEN = "Green";
    static final String BLUE = "Blue";
    static final String PASS_GO = "Pass Go";
    static final String SLY_DEAL = "Sly Deal";
    static final String FORCED_DEAL = "Forced Deal";
    static final String DEAL_BREAKER = "Deal Breaker";
    static final String JUST_SAY_NO = "Just Say No";
    static final String DEBT_COLLECTOR = "Debt Collector";
    static final String BIRTHDAY = "It's My Birthday";

    public static List<Card> createDefaultDeck(List<Card> deck, MonopolyDeal game) {
        // Fallback hardcoded deck in case CSV reading fails
        HashMap<Integer, Integer> moneyCards = new HashMap<>();
        moneyCards.put(1, 6);
        moneyCards.put(2, 5);
        moneyCards.put(3, 3);
        moneyCards.put(4, 3);
        moneyCards.put(5, 2);
        moneyCards.put(10, 1);
        for (int value : moneyCards.keySet()) {
            int count = moneyCards.get(value);
            for (int i = 0; i < count; i++) {
                deck.add(new MonopolyCard(String.valueOf(value), "Money"));
            }
        }

        game.propertyCounts = new HashMap<>();
        game.propertyCounts.put(MonopolyFields.BLUE, 2);
        game.propertyCounts.put(MonopolyFields.BROWN, 2);
        game.propertyCounts.put(MonopolyFields.UTILITY, 2);
        game.propertyCounts.put(MonopolyFields.RAILROAD, 4);
        String[] properties = { MonopolyFields.GREEN, MonopolyFields.RED, MonopolyFields.ORANGE,
                MonopolyFields.LIGHT_BLUE,
                MonopolyFields.PINK, MonopolyFields.YELLOW };
        for (String prop : properties) {
            game.propertyCounts.put(prop, 3);
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
        for (String prop : game.propertyCounts.keySet()) {
            int count = game.propertyCounts.get(prop);
            for (int i = 0; i < count; i++) {
                deck.add(new PropertyCard(
                        String.valueOf(prop == MonopolyFields.UTILITY || prop == MonopolyFields.RAILROAD ? 2
                                : propertyRents.get(prop)),
                        propertyRents.get(prop), prop));
            }
        }

        String[] actions = { MonopolyFields.PASS_GO, MonopolyFields.DEAL_BREAKER, MonopolyFields.JUST_SAY_NO,
                MonopolyFields.SLY_DEAL, MonopolyFields.FORCED_DEAL, MonopolyFields.DEBT_COLLECTOR,
                MonopolyFields.BIRTHDAY };
        String[] actionValues = { "1", "5", "4", "3", "3", "3", "2" };
        int[] actionCounts = { 10, 2, 3, 3, 3, 3, 3 };
        for (int i = 0; i < actions.length; i++) {
            for (int j = 0; j < actionCounts[i]; j++) {
                deck.add(new ActionCard(actionValues[i], actions[i], game));
            }
        }

        for (Card card : deck) {
            card.setClickableWidth(80);
        }
        return deck;
    }
}
