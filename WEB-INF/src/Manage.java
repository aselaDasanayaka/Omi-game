/**
 * CO 324 - project 2
 * Servlet class to manage reaquests
 */

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Manage extends HttpServlet {

    private static int user = 0;    // genarate IDs for users
    private static final Map<Integer, Integer> userDB = new HashMap<>();   //HashMap to store user IDs and game ID
    public static final HashMap<Integer, Game> gameDB = new HashMap<Integer, Game>();  // Hash map to store game ID and game object for that particular game
    public static final HashMap<Integer, Integer> gameSTATE = new HashMap<Integer, Integer>(); // Hash map to store game state  of particular game

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        String jsonMSG = "";
        String incomming = request.getParameter("state");
/*
* In initial state process first requests receve from clients. After 4 pl,ayes join it assign game id and game object and start the game 
*/
    if (incomming.equals("initial")){
            userDB.put(user,user/4);
            jsonMSG = "{\"id\":"+ user +"}";
            gameSTATE.put(user/4,1);
            user++;
            if (user%4==0){
                gameSTATE.put((user-1)/4,2);
                Game a = new Game();
                a.state='D';
                a.next(user-1,"start");
                gameDB.put((user-1)/4,a);
            }
        }else if(incomming.equals("wating") ){  // In waiting state all the UI updates happen polling requests come from playes brouser is handle by this state

            int incommingID = Integer.parseInt(request.getParameter("id")); // User's ID

            if (gameSTATE.get(new Integer(incommingID/4)).intValue()==1) {
                jsonMSG = "{\"cards\":[],\"showHand\" : false, \"showCards\" : false , \"message\" : \"Waiting for others to connect. Only " + (user % 4) + " players connected...\",\"stop\" : false}";
            }

            if (gameSTATE.get(new Integer(incommingID/4)).intValue()==2) {
                Game engine = gameDB.get(new Integer(incommingID / 4));
                jsonMSG = engine.messages[incommingID % 4];
            }

        }else if(incomming.equals("playing") ){// In playing state all requests that come from playCard() function is handled.
            int incommingID = Integer.parseInt(request.getParameter("id"));
            String cardT = request.getParameter("card");
            Game engine = gameDB.get(new Integer(incommingID / 4));
            String card = Integer.toString(engine.identifyCard(cardT));
            engine.next(incommingID%4,card);
        }

        //Send massage to user
        out.println(jsonMSG);
        out.close();

    }

}