/*
 * BattleShipsServer.java
 *
 * Version:
 *     1.1
 *
 * Revisions:
 *     1.0
 */

import java.util.Scanner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The class BattleShipsServer acts as the server in this game
 *
 */
public class BattleShipsServer {

    static char[][] myMatrix = new char[10][10];
    static char[][] oppMatrix = new char[10][10];
    int port;
    Socket serverSocket;
    ServerSocket socket;
    BufferedReader br;
    PrintWriter write;
    String opponentShip;
    String shipName =  "Ship B";
    StringBuilder sb;
    //int x,y;
    boolean hit;
    int skip=0;

    /**
     * constructor
     * @param port
     */
    BattleShipsServer(int port){
        this.port = port;
        try {
            socket  = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * this method gets connected to the client and starts the game
     */
    public void connectServer() {
        int a=0,b=0,x1=0,y1=0;
        System.out.println("connected");
        try {
            serverSocket = socket.accept();
            String line;
            br = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            sb = new StringBuilder();
            Scanner sc = new Scanner(System.in);
            line = br.readLine();
            if (line != null) {
                this.opponentShip = line;
                System.out.println("Opponent Ship name: " + opponentShip);
            } else {
                this.opponentShip = "Ship A";
            }
            write = new PrintWriter(serverSocket.getOutputStream(), true);
            write.println(shipName);

            for (; ; ) {
                //loop for the game
                String[] arr;
                line = "";
                //skips n number of characters for new co ordinates
                br.skip(skip);
                if ((line = br.readLine()) != null) {
                    skip = skip + line.length();
                    System.out.println(line);
                    arr = line.split(",");

                    if (arr.length == 2) {
                        a = Integer.parseInt(arr[0]);
                        b = Integer.parseInt(arr[1]);
                    }
                    System.out.println(a + "---" + b);
                }

                if ((a + b) == -2 ) {
                    System.out.println("You won");
                    break;
                }

                hit = setBomb(a,b);

                //Check if it a hit or miss
                if (hit) {
                    System.out.println("You got hit");
                    //myMatrix[x-1][y-1] = 'x';
                    write.println(1);
                } else {
                    System.out.println("Phew! it's a miss");
                    //myMatrix[x-1][y-1] = '.';
                    write.println(0);
                }

                System.out.println("\nOur arena");
                displayMat(myMatrix);
                System.out.println("\n\n");
                System.out.println("Opponent arena");
                displayMat(oppMatrix );

                //if lost
                if(checkIfSomeoneWon()){
                    x1= -1;
                    y1 = -1;
                }
                else {
                    System.out.println("Enter coordinates to set the bomb ");
                    x1 = sc.nextInt();
                    y1 = sc.nextInt();
                }//else end
                System.out.println(x1 + "---" + y1);
                sb.append(x1);
                sb.append(",");
                sb.append(y1);
                write.println(sb.toString());
                if ((x1 + y1) == -2) {
                    System.out.println("You lost");
                    break;
                }
                int check = Integer.parseInt(br.readLine());
                if (check == 0) {
                    //update matrix to miss
                    oppMatrix[x1-1][y1-1] = '.';
                    System.out.println("You miss the ship");
                } else if (check == 1) {
                    //update matrix to hit
                    oppMatrix[x1-1][y1-1] = 'x';
                    System.out.println("You hit the ship");
                }

                System.out.println("\nOur arena");
                displayMat(myMatrix);
                System.out.println("\n\n");
                System.out.println("Opponent arena");
                displayMat(oppMatrix );
            }

            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * setting matrix to all 0
     * @param puttingZeroes
     */
    public static void setMyMatrix(char[][] puttingZeroes){
        for(int i = 0; i < 10;i++)
            for(int j = 0; j < 10;j++)
                puttingZeroes[i][j] = '0';
    }

    /**
     * helps to put ships in column
     * @param i
     * @param j
     * @param size
     * @return
     */
    public static boolean putShipsInColumn(int i,int j, int size){
        boolean rVal = false;
        if(i+size > 11)
            return rVal;
        else if(checkCanPutShip(i,j,size,"C")){
            for(; size != 0; size--) {
                myMatrix[i-1][j-1] = 's';
                i++;
            }
            rVal = true;
        }
        return rVal;
    }

    /**
     * helps in putting ships in row
     * @param i
     * @param j
     * @param size
     * @return
     */
    public static boolean putShipsInRow(int i, int j, int size){
        boolean rVal = false;
        if(j+size > 11)
            return rVal;
        else if(checkCanPutShip(i,j,size,"R")){
            rVal = true;
            for(;size != 0; size--){
                myMatrix[i-1][j-1] = 's';
                j++;
            }
        }
        return rVal;
    }

    /**
     * validates if ship can be placed at that position
     * @param i
     * @param j
     * @param size
     * @param rORc
     * @return
     */
    public static boolean checkCanPutShip(int i, int j, int size, String rORc){
        if(rORc.equals("R")){
            boolean rVal = true;
            for(;size != 0; size--){
            if(myMatrix[i - 1][j - 1] == 's') {
                return false;
            }
            j++;
        }
        return rVal;
        }else{
            boolean rVal = true;
            for(;size != 0; size--){
                if(myMatrix[i - 1][j - 1] == 's') {
                    return false;
                }
                i++;
        }
            return rVal;
        }
    }

    /**
     * sets bomb at that co ordinates
     * @param i
     * @param j
     * @return
     */
    public static boolean setBomb(int i, int j){
        if(i > 10 || j > 10) {
            System.out.println("Enter 1-10 for planting bomb at any location");
            return false;
        }
        if (myMatrix[i-1][j-1] == '.' ){
            System.out.println("Already planted a bomb there");
            return false;
        }
        if (myMatrix[i-1][j-1] == 's' || myMatrix[i-1][j-1] == 'x') {
            myMatrix[i - 1][j - 1] = 'x';
            return true;
        }
        else{
            myMatrix[i-1][j-1] = '.';
        return false;
        }
    }

    /**
     * checks if opponent has won?
     * @return
     */
    public static boolean checkIfSomeoneWon(){
        int count = 0;
        for(int i = 0; i < 10; i++)
            for(int j = 0; j < 10; j++)
                if(myMatrix[i][j] == 'x')
                    count++;

        if(count == 6)
            return true;
        else return false;
    }

    /**
     * displays matrix
     * @param mat
     */
    public static void displayMat(char[][] mat){
        for(int i = 0; i < 10;i++) {
            for (int j = 0; j < 10; j++)
                System.out.print(mat[i][j] + " ");
            System.out.println();
        }
    }

    /**
     * main method for setting ships and running game
     * @param args
     */
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        setMyMatrix(myMatrix);
        setMyMatrix(oppMatrix);
            try {
                while (true) {
                    System.out.println("setting ship size 4: Enter 1 for column and 2 for row");
                    int RorC = sc.nextInt();
                if(RorC == 1 || RorC == 2){
                    System.out.println("Enter column number, row number for ship size 4");
                    int i = sc.nextInt();
                    int j = sc.nextInt();
                    if(RorC == 1){
                        if(putShipsInColumn(i,j ,4 )) {
                            break;
                        }
                    }
                    else {
                        if(putShipsInRow(i,j ,4 )) {
                            break;
                        }
                    }
                }
                }
            }catch (Exception e){}


            try {
                while (true) {
                    System.out.println("setting ship size 2: Enter 1 for column and 2 for row");
                    int RorC = sc.nextInt();
                    if(RorC == 1 || RorC == 2) {
                        System.out.println("Enter column number, row number for ship size 2");
                        int i = sc.nextInt();
                        int j = sc.nextInt();
                        if (RorC == 1) {
                            if (putShipsInColumn(i, j, 2)) {
                                break;
                            }
                        } else {
                            if (putShipsInRow(i, j, 2)) {
                                break;
                            }
                        }
                    }
                }
            }catch (Exception e){}

        displayMat(myMatrix);
        int port;
        if(args.length == 1){
            port = Integer.parseInt(args[0]);
        }
        else
            port = 80;
        BattleShipsServer server = new BattleShipsServer(port);
        server.connectServer();


    }
}
