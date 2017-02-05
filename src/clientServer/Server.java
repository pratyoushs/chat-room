package clientServer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server{
	private static final int PORT = 444;
	private static HashMap<Socket, String> socketMap = new HashMap<Socket, String>();
	private static int clientCount = 0;
	static ArrayList<Socket> allSockets = new ArrayList<Socket>();

	public static void main(String[] args) throws Exception{
		ServerSocket servsock = new ServerSocket(PORT);
		if(servsock != null){
			System.out.println("server started");
		}
		ExecutorService executor = Executors.newCachedThreadPool();
		while(true){
			Socket sock = servsock.accept();

			//this blocks the server. make it part of clienthandler
			//String option = printMenu(sock);
			clientCount++;
			socketMap.put(sock, "client"+clientCount);
			executor.execute(new ClientHandler(sock));
		}
	}


	private static class ClientHandler implements Runnable{
		Socket sock;
		BufferedReader br;
		String message;
		String mode;
		String client;
		public ClientHandler(Socket sock){
			this.sock = sock;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				System.out.println("New client added with port number " + sock.getPort());
				PrintStream ps = new PrintStream(sock.getOutputStream());
				ps.println(socketMap.get(sock));
				while(true){
					br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
					mode = br.readLine();
					client = br.readLine();
					System.out.println(mode + "\n" + client);
					//bmessage = br.readLine();
					switch(mode){
					case "broadcast" : broadCast();
					break;
					case "unicast" :
						uniCast(client);
						break;
					case "blockcast" :
						blockClient(client);
						break;
					case "broadcast file" : 
						broadCastFile();
						break;
					case "unicast file" :
						try {
							uniCastFile(client);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					case "blockcast file" :
						try {
							blockClientFile(client);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					default:
						break;
					}
				}
			}
			catch(IOException ignore){};
		}

		private void blockClientFile(String client) throws Exception {
			// TODO Auto-generated method stub
			DataInputStream dis = new DataInputStream(sock.getInputStream());
			String fileName = br.readLine();
			int fileLength = Integer.parseInt(br.readLine());
			byte[] byteArray = new byte[fileLength];
			dis.readFully(byteArray, 0, byteArray.length);

			System.out.println("Server received");
			for(Socket s: socketMap.keySet()){
				if(s.getPort() != sock.getPort() && !socketMap.get(s).equals(client)){
					OutputStream os = s.getOutputStream();
					DataOutputStream dos = new DataOutputStream(os);
					PrintStream ps = new PrintStream(os);
					
					ps.println(socketMap.get(sock));
					ps.println("%File%");
					ps.println(socketMap.get(s));
					ps.println(fileName);
					ps.println(byteArray.length);
					ps.flush();
					
					dos.write(byteArray,0,byteArray.length);
					dos.flush();
					System.out.println("File sent to " + socketMap.get(s));
				}
			}
			
		}

		private void uniCastFile(String client) throws Exception {
			// TODO Auto-generated method stub
			DataInputStream dis = new DataInputStream(sock.getInputStream());
			String fileName = br.readLine();
			int fileLength = Integer.parseInt(br.readLine());
			byte[] byteArray = new byte[fileLength];
			dis.readFully(byteArray, 0, byteArray.length);
			
			
			System.out.println("Server received");
			for(Socket s: socketMap.keySet()){
				if(s.getPort() != sock.getPort() && socketMap.get(s).equals(client)){
					OutputStream os = s.getOutputStream();
					DataOutputStream dos = new DataOutputStream(os);
					PrintStream ps = new PrintStream(os);
					
					ps.println(socketMap.get(sock));
					ps.println("%File%");
					ps.println(socketMap.get(s));
					ps.println(fileName);
					ps.println(byteArray.length);
					ps.flush();
					
					dos.write(byteArray,0,byteArray.length);
					dos.flush();
					System.out.println("File sent to " + socketMap.get(s));
				}
			}
			
		}

		private void broadCastFile() throws IOException {
			// TODO Auto-generated method stub
			DataInputStream dis = new DataInputStream(sock.getInputStream());
			String fileName = br.readLine();
			int fileLength = Integer.parseInt(br.readLine());
			byte[] byteArray = new byte[fileLength];
			dis.readFully(byteArray, 0, byteArray.length);
			
			
			System.out.println("Server received: " + byteArray.length);
			for(Socket s: socketMap.keySet()){
				if(s.getPort() != sock.getPort()){
					OutputStream os = s.getOutputStream();
					DataOutputStream dos = new DataOutputStream(os);
					PrintStream ps = new PrintStream(os);
					
					ps.println(socketMap.get(sock));
					ps.println("%File%");
					ps.println(socketMap.get(s));
					ps.println(fileName);
					ps.println(byteArray.length);
					ps.flush();
					
					dos.write(byteArray,0,byteArray.length);
					dos.flush();
					System.out.println("File sent to " + socketMap.get(s));
				}
			}
		}

		private void blockClient(String client) throws IOException {
			// TODO Auto-generated method stub
			message = br.readLine();
			for(Map.Entry e: socketMap.entrySet())
			{
				Socket s = (Socket)e.getKey();
				if(s.getPort()!= sock.getPort() && !((String)e.getValue()).equals(client))
				{
					PrintStream ps = new PrintStream(s.getOutputStream());
					ps.println(socketMap.get(sock));
					ps.println(message);
					System.out.println("message sent");
				}
			}

		}

		private void uniCast(String client) throws IOException {
			// TODO Auto-generated method stub
			message = br.readLine();
			{

				for(Map.Entry e: socketMap.entrySet())
				{  Socket s = (Socket)e.getKey();
				if(s.getPort()!=sock.getPort() && ((String)e.getValue()).equals(client))
				{
					PrintStream ps = new PrintStream(s.getOutputStream());
					ps.println(socketMap.get(sock));
					ps.println(message);
					System.out.println("message sent");
				}
				}

			}


		}

		private void broadCast() throws IOException {
			// TODO Auto-generated method stub
			message = br.readLine();
			System.out.println(message);
			for(Socket s: socketMap.keySet()){
				if(message != null && s.getPort()!= sock.getPort()){
					System.out.println(message + " " + s.getPort() + " " + sock.getPort());

					PrintStream ps = new PrintStream(s.getOutputStream());
					ps.println(socketMap.get(sock));
					ps.println(message);
					ps.flush();
					System.out.println("message sent");
				}
				else{
					//sock.close();
				}

			}
		}
	}

}
