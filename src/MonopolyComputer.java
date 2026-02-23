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
            if (c instanceof ActionCard && MonopolyFields.PASS_GO.equals(((ActionCard) c).getAction())) {
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
                if (c instanceof ActionCard && MonopolyFields.DEAL_BREAKER.equals(((ActionCard) c).getAction())) {
                    game.handleActionCard((ActionCard) c);
                    return true;
                }
            }
        }
        return false;
    }

    static boolean actionCardStrategy(MonopolyDeal game, MonopolyHand computerHand, MonopolyHand opponentHand) {
        for (Card c : computerHand.getCards()) {
            if (c instanceof ActionCard) {
                ActionCard actionCard = (ActionCard) c;
                String action = actionCard.getAction();

                boolean opponentHasMoneyOrProps = opponentHand.bankPile.getSize() > 0
                        || opponentHand.propertyPile.getSize() > 0;
                boolean opponentHasStealableProps = !calculateNonSetProperties(opponentHand).isEmpty();
                boolean youHaveProperties = computerHand.propertyPile.getSize() > 0;

                boolean isMoneyAction = (MonopolyFields.DEBT_COLLECTOR.equals(action)
                        || MonopolyFields.BIRTHDAY.equals(action)) && opponentHasMoneyOrProps;
                boolean isStealAction = (MonopolyFields.SLY_DEAL.equals(action) && opponentHasStealableProps)
                        || (MonopolyFields.FORCED_DEAL.equals(action) && opponentHasStealableProps
                                && youHaveProperties);

                if (isMoneyAction || isStealAction) {
                    if (isStealAction) {
                        // calculate the best thing to steal
                        Card bestCard = bestCardToSteal(opponentHand);
                        if (bestCard != null) {
                            game.stolenCards.add((MonopolyCard) bestCard);
                        }
                    }
                    game.selectedCard = actionCard;
                    game.handleActionCard(actionCard);
                    System.out.println("Computer plays action card: " + action);
                    return true;
                }
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
        int totalValue = 0;

        // First, try to pay with money from bank
        List<Card> bankCards = new ArrayList<>(hand.bankPile.getCards());
        for (Card card : bankCards) {
            if (totalValue >= amountNeeded)
                break;
            int value = Integer.parseInt(card.value);
            totalValue += value;
            stolenCards.add((MonopolyCard) card);
        }

        // If still need more, give up properties (non-set properties first)
        if (totalValue < amountNeeded) {
            List<Card> nonSetProperties = calculateNonSetProperties(hand);
            for (Card card : nonSetProperties) {
                if (totalValue >= amountNeeded)
                    break;
                int value = Integer.parseInt(card.value);
                totalValue += value;
                stolenCards.add((MonopolyCard) card);
            }
        }
        return stolenCards;
    }
}
