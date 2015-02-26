import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class FinalRouter3 {

	
	static final int routerID = 3;
	static Map<Integer,Integer> destinationLink = new HashMap<Integer,Integer>();	//Map that stores current known output link to destination at Router3
	static Map<Integer,Integer> destinationCost = new HashMap<Integer,Integer>();	//Map that stores current known cost to destination at Router3
	
	public static void main(String argv[]) throws Exception {
		
		//Lets populate this map with known values for Router3
		destinationLink.put(0, 0);	//Interface to Router0 is 0
		destinationLink.put(2, 2);	//Interface to Router2 is 2
			
		destinationCost.put(0, 7);	//Cost to Router0 is 7
		destinationCost.put(2, 2);	//Cost to Router2 is 2
		
		System.out.println("Initial values for Router3:\n\nDestination Router\tInterface\tLink Cost");
		for(int i=0;i<=3;i++){
			if(i == routerID){
				System.out.println(i+"\t\t\t"+"Local\t\t\t"+"0");
			}else if(destinationLink.containsKey(i) && destinationCost.containsKey(i)){
				System.out.println(i+"\t\t\t"+destinationLink.get(i)+"\t\t\t"+destinationCost.get(i));
			}else{
				System.out.println(i+"\t\t\t"+"-"+"\t\t\t"+"-");
			}
		}
		
		ServerSocket welcomeSocket = new ServerSocket(6963);	//Creates a new Server Socket					
		Socket connectionSocket = welcomeSocket.accept();		//Accept connection from client
		
		//Sending Router table
		OutputStream os = connectionSocket.getOutputStream();				
		ObjectOutputStream oos = new ObjectOutputStream(os);
		InputStream is = connectionSocket.getInputStream();
		ObjectInputStream ois = new ObjectInputStream(is); 
		
		for(int i=1;i<=2;i++){		//for 2 hops
			exchangeTables(ois,oos);
		}
		
		welcomeSocket.close();
	}
	
	@SuppressWarnings("unchecked")
	public static void exchangeTables(ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException{
		
		Map<Integer,Integer> incomingDestinationLink;	//Map that stores destination link map from incoming Router
		Map<Integer,Integer> incomingDestinationCost;	//Map that stores destination cost map from incoming Router
		int incomingRouterID;							//Stores ID of incoming routing table
		
		
		//Send Table
		oos.writeInt(routerID);						//send RouterID
		oos.writeObject(destinationLink);			//send destinationLink Map
		oos.writeObject(destinationCost);			//send destinationCost Map
		
		//Receiving router table
		incomingRouterID = ois.readInt();									//get ID of incoming Router
		incomingDestinationLink = (Map<Integer, Integer>) ois.readObject();	//get destinationLink Map from incoming Router
		incomingDestinationCost = (Map<Integer, Integer>) ois.readObject();	//get destinationCost Map from incoming Router
			
		//Print table received from incoming Router
		System.out.println("\nValues from Router"+incomingRouterID+":\n\nDestination Router\tInterface\tLink Cost");
		for(int i=0;i<=3;i++){
			if(incomingDestinationLink.containsKey(i) && incomingDestinationCost.containsKey(i)){
				System.out.println(i+"\t\t\t"+incomingDestinationLink.get(i)+"\t\t\t"+incomingDestinationCost.get(i));
			}else if(i == incomingRouterID){
				System.out.println(i+"\t\t\t"+"Local\t\t\t"+"0");
			}else{
				System.out.println(i+"\t\t\t"+"-"+"\t\t\t"+"-");
			}
		}
		
		int costToIncommingRouter = destinationCost.get(incomingRouterID);
		int linkToIncommingRouter = destinationLink.get(incomingRouterID);
		
		//Compare and optimize existing table at Router3 with the one received from incoming Router
		for(int i=0;i<=3;i++){
			if(incomingDestinationLink.containsKey(i) && incomingDestinationCost.containsKey(i) && i != routerID){
				int existingCost = 9999;
				if(destinationCost.containsKey(i)){
					existingCost = destinationCost.get(i);
				}				
				int newCost = costToIncommingRouter + incomingDestinationCost.get(i);
				if(newCost < existingCost){
					destinationCost.put(i, newCost);
					destinationLink.put(i, linkToIncommingRouter);
				}
			}
		}
		
		//Print updated Routing table at Router3
		System.out.println("\nUpdated values for Router3:\n\nDestination Router\tInterface\tLink Cost");
		for(int i=0;i<=3;i++){
			if(i == routerID){
				System.out.println(i+"\t\t\t"+"Local\t\t\t"+"0");
			}else if(destinationLink.containsKey(i) && destinationCost.containsKey(i)){
				System.out.println(i+"\t\t\t"+destinationLink.get(i)+"\t\t\t"+destinationCost.get(i));
			}else{
				System.out.println(i+"\t\t\t"+"-\t\t\t"+"-");
			}
		}
			
	}
	
}
