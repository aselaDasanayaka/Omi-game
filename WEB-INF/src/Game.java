/**
 * CO 324 - project 2
 * Game engine
 */

import java.util.*;

public class Game {
    private int [] cards = new int[52];
    private int [] cards_p1 = new int[13];
    private int [] cards_p2 = new int[13];
    private int [] cards_p3 = new int[13];
    private int [] cards_p4 = new int[13];
    public String messages[] = new String[4];
    private int leader;
    private int trickstate = 0;
    private int roundStarter = 0;
    public int state ='N';
    public int nextPlayer;
    private int[] cardPlayed = {-1, -1, -1, -1};
    private int[] roundmarks = {0, 0, 0, 0};
    private int[] scores = {0, 0, 0, 0};
    private int rounds =0;
    private int trump ;
    private String trump_Str=null;

    public Game() {
        for (int i = 0; i < 4; i++) {
            messages[i] = "";
        }
    }

/*
* Genarate 52 random (non repeating) numbers for suffeling
*/
    public void random (){
        Arrays.fill(cards,-1);
        Random ran = new Random();
        int i=0;
        while(i<52){
            int a = ran.nextInt(52);
            boolean check= false;

            for (int j=0;j<52;j++){
                if (a==cards[j]){
                    check= true;
                    break;
                }
            }
            if (!check){
                cards[i]=a;
                i++;
            }
        }
    }

/*
* Deal cards to 4 playes
*/
    public void dealcards(){
        random();

        for (int i=0;i<13;i++){
            cards_p1[i]= cards[i];
            cards_p2[i]= cards[13+i];
            cards_p3[i]= cards[26+i];
            cards_p4[i]= cards[39+i];
        }
        trump = cards[51];System.out.println("chose trump str int :"+trump);
        if (trump/4 ==0){ this.trump_Str = "Diamonds";}
        else if (trump/4 ==1){ this.trump_Str = "Clubs";}
        else if (trump/4 ==2){ this.trump_Str = "Harts";}
        else if (trump/4 ==3){ this.trump_Str = "Spades";}
    }

/*
* This is the key function that handle all the requests that come from servelet. All of other functions are helper function to this function
*/
    public void next(int player, String cmd){
        if (state == 'D' && cmd.equals("start")){   //Deal cards to four players 
            dealcards();
            for (int i = 0; i < 4; i++) {
                if (i != roundStarter) {
                    genMessage("Trump suit for the round is " + trump_Str, true, true, i);
                } else {
                    genMessage("Trump suit for the round is " + trump_Str + " and it is your turn.", true, true, i);
                }
            }
            leader = roundStarter;
            roundStarter =((roundStarter + 1) % 4);
            nextPlayer = leader;
        }else if(player==nextPlayer){   // Initiating playing
            if (player == leader){  // handle if player is trick leader
                cardPlayed[player] =Integer.parseInt(cmd);
                confirmPlayed(player,Integer.parseInt(cmd));
                genMessage("Good Choice!", true, true, player);
                genMessage("It is your turn now.", true, true, (nextPlayer + 1) % 4);
                trickstate = 1;
                nextPlayer =(nextPlayer + 1) % 4;

            }else if(validCard(player, Integer.parseInt(cmd))){ // handle if player is not leader
                cardPlayed[player] =Integer.parseInt(cmd);
                confirmPlayed(player,Integer.parseInt(cmd));
                trickstate++;
                if (trickstate < 4){    // Played cards are less than 4
                    genMessage("Let's see...", true, true, player);
                    genMessage("It is your turn now.", true, true,(nextPlayer + 1) % 4);
                    nextPlayer =(nextPlayer + 1) % 4;
                }else{  // played cards are equal to 4
                    checkTric();
                    if(isWinner()){ // check if there is any winner or  not game over or not
                        int winner = getWinner();
                        for (int i = 0; i < 4; i++) {
                            if (i != winner) {
                                genMessage("Player " + (winner + 1) + " won the game.", false, false, i);
                            } else {
                                genMessage("Congratulations! You won.", false, false, i);
                            }
                        }
                    }else{
                        trickstate = 0;
                        for (int i = 0; i < 4; i++) {
                            cardPlayed[i] = -1;
                        }
                        for (int i = 0; i < 4; i++) {
                            if (i != leader) {
                                genMessage("You have some chances left. Good luck!", true, true, i);
                            } else {
                                genMessage("Good job. You won the trick and now it is your turn.", true, true, i);
                            }
                        }
                        nextPlayer = leader;
                    }
                }
            }else{
                genMessage("Check again you have missed cards", true, true, player);
            }

        }else if (player != nextPlayer) {
            genMessage("Ops! It is not your turn.", true, true, player);
        }
    }

    /*
    * This function genarate messages to send players
    */
    public void genMessage(String message, boolean showHand, boolean showCards, int player){
        messages[player] = "{\"cards\":[" + getCardlist(player) + "], " + playedCardlist() + "\"showHand\" : " + showHand + ", \"showCards\" : " + showCards + " , \"message\" : \"" + message + "\"}";
    }

    /*
    * Tis is helper function to genMessage() to genarate cards
    */
    public String getCardlist(int player){
        String list="";
        if (player%4==0){list = getCardsOfPlayer(cards_p1);}
        if (player%4==1){list = getCardsOfPlayer(cards_p2);}
        if (player%4==2){list = getCardsOfPlayer(cards_p3);}
        if (player%4==3){list = getCardsOfPlayer(cards_p4);}
        return list;
    }

    /*
    *   This is helper function to getCardList() tis genarete card list as json string
    */
    public String getCardsOfPlayer(int[] card){
        String txt="";
        for (int i=0;i<12;i++) {
            if(card[i]!=-1){
                txt = txt + "{\"image\": \"cards/" + (card[i] / 13) + "_" + (card[i] % 13) + ".png\" },";
            }
        }
        if(card[12]!=-1) {
            txt = txt + "{\"image\": \"cards/" + (card[12] / 13) + "_" + (card[12] % 13) + ".png\" }";
        }
        return txt;
    }

    /*
    * This function defines played cards of playes. This is helper function to genMessage()
    */
    public String playedCardlist(){
        String list = "";
        for (int i = 0; i < 4; i++) {
            if (cardPlayed[i] == -1) {
                list = list + "\"card" + (i + 1) + "\": \"cards/blank.png\" ,";
            } else {
                list = list + "\"card" + (i + 1) + "\": \"cards/" + (cardPlayed[i] / 13) + "_" + (cardPlayed[i] % 13) + ".png\" ,";
            }
        }
        return list;
    }

    /*
    * This function can identify card return its int vale from string came from user
    */
    public int identifyCard(String name){
        String str = name.substring(name.indexOf("/") + 1, name.indexOf("."));
        String [] list = str.split("_");
        return Integer.parseInt(list[0])*13+Integer.parseInt(list[1]);
    }

    /*
    *   This function validate card that played by user
    */
    public boolean validCard(int player, int card) {
        int type = card/13;
        int leaderType = cardPlayed[leader]/13;
        int trumpType = trump/13;
        if (type == leaderType || type==trumpType) {
            return true;
        } else {
            for (int i = 0; i < 13; i++) {
                if (player%4==0) {
                    if (cards_p1[i]/13==leaderType){
                        return false;
                    }
                }else if(player%4==1){
                    if (cards_p2[i]/13==leaderType){
                        return false;
                    }
                }else if(player%4==2){
                    if (cards_p3[i]/13==leaderType){
                        return false;
                    }
                }else if(player%4==3){
                    if (cards_p4[i]/13==leaderType){
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /*
    * This function confirmed playes move
    */
    public void confirmPlayed(int player,int card){
        if(player ==0){
            for(int i=0;i<13;i++){
                if(cards_p1[i]==card){
                    cards_p1[i]=-1;
                    return;
                }
            }
        }
        else if(player ==1){
            for(int i=0;i<13;i++){
                if(cards_p2[i]==card){
                    cards_p2[i]=-1;
                    return;
                }
            }
        }
        else if(player ==2){
            for(int i=0;i<13;i++){
                if(cards_p3[i]==card){
                    cards_p3[i]=-1;
                    return;
                }
            }
        }
        else if(player ==3){
            for(int i=0;i<13;i++){
                if(cards_p4[i]==card){
                    cards_p4[i]=-1;
                    return;
                }
            }
        }
    }

    /*
    * This function evaluvate trick
    */
    public void checkTric(){

        int[] set1 = {-1, -1, -1, -1};
        int[] set2 = {-1, -1, -1, -1};
        int max = -1, maxloc = -1;

        for (int i = 0; i < 4; i++) {
            if ((cardPlayed[i] / 13) == trump/13) {
                set1[i] = (cardPlayed[i] % 13);
            }
        }
        for (int i = 0; i < 4; i++) {
            if (set1[i] > max) {
                max = set1[i];
                maxloc = i;
            }
        }
        if (max > -1) {
            leader = maxloc;
            rounds++;
            roundmarks[leader] = (roundmarks[leader] + 1);
            return;
        }
        for (int i = 0; i < 4; i++) {
            if ((cardPlayed[i] / 13) == (cardPlayed[leader] / 13)) {
                set2[i] = cardPlayed[i] % 13;
            }
        }
        for (int i = 0; i < 4; i++) {
            if (set2[i] > max) {
                max = set2[i];
                maxloc = i;
            }
        }
        leader = maxloc;
        rounds++;
        roundmarks[leader] =(roundmarks[leader] + 1);
    }

    /*
    * Check is there a winner
    */
    public boolean isWinner(){
            if (rounds == 13) {
                updateScores();
                for (int i = 0; i < 4; i++) {
                    if (scores[i] >= 10) {
                        return true;
                    }
                }
                rounds = 0;
                state = 'D';
                next(1, "start");
                return false;
            } else {
                return false;
            }
    }

    /*
    * Update trick scores
    */
    private void updateScores() {
        int max = 0, maxloc = 0;
        for (int i = 0; i < 4; i++) {
            if (max < roundmarks[i]) {
                max = roundmarks[i];
                maxloc = i;
            }
        }
        scores[maxloc] =scores[maxloc] + max;
        for (int i = 0; i < 4; i++) {
            roundmarks[i] = 0;
        }
    }

    /*
    * If there is a winner this function get winner
    */
    public int getWinner(){
        for (int i = 0; i < 4; i++) {
            if (scores[i] >= 10) {
                return i;
            }
        }
        return -1;
    }

    /*
    * Main function to test functionality of class
    */
    public static void main(String args[]){
        Game o = new Game();
        o.random();
        for (int i=0;i<52;i++)
            System.out.print(o.cards[i]+" ");
    }

}
