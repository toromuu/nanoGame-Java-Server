package es.um.redes.nanoGames.client.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import es.um.redes.nanoGames.message.NGBasicMessage;
import es.um.redes.nanoGames.message.NGMessage;
import es.um.redes.nanoGames.message.NGParamMessage;
import es.um.redes.nanoGames.message.NGPlayersMessage;
import es.um.redes.nanoGames.message.NGRoomsMessage;
import es.um.redes.nanoGames.message.NGShortMessage;
import es.um.redes.nanoGames.message.NGStringMessage;
import es.um.redes.nanoGames.server.NGPlayerInfo;
import es.um.redes.nanoGames.server.roomManager.NGRoomDescription;

//This class provides the functionality required to exchange messages between the client and the game server 
public class NGGameClient {
	private Socket socket;
	protected DataOutputStream dos;
	protected DataInputStream dis;
	
	private static final int SERVER_PORT = 6969;

	public NGGameClient(String serverName) {
		try {
			socket = new Socket(serverName, SERVER_PORT);
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.err.println("La direccion proporcionada no es valida");
		}
	}

	public boolean verifyToken(long token) throws IOException {
		NGParamMessage mensaje = (NGParamMessage)
			NGMessage.makeParamMessage(NGMessage.OP_PARAM, token);
		byte[] mensajeBytes = mensaje.toByteArray();
		dos.write(mensajeBytes);
		//ESPERANDO CONTESTACION
		NGBasicMessage response = (NGBasicMessage)
				 NGMessage.readMessageFromSocket(dis);
		return (response.getOpcode() == NGMessage.OP_TOKEN_OK);
	}
	
	public boolean registerNickname(String nick) throws IOException {
		
		NGStringMessage mensajeNick = (NGStringMessage)
				NGMessage.makeStringMessage(NGMessage.OP_NICK, nick);
		byte[] mensajeBytes = mensajeNick.toByteArray();
		dos.write(mensajeBytes);
		//ESPERANDO CONTESTACION
		NGBasicMessage response = (NGBasicMessage)
				 NGMessage.readMessageFromSocket(dis);
		return (response.getOpcode() == NGMessage.OP_NICK_OK);
			
	}

	
	public List<NGRoomDescription> getRoomList() throws IOException{
		NGBasicMessage mensaje = (NGBasicMessage)
				NGMessage.makeBasicMessage(NGMessage.OP_GET_ROOM_LIST);
		byte[] mensajeBytes = mensaje.toByteArray();
		dos.write(mensajeBytes);
		//ESPERANDO CONTESTACION
		NGRoomsMessage lista = (NGRoomsMessage)
				 NGMessage.readMessageFromSocket(dis);
		return lista.getRoomList();
		
	}
	//Used by the shell in order to check if there is data available to read 
	public boolean isDataAvailable() throws IOException {
		return (dis.available() != 0);
	}
	
	// To enter a specific room to play
	public boolean requestEnterRoom(short sala) throws IOException {
		NGShortMessage mensaje = (NGShortMessage)
				NGMessage.makeShortMessage(NGMessage.OP_ENTER_ROOM, sala);
		byte[] mensajeBytes = mensaje.toByteArray();
		dos.write(mensajeBytes);
		//ESPERANDO CONTESTACION
		NGBasicMessage response = (NGBasicMessage)
			 NGMessage.readMessageFromSocket(dis);
		return (response.getOpcode() == NGMessage.OP_ACCESS_OK);
	}
	
	public void getRules() throws IOException{
		NGBasicMessage mensaje = (NGBasicMessage)
				NGMessage.makeBasicMessage(NGMessage.OP_GET_RULES);
		byte[] mensajeBytes = mensaje.toByteArray();
		dos.write(mensajeBytes);
	}
	
	public void getStatus() throws IOException{
		
		NGBasicMessage mensaje = (NGBasicMessage)
				NGMessage.makeBasicMessage(NGMessage.OP_GET_STATUS);
		byte[] mensajeBytes = mensaje.toByteArray();
		dos.write(mensajeBytes);
	}
	
	public synchronized String receiveRules()  {
		NGStringMessage response = null;
		try {
			response = (NGStringMessage)
					 NGMessage.readMessageFromSocket(dis);
			return response.getString();
		} catch (IOException e) {
			System.out.println("Imposible conectar con el servidor (receiveRules)");
		}
		return "Error! El server requiere un fichero en formato '(Nombre del juego)Rules' en el directorio";
		
	}
	
	public synchronized Set<NGPlayerInfo> receiveStatus()  {
		NGPlayersMessage mensaje = null;
		try {
			NGMessage temp =
					 NGMessage.readMessageFromSocket(dis);
			mensaje = (NGPlayersMessage)
					 temp;
			Set<NGPlayerInfo> players = mensaje.getPlayers();
			return players;
			
		} catch (IOException e) {
			System.out.println("Imposible conectar con el servidor (receiveStatus)");
			return null;
		}
		
		
	}
	
	public NGMessage recibirInfoServer() {
		try {
			NGMessage temp =
			 	NGMessage.readMessageFromSocket(dis);
			return temp;
		} catch (IOException e) {
			System.out.println("Imposible conectar con el servidor (receiveStatus)");
			return null;
		}
	}
	
	
	public void sendExitRoom(short sala) throws IOException{
		NGShortMessage mensaje = (NGShortMessage)
				NGMessage.makeShortMessage(NGMessage.OP_EXIT_ROOM, sala);
		byte[] mensajeBytes = mensaje.toByteArray();
		dos.write(mensajeBytes);

	}
	

	//To close the communication with the server
	public void disconnect() {
		NGBasicMessage mensaje = (NGBasicMessage)
				NGMessage.makeBasicMessage(NGMessage.OP_QUIT);
		byte[] mensajeBytes = mensaje.toByteArray();
		try {
			dos.write(mensajeBytes);
			// La excepcion no deberia saltar nunca, ya hemos contactado antes con el server
		} catch (IOException e) {
			System.out.println("Imposible conectar con el servidor");
		}
		
	}

	public void sendAnswerToServer(String answer) {
		NGStringMessage mensaje = (NGStringMessage)
				NGMessage.makeStringMessage(NGMessage.OP_ANSWER, answer);
		byte[] mensajeBytes = mensaje.toByteArray();
		try {
			dos.write(mensajeBytes);
			// La excepcion no deberia saltar nunca, ya hemos contactado antes con el server
		} catch (IOException e) {
			System.out.println("Imposible conectar con el servidor (Request answer)");
		}
		
	}
}
