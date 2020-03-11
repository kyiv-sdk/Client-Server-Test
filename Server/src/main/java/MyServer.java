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

            InputThread inputThread = new InputThread(s, dis);
            inputThread.start();
            while(true) {

                Scanner input = new Scanner(System.in);
                System.out.print("Enter text to be send(" +"/" + "-/" +  "for end session): ");
                String toBeSend = input.next();

                if (toBeSend.equals("-")){
                    //dout.flush();
                    dout.close();
                    ss.close();
                    break;
                }
                OutputThread outputThread = new OutputThread(s, dout, toBeSend);
                outputThread.start();
            }
            inputThread.join();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    static class InputThread extends Thread{

        Socket socket;
        DataInputStream dis;

        InputThread(Socket s, DataInputStream dis){
            this.dis = dis;
            this.socket = s;
        }
        @Override
        public void run() {
            while (true){

                String str = null;
                try {
                    str = (String) dis.readUTF();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("message= " + str);

                if (str.equals("quit")) {
                    break;
                }
            }
            //close socket in main thread
        }
    }

    static class OutputThread extends Thread{

        Socket socket;
        DataOutputStream dout;
        String toBeSend;

        OutputThread(Socket s, DataOutputStream dout, String message){
            this.dout = dout;
            this.socket = s;
            toBeSend = message;
        }
        @Override
        public void run() {
            try {
                dout.writeUTF(toBeSend);
                dout.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
