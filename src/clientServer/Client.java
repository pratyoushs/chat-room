package clientServer;

import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;
public class Client {
	private Socket sock;
	
	public static void main(String[] args) throws Exception{
		Client c = new Client();
		c.startClient();
	}

	private void startClient() throws Exception {
		// TODO Auto-generated method stub
		sock = new Socket("localhost", 444);
				
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(new Send());
		executor.execute(new Receive());		
	}
	
	public class Send implements Runnable{
		BufferedReader br;
		PrintStream ps;
		OutputStream os;
		public Send(){
			this.br = new BufferedReader(new InputStreamReader(System.in));
			try {
				os = sock.getOutputStream();
				ps = new PrintStream(os);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println("You are connected!");
			String messageSent;
			while(true){
				try {
					messageSent = br.readLine();
					//System.out.println("Sending...");
					messageSent.trim();
					if(messageSent.equalsIgnoreCase("bye")){
						System.out.println("Exiting...");
						System.exit(1);
					}
					String[] words = messageSent.split(" ");
					String client;
					String message = null;
					int file = 0;
					
					if(words[0].equalsIgnoreCase("broadcast")){
						client = "null";
						if(words[1].equalsIgnoreCase("file")){
							message = words[2];
							file = 1;
						}
						else{
							message = messageSent.substring(words[0].length()+1);
							//System.out.println("Message is " + message);
						}
					}
					else if(words[0].equalsIgnoreCase("blockcast") || words[0].equalsIgnoreCase("unicast")){
						client = words[1];
						if(words[2].equalsIgnoreCase("file")){
							message = words[3];
							file = 1;
						}
						else{
							message = messageSent.substring(words[0].length() + words[1].length() + 2);
							//System.out.println("Message is " + message);
						}
					}
					else{
						System.out.println("You>  Incorrect message format!");
						continue;
					}
					//System.out.println(file);

					if(file == 0){
						sendMessage(words[0], client, message);
					}
					else if(file == 1){
						sendFile(words[0], client, message);						
					}
					//ps.println(messageSent);
				} 
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	
		}

		private void sendMessage(String mode, String client, String message) {
			// TODO Auto-generated method stub
			System.out.println("You>   " + message);
			ps.println(mode.toLowerCase());
			ps.println(client.toLowerCase());
			ps.println(message);
			ps.flush();
		}

		private void sendFile(String mode, String client, String message) throws IOException {
			// TODO Auto-generated method stub
			//ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			ps.println(mode.toLowerCase() + " file");
			ps.println(client.toLowerCase());
			//System.out.println(mode + " " + client + " " + message);
			File transferFile = new File ("C:\\Users\\Pratyoush\\Desktop\\clientserver\\client1\\"+message);
			ps.println(transferFile.getName());
			ps.println(transferFile.length());
	
		    byte [] byteArray  = new byte [1024];
		    //FileInputStream fin = new FileInputStream(transferFile);
		    int c = 0;
		    try{
		    	FileInputStream bis = (new FileInputStream(transferFile));
		    	while( (c = bis.read(byteArray, 0, byteArray.length)) != -1){
		    		//ps.print(byteArray);
		    		os.write(byteArray, 0, c);
		    	}
		    	os.flush();
		    	bis.close();
				System.out.println("You>   Sent a file: " + message);
		    }
		    catch(FileNotFoundException e){
		    	System.out.println("File not found. Please check the filename and retry.");
		    }	
		}
	}
	
	public class Receive implements Runnable{
		InputStreamReader ir; 
		BufferedReader br;
		InputStream is;
		public Receive(){
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				is = sock.getInputStream();
				ir = new InputStreamReader(is);
				br = new BufferedReader(ir);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				System.out.println(e1.getMessage());
			}
			//System.out.println("Ready to receive");
			String messageRecv;
			String sender;
			while(true){
				
				try {
					sender = br.readLine();
					System.out.print(sender+">   ");
					messageRecv = br.readLine().trim();
					if(messageRecv != null){
						if(messageRecv.equalsIgnoreCase("%file%")){
							String clientId = br.readLine();
							String fileName = br.readLine();
							int fLength = Integer.parseInt(br.readLine());
							//System.out.println(clientId + fileName + " " + fLength + messageRecv);
							//System.exit(0);
							byte[] byteArray = new byte[1024];
							File outputFile = new File(
									"C:\\Users\\Pratyoush\\Desktop\\clientserver\\"+clientId+"\\"+fileName);
							
							FileOutputStream os = new FileOutputStream(outputFile);
							if(!outputFile.exists()){
								outputFile.mkdirs();
							}
							int countS = 0;
							while ( fLength > 0 && (countS = is.read(byteArray, 0, byteArray.length))!= -1) {
					            //countS = is.read(byteArray,0,byteArray.length);
					            os.write(byteArray, 0, byteArray.length);
					            fLength -= countS;
					            //System.out.println(fLength);
					        }
							os.flush();
							os.close();
							System.out.println("Sent you a file: " + fileName);							
						}
						else{
							System.out.println(messageRecv);
						}
					}
				} catch (FileNotFoundException e2){
					System.out.println("File not found.");
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println(e.getMessage());
				}	
			}				
		}
	}
}
