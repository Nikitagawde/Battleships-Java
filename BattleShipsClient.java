/*
 * BattleShipsClient.java
 *
 * Version:
 *     1.1
 *
 * Revisions:
 *     1.0
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * The class BattleShipsClient acts as the client in this game
 *
 */
public class BattleShipsClient {

    static char[][] myMatrix = new char[10][10];
    static char[][] oppMatrix = new char[10][10];
    int port;
    String address;
    Socket clientSocket;
    PrintWriter write;
    BufferedReader br;
    String opponentShip ;
    String shipName = "Ship A";
    //int x,y;
    StringBuilder sb;
    boolean hit;
    int skip = 0;

    /**
     * constructor
     * @param port
     */
    BattleShipsClient(String address, int port){
        this.address = address;
        this.port = port;
//        try {
//            clientSocket = new Socket(address,port);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * this method gets connected to the client and starts the game
     */
    void connectClient(){
        System.out.println("connected");
        try {
            clientSocket = new Socket(address,port);
            String name;
            Scanner sc = new Scanner(System.in);
            sb = new StringBuilder();
            write = new PrintWriter(clientSocket.getOutputStream(),true);
            write.println(shipName);
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            name = br.readLine();
            if(name != null){
                this.opponentShip = name;
                System.out.println("Ship name: "+opponentShip);
            }else{
                this.opponentShip = "Ship B";
            }

            for(;;) {
                //if lost
                int a , b,x1=0,y1=0;
                if(checkIfSomeoneWon()){
                    a= -1;
                    b = -1;
                }else {
                    System.out.println("Enter coordinates to set the bomb ");
                    a = sc.nextInt();
                    b = sc.nextInt();
                }//else end
                System.out.println(a + "---" + b);
                sb.append(a);
                sb.append(",");
                sb.append(b);

                write.println(sb.toString());
                if((a + b) == -2){
                    System.out.println("You lost");
                    break;
                }
                int check = Integer.parseInt(br.readLine());
                if (check == 0) {
                    //update matrix to missed
                    oppMatrix[a-1][b-1] = '.';
                    System.out.println("You miss the ship");
                } else if (check == 1) {
                    //update matrix to hit
                    oppMatrix[a-1][b-1] = 'x';
                    System.out.println("You hit the ship");
                }

                System.out.println("\nOur arena");
                displayMat(myMatrix);
                System.out.println("\n\n");
                System.out.println("Opponent arena");
                displayMat(oppMatrix);

                String[] arr;
                name = "";
                br.skip(skip);
                if ((name = br.readLine()) != null) {
                    skip = skip + name.length();
                    System.out.println(name);
                    arr = name.split(",");

                    if (arr.length == 2) {
                        x1 = Integer.parseInt(arr[0]);
                        y1 = Integer.parseInt(arr[1]);
                    }
                    System.out.println(x1 + "---" + y1);
                }

                if((x1 + y1) == -2){
                    System.out.println("You won");
                    break;
                }

                hit = setBomb(x1,y1);
                if (hit) {
                    System.out.println("You got hit");
                    //myMatrix[x-1][y-1] = 'x';
                    write.println(1);
                }
                else {
                    System.out.println("Phew! it's a miss");
                    //myMatrix[x-1][y-1] = '.';
                    write.println(0);
                }
                System.out.println("\nOur arena");
                displayMat(myMatrix);
                System.out.println("\n\n");
                System.out.println("Opponent arena");
                displayMat(oppMatrix );

            }

            clientSocket.close();
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
     * helps to put ships in row
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
        if ( myMatrix[i-1][j-1] == '.' ){
            System.out.println("Already planted a bomb there");
            return false;
        }
        if (myMatrix[i-1][j-1] == 's' || myMatrix[i-1][j-1] == 'x') {
            myMatrix[i - 1][j - 1] = 'x';
            return true;
        }
        else
            myMatrix[i-1][j-1] = '.';
        return false;
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
                            //settingShip++;
                            break;
                        }
                    } else {
                        if (putShipsInRow(i, j, 2)) {
                            //settingShip++;
                            break;
                        }
                    }
                }
            }
        }catch (Exception e){}

        displayMat(myMatrix);
        int port;
        String address;
        if(args.length == 2) {
            port = Integer.parseInt(args[0]);
            address = args[1];
        }
        else{
            port = 80;
            address = "127.0.0.1";
        }
        BattleShipsClient client = new BattleShipsClient(address, port);
        //Function for setting ship
        System.out.println("function");
        client.connectClient();

    }
}

