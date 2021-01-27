package dt.ai;

/**
 * @author Emiel Rous and Wouter Koning
 * The various types of AI there are available.
 */

public enum AITypes {
    HUMAN,
    RANDOM {
        @Override
        public AI getAIClass() {
            return new RandomAI();
        }
    },
    GREEDY {
        @Override
        public AI getAIClass() {
            return new GreedyAI();
        }
    },
    MINIMAX {
        @Override
        public AI getAIClass() {
            return new MiniMaxAI();
        }
    },
    MINIMAX2 {
        @Override
        public AI getAIClass() {
            return new MiniMaxAI2();
        }
    };

    /**
     * A method which returns all of the AI types in a nice String, seperated by newlines.
     *
     * @return A string containing all the AI types.
     */
    static public String allToString() {

        StringBuilder returnString = new StringBuilder();
        returnString.append(AITypes.values()[0].toString());

        AITypes[] allTypes = AITypes.values();
        for (int i = 1; i < allTypes.length; i++) {
            returnString.append(System.lineSeparator());
            returnString.append(allTypes[i]);
        }
        returnString.append(System.lineSeparator());
        return returnString.toString();
    }

    /**
     * Returns a new instance of the type of the AI that has been chosen, and returns null for Human.
     *
     * @return A new instance of the type of the AI that has been chosen, and returns null for Human.
     */
    public AI getAIClass() {
        return null;
    }
}
