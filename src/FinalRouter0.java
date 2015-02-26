import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class FinalRouter0 {
	
	static final int routerID = 0;
	static Map<Integer,Integer> destinationLink = new HashMap<Integer,Integer>();	//Map that stores current known output link to destination at Router0
	static Map<Integer,Integer> destinationCost = new HashMap<Integer,Integer>();	//Map that stores current known cost to destination at Router0
	
	@SuppressWarnings("resource")
	public static void main(String argv[]) throws Exception {
		
		//Lets populate this map with known values for Router0
		destinationLink.put(1, 0);	//Interface to Router1 is 0
		destinationLink.put(2, 1);	//Interface to Router2 is 1
		destinationLink.put(3, 2);	//Interface to Router3 is 2
		
		destinationCost.put(1, 1);	//Cost to Router1 is 1
		destinationCost.put(2, 3);	//Cost to Router2 is 3
		destinationCost.put(3, 7);	//Cost to Router3 is 7
		
		System.out.println("Initial values for Router0(No Hops):\n\nDestination Router\tInterface\tLink Cost");
		for(int i=0;i<=3;i++){
			if(i == routerID){
				System.out.println(i+"\t\t\t"+"Local\t\t\t"+"0");
			}else if(destinationLink.containsKey(i) && destinationCost.containsKey(i)){
				System.out.println(i+"\t\t\t"+destinationLink.get(i)+"\t\t\t"+destinationCost.get(i));
			}else{
				System.out.println(i+"\t\t\t"+"-"+"\t\t\t"+"-");
			}
		}
		
		Socket clientSocket1 = new Socket(InetAddress.getByName("afs2.njit.edu"), 6961);
		Socket clientSocket2 = new Socket(InetAddress.getByName("afs3.njit.edu"), 6962);
		Socket clientSocket3 = new Socket(InetAddress.getByName("afs4.njit.edu"), 6963);
		
		InputStream is1 = clientSocket1.getInputStream();
		ObjectInputStream ois1 = new ObjectInputStream(is1); 
		OutputStream os1 = clientSocket1.getOutputStream();				
		ObjectOutputStream oos1 = new ObjectOutputStream(os1);
				
		InputStream is2 = clientSocket2.getInputStream();
		ObjectInputStream ois2 = new ObjectInputStream(is2); 
		OutputStream os2 = clientSocket2.getOutputStream();				
		ObjectOutputStream oos2 = new ObjectOutputStream(os2);
		
		InputStream is3 = clientSocket3.getInputStream();
		ObjectInputStream ois3 = new ObjectInputStream(is3); 
		OutputStream os3 = clientSocket3.getOutputStream();				
		ObjectOutputStream oos3 = new ObjectOutputStream(os3);
		
		for(int i=1;i<=2;i++){
			System.out.print("\nBegin update cycle "+i);
			exchangeTables(ois3, oos3);		//Exchange tables with Router 3
			exchangeTables(ois2, oos2);		//Exchange tables with Router 2
			exchangeTables(ois1, oos1);		//Exchange tables with Router 1
			System.out.println("Complete update cycle "+i);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public static void exchangeTables(ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException{

		Map<Integer,Integer> incomingDestinationLink;	//Map that stores destination link map from incoming Router
		Map<Integer,Integer> incomingDestinationCost;	//Map that stores destination cost map from incoming Router
		int incomingRouterID;							//Stores ID of incoming routing table
	
		
		
		//Get Table from Router
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
		
		//Compare and optimize existing table at Router0 with the one received from incoming Router
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
		
		//Print updated Routing table at Router0
		System.out.println("\nUpdated values for Router0:\n\nDestination Router\tInterface\tLink Cost");
		for(int i=0;i<=3;i++){
			if(i == routerID){
				System.out.println(i+"\t\t\t"+"Local\t\t\t"+"0");
			}else if(destinationLink.containsKey(i) && destinationCost.containsKey(i)){
				System.out.println(i+"\t\t\t"+destinationLink.get(i)+"\t\t\t"+destinationCost.get(i));
			}else{
				System.out.println(i+"\t\t\t"+"-\t\t\t"+"-");
			}
		}
		
		//Send updated values to new Router
		oos.writeInt(routerID);						//send RouterID to Router0
		oos.writeObject(destinationLink);			//send destinationLink Map to Router0
		oos.writeObject(destinationCost);			//send destinationCost Map to Router0
	
	}
}
