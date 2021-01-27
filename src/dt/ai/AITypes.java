package dt.ai;

public enum AITypes {
    HUMAN,
    RANDOM
            {
                @Override
                public AI getAIClass(){
                    return new RandomAI();
                }
            },
    GREEDY
            {
                @Override
                public AI getAIClass(){
                    return new GreedyAI();
                }
            },
    MINIMAX
            {
                @Override
                public AI getAIClass(){
                    return new MiniMaxAI();
                }
            },
    MINIMAX2
            {
                @Override
                public AI getAIClass(){
                    return new MiniMaxAI2();
                }
            };

    static public String allToString(){

        StringBuilder returnString = new StringBuilder();
        returnString.append(AITypes.values()[0].toString());

        AITypes[] allTypes = AITypes.values();
        for(int i = 1; i < allTypes.length; i++) {
//            returnString.append(",");
            returnString.append(System.lineSeparator());
            returnString.append(allTypes[i]);
        }
        returnString.append(System.lineSeparator());
        return returnString.toString();
    }

    public AI getAIClass() {
        return null;
    }
}
