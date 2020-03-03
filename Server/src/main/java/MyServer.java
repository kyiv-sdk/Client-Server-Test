import java.io.*;
import java.net.*;
import java.util.Scanner;

public class MyServer {
    public static void main(String[] args){


        try {
            ServerSocket ss = new ServerSocket(6666);
            Socket s = ss.accept();//establishes connection
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());
            while(true) {
                String str = (String) dis.readUTF();
                System.out.println("message= " + str);

                if (str.equals("quit")) {
                    ss.close();
                    break;
                }

                Scanner input = new Scanner(System.in);
                System.out.print("Enter text to be send(" +"/" + "-/" +  "for end session): ");
                String myString = input.next();

                if (myString.equals("-")){
                    dout.flush();
                    dout.close();
                    ss.close();
                    break;
                }

                dout.writeUTF(myString);
                dout.flush();

            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
