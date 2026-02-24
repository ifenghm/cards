import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class MonopolyComputer {

    public static int calculateNumSets(MonopolyHand hand) {
        return calculateSets(hand).size();
    }

    public static List<String> calculateSets(MonopolyHand hand) {
        List<String> sets = new ArrayList<>();
        // check if set is complete
        HashMap<String, Integer> propertyCounts = new HashMap<>();
        for (Card c : hand.propertyPile.getCards()) {
            String color = ((PropertyCard) c).color;
            propertyCounts.put(color, propertyCounts.getOrDefault(color, 0) + 1);
        }
        for (String color : propertyCounts.keySet()) {
            if (propertyCounts.getOrDefault(color, 0) >= MonopolyDeal.propertyCounts.get(color)) {
                sets.add(color);
            }
        }
        return sets;
    }

    public static List<Card> calculateNonSetProperties(MonopolyHand hand) {
        List<Card> nonSetProperties = new ArrayList<>();
        List<String> sets = calculateSets(hand);
        for (Card c : hand.propertyPile.getCards()) {
            String color = ((PropertyCard) c).color;
            if (!sets.contains(color)) {
                nonSetProperties.add(c);
            }
        }
        return nonSetProperties;
    }

    public static boolean playCard(MonopolyDeal game) {
        MonopolyHand computerHand = (MonopolyHand) game.playerTwoHand;
        MonopolyHand opponentHand = (MonopolyHand) game.playerOneHand;

        // 1) Deal Breaker if opponent has a complete set
        if (checkDealBreakerStrategy(game, computerHand, opponentHand)) {
            return true;
        }

        // 2) Action cards for money or stealing properties
        if (actionCardStrategy(game, computerHand, opponentHand)) {
            return true;
        }

        // 3) Complete a set if possible
        if (completeSetStrategy(game, computerHand)) {
            return true;
        }

        // 4) Play money
        for (Card c : computerHand.getCards()) {
            if (c.suit.equals("Money")) {
                c.setTurned(false);
                game.playCard(c, computerHand);
                System.out.println("Computer plays money card: " + c.value);
                return true;
            }
        }
        // only play pass go after playing money cards
        for (Card c : computerHand.getCards()) {
            if (c instanceof PassGoCard) {
                game.handleActionCard((ActionCard) c);
                System.out.println("Computer plays Pass Go to draw cards");
                return true;
            }
        }

        // 5) Play property
        for (Card c : computerHand.getCards()) {
            if (c instanceof PropertyCard) {
                c.setTurned(false);
                game.playCard(c, computerHand);
                System.out.println("Computer plays property card: " + c.value);
                return true;
            }
        }

        return false;
    }

    private static boolean completeSetStrategy(MonopolyDeal game, MonopolyHand computerHand) {
        HashMap<String, Integer> propertyCounts = new HashMap<>();
        // current property counts in hand and pile
        for (Card c : computerHand.propertyPile.getCards()) {
            String color = ((PropertyCard) c).color;
            propertyCounts.put(color, propertyCounts.getOrDefault(color, 0) + 1);
        }
        for (Card c : computerHand.getCards()) {
            if (c instanceof PropertyCard) {
                String color = ((PropertyCard) c).color;
                propertyCounts.put(color, propertyCounts.getOrDefault(color, 0) + 1);
            }
        }
        // check if can complete a set
        for (Card c : computerHand.getCards()) {
            if (c instanceof PropertyCard) {
                String color = ((PropertyCard) c).color;
                int needed = MonopolyDeal.propertyCounts.getOrDefault(color, 0);
                if (needed > 0 && propertyCounts.getOrDefault(color, 0) >= needed) {
                    c.setTurned(false);
                    game.playCard(c, computerHand);
                    return true;
                }
            }
        }
        return false;
    }

    static boolean checkDealBreakerStrategy(MonopolyDeal game, MonopolyHand computerHand, MonopolyHand opponentHand) {
        List<String> opponentSets = calculateSets(opponentHand);
        for (String set : opponentSets) {
            // check if have deal breaker and can steal the set
            for (Card c : computerHand.getCards()) {
                if (c instanceof DealBreakerCard) {
                    game.handleActionCard((ActionCard) c);
                    return true;
                }
            }
        }
        return false;
    }

    static boolean actionCardStrategy(MonopolyDeal game, MonopolyHand computerHand, MonopolyHand opponentHand) {
        for (Card c : computerHand.getCards()) {
            if (c instanceof ActionCard actionCard && !(c instanceof PassGoCard) && !(c instanceof JustSayNoCard)) {
                if (!actionCard.canPlay()) continue;

                boolean isStealAction = c instanceof SlyDealCard || c instanceof ForcedDealCard;

                if (isStealAction) {
                    // calculate the best thing to steal
                    Card bestCard = bestCardToSteal(opponentHand);
                    if (bestCard != null) {
                        game.stolenCards.add((MonopolyCard) bestCard);
                    }
                }
                game.selectedCard = actionCard;
                game.handleActionCard(actionCard);
                return true;
            }
        }
        return false;
    }

    static private Card bestCardToSteal(MonopolyHand opponentHand) {
        List<Card> stealable = calculateNonSetProperties(opponentHand);
        if (stealable.isEmpty()) {
            return null;
        }
        // steal anything that makes a set for opponent, otherwise steal the most
        // expensive property
        HashMap<String, Integer> propertyCounts = new HashMap<>();
        for (Card c : opponentHand.propertyPile.getCards()) {
            String color = ((PropertyCard) c).color;
            propertyCounts.put(color, propertyCounts.getOrDefault(color, 0) + 1);
        }
        Card bestCard = stealable.get(0);
        for (Card c : stealable) {
            String color = ((PropertyCard) c).color;
            if (propertyCounts.getOrDefault(color, 0) == MonopolyDeal.propertyCounts.getOrDefault(color, 0) - 1) {
                return c;
            }
        }
        return bestCard;
    }

    public static List<MonopolyCard> selectCardsToGiveUp(MonopolyHand hand, int amountNeeded, List<MonopolyCard> stolenCards) {
        int paid = 0;
        // 1) Bank money, 2) non-set properties, 3) set properties (least valuable first)
        paid = addCardsUpTo(hand.bankPile.getCards(), amountNeeded, paid, stolenCards);
        paid = addCardsUpTo(calculateNonSetProperties(hand), amountNeeded, paid, stolenCards);

        List<Card> setProperties = new ArrayList<>(hand.propertyPile.getCards());
        setProperties.removeAll(calculateNonSetProperties(hand));
        setProperties.sort((a, b) -> Integer.parseInt(a.value) - Integer.parseInt(b.value));
        addCardsUpTo(setProperties, amountNeeded, paid, stolenCards);
        System.out.println(stolenCards + " cards selected to give up: ");
        return stolenCards;
    }

    private static int addCardsUpTo(List<Card> cards, int amountNeeded, int paid, List<MonopolyCard> stolenCards) {
        for (Card card : cards) {
            if (paid >= amountNeeded) break;
            paid += Integer.parseInt(card.value);
            stolenCards.add((MonopolyCard) card);
        }
        return paid;
    }
}
