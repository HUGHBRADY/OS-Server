package ie.gmit.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Stack;

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
					// First menu
					sendMessage("\n1) Register\n2) Log In\n3) Exit");
					outerMenu = (String)in.readObject();
					
					// Register
					if(outerMenu.compareToIgnoreCase("1")==0){
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
							
							// Write user name and password to users.txt
							bw.close();
							
							
							sendMessage("Profile registered");				// Confirmation message (true)
							sendMessage("true");							// Send confirmation message								
						}							
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
								String[] credentials = ln.split("\\s+", 3);
								
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
									String mode;
									String duration;
									
									sendMessage("\n1) Add a Fitness Record\n2) Add Meal Record\n3) View last 10 Records\n"
											+ "4) View last 10 Fitness Records\n5) Delete a Fitness Record\n6) Logout");
									innerMenu = (String)in.readObject();				// take in user choice
									
									// 1. Add a Fitness Record
									if (innerMenu.equals("1")){
										// Assign Mode
										sendMessage("Add fitness record selected");
										sendMessage("Enter activity (one word only)");					 
										mode = (String)in.readObject();					// take in mode
											
										// Assign Duration (Needs error handling)
										sendMessage("Enter duration of exercise (minutes)");
										duration = (String)in.readObject();										
										
										// Print record to file
										BufferedWriter bw = null;
										bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(username+".txt", true)));
										
										bw.append("F " + mode + " " + duration + "mins");
										bw.newLine();
										
										bw.close();
									}
									else if (innerMenu.equals("2")){
										String meal = "null";
										boolean validM = false;
										String desc;
										sendMessage("Add meal record selected");
										
										// Meal type - needs error handling
										do {
											sendMessage("Enter type of meal (breakfast/lunch/dinner/snack)");
											message = (String)in.readObject();
											
											if(message.equalsIgnoreCase("breakfast") || message.equalsIgnoreCase("b")){
												validM = true;
												meal = "Breakfast";
											}
											else if(message.equalsIgnoreCase("lunch") || message.equalsIgnoreCase("l")){
												validM = true;
												meal = "Lunch";
											}
											else if(message.equalsIgnoreCase("dinner") || message.equalsIgnoreCase("d")){
												validM = true;
												meal = "Dinner";
											}
											else if(message.equalsIgnoreCase("snack") || message.equalsIgnoreCase("s")){
												validM = true;
												meal = "Snack";
											}
											else {
												validM = false;
											}	
											
											// Tell client if meal is valid
											if (validM == true){
												sendMessage("true");
											}
											else{
												sendMessage("false");
											}
										}while (validM == false);
										
										// Set description
										sendMessage("Enter description of meal");
										desc = (String)in.readObject();
										
										// Print record to file
										BufferedWriter bw = null;
										bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(username+".txt", true)));
										
										bw.append("M " + meal + " " + desc);
										bw.newLine();
										
										bw.close();
									}
									else if (innerMenu.equals("3")){
										sendMessage("View the last 10 records selected");
										
										String[] lns = new String[10];
										int count = 0;
										
										ln = null;
										BufferedReader viewRec = null;
										viewRec = new BufferedReader(new InputStreamReader(new FileInputStream(username + ".txt")));
										
										//Loops through user file and take in last 10 entered records
										while ((ln = viewRec.readLine()) != null) {
									    	lns[count % lns.length] = ln;
										    count++;
										}
										viewRec.close();
										int start = count - 10;
										if (start < 0) {
										    start = 0;
										}
										// Displays last 10 lines by looping through array
										for (int i = start; i < count; i++) {
										    sendMessage(lns[i % lns.length]);
										}
									}
									else if (innerMenu.equals("4")){
										sendMessage("View the last 10 fitness records selected");
									}
									else if (innerMenu.equals("5")){
										sendMessage("You don't 'delete' records you 'lose' them.");
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