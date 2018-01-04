package ie.gmit.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
  public static void main(String[] args) throws Exception {
    ServerSocket m_ServerSocket = new ServerSocket(2004,10);
    int id = 0;
    while (true) {
      Socket clientSocket = m_ServerSocket.accept();
      ClientServiceThread cliThread = new ClientServiceThread(clientSocket, id++);
      cliThread.start();
    }
  }
}

class ClientServiceThread extends Thread {
	Socket clientSocket;
	String message, userMessage, passMessage;
	String outerMenu, innerMenu;
	String username, password;
	boolean loggedIn = false, usernameFree;
	String loggedInMsg, usernameFreeMsg;
	int clientID = -1;
	boolean running = true;
	ObjectOutputStream out;
	ObjectInputStream in;

	ClientServiceThread(Socket s, int i) {
		clientSocket = s;
		clientID = i;
	}

	void sendMessage(String msg){
		try{
			out.writeObject(msg);
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
  
	public void run() {
		System.out.println("Accepted Client : ID - " + clientID + " : Address - "
					+ clientSocket.getInetAddress().getHostName());
		try {
	    	out = new ObjectOutputStream(clientSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(clientSocket.getInputStream());
			System.out.println("Accepted Client : ID - " + clientID + " : Address - "
			        + clientSocket.getInetAddress().getHostName());
			
			do {
				try {
					// first menu
					sendMessage("\n1) Register\n2) Log In\n3) Exit");
					outerMenu = (String)in.readObject();
					
					// Register
					if(outerMenu.compareToIgnoreCase("1")==0){
						do {
							usernameFree = true;
							sendMessage("Enter username");
							username = (String)in.readObject();
							sendMessage("Enter password");
							password = (String)in.readObject();
							
							// Read users.txt to check if user name is free
							BufferedReader br = null;
							br = new BufferedReader(new InputStreamReader(new FileInputStream("users.txt")));
							
							String ln = "";
							while((ln = br.readLine()) != null){
								String[] credentials = ln.split("\\s");
								
								if (credentials[0].equals(username)){
									usernameFree = false;						// Set boolean
									sendMessage("Username already in use");		// Confirmation message (false)
									sendMessage("false");						// Send Confirmation message
									break;
								}							
							}
							br.close();
							
							if(usernameFree){							
								BufferedWriter bw = null;
								bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("users.txt", true)));
								
								bw.append(username + " " + password);
								bw.newLine();
								
								// write user name and password to users.txt
								bw.close();
								
								sendMessage("Profile registered");				// Confirmation message (true)
								sendMessage("true");							// Send confirmation message
							}							
						} while(usernameFree == false);
					}
					
					// LOG IN
					else if(outerMenu.compareToIgnoreCase("2") == 0){
						loggedIn = false;
						sendMessage("Enter Username");
						String username = (String)in.readObject();							
						sendMessage("Enter Password");
						String password = (String)in.readObject();
						
						try {
							BufferedReader br = null;
							br = new BufferedReader(new InputStreamReader(new FileInputStream("users.txt")));
														
							// Placeholder String
							String ln = "";
							while((ln = br.readLine()) != null){
								String[] credentials = ln.split("\\s");
								
								if (credentials[0].equals(username) && credentials[1].equals(password)){
									loggedIn = true;
									sendMessage("true");
									break;
								}
							}
							br.close();
							
							if (loggedIn == false){
								sendMessage("false");
							}
							
							
							if (loggedIn == true) {
								// INNER MENU
								do {
									sendMessage("\n1) Add a Fitness Record\n2)Add Meal Record\n3) View last 10 Records\n4) View last 10 Fitness Records"
											+ "	\n5) Delete a Fitness Record\n6) Logout");
									innerMenu = (String)in.readObject();				// take in user choice
									
									// 1. Add a Fitness Record
									if (innerMenu.equals("1")){
										sendMessage("Add fitness record selected");
										sendMessage("Enter mode (cycling/walking/running)");
										message = (String)in.readObject();				// take in user mode asa
									}
									else if (innerMenu.equals("2")){
										sendMessage("Add meal record selected");
									}
									else if (innerMenu.equals("3")){
										sendMessage("View the last 10 records selected");
									}
									else if (innerMenu.equals("4")){
										sendMessage("View the last 10 fitness records selected");
									}
									else if (innerMenu.equals("5")){
										sendMessage("Delete a fitness record selected");
									}
									else {
										sendMessage("Invalid choice, try again");
									}
									
								} while(!innerMenu.equals("6"));
								
							}
						}
						catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					}
				}
				catch(ClassNotFoundException classNot){
					System.err.println("Data received in unknown format");
				}
	    	}while(!outerMenu.equals("3"));
	      
			System.out.println("Ending Client : ID - " + clientID + " : Address - "
			        + clientSocket.getInetAddress().getHostName());
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	}
}
