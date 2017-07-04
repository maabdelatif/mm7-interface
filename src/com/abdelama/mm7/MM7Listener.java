import java.io.*;
import java.net.*;
public class MM7Listener{
	ServerSocket providerSocket;
	Socket connection = null;
	ObjectOutputStream out;
	ObjectInputStream in;
	String message;
	MM7Listener(){}
	void listen(String url)
	{
		try{
			
			URL smsURL = new URL(url);
			//Divide url into server and port
			int serverPort = 3812;
			
			String message = "";
			
			//1. creating a server socket 
			providerSocket = new ServerSocket(serverPort);
			//2. Wait for connection
			System.out.println("Waiting for connection");
			connection = providerSocket.accept();
			System.out.println("Connection received from " + connection.getInetAddress().getHostName());
			//3. get Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			//sendMessage("Connection successful");
			//4. Print out whatever you get
			do
			{
				message = (String)in.readObject();
				System.out.println(message);
			}while(true);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			//4: Closing connection
			try{
				in.close();
				out.close();
				providerSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
	void sendMessage(String msg)
	{
		try{
			out.writeObject(msg);
			out.flush();
			System.out.println("server>" + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	public static void main(String args[])
	{
		MM7Listener listener = new MM7Listener();
		while(true){
			listener.listen("http://localhost:3812");
		}
	}
}