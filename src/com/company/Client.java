package com.company;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws Exception{
        Socket sc = new Socket("localhost", 5000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(sc.getInputStream()));
        PrintWriter writer = new PrintWriter(sc.getOutputStream(), true);
        Scanner scanner = new Scanner(System.in);

        new Thread(()->{
            try{
                while (true) {
                    String message = reader.readLine();
                    System.out.println(message);
                }
            } catch (Exception ex) {

            }
        }).start();

        new Thread(()->{
            while (true) {
                String message = scanner.nextLine();
                writer.println(message);
            }
        }).start();
    }
}

