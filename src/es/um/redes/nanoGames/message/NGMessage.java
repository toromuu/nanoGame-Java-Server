package es.um.redes.nanoGames.message;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import es.um.redes.nanoGames.server.NGPlayerInfo;
import es.um.redes.nanoGames.server.roomManager.NGRoomDescription;

public abstract class NGMessage {
	protected byte opcode;

	public static final byte OP_INVALID_CODE = 0;
	public static final byte OP_BASIC = 1;
	public static final byte OP_PARAM = 2;
	public static final byte OP_NICK = 3;
	public static final byte OP_ROOM_LIST = 4;
	public static final byte OP_MATRIX = 5;
	public static final byte OP_TOKEN_OK = 7;
	public static final byte OP_TOKEN_ERROR = 8;
	public static final byte OP_NICK_OK = 9;
	public static final byte OP_DUPLICATED_NICK = 10;
	public static final byte OP_GET_ROOM_LIST = 11;
	public static final byte OP_QUIT = 12;
	public static final byte OP_ENTER_ROOM = 13;
	public static final byte OP_ACCESS_OK = 14;
	public static final byte OP_ACCESS_ERROR = 15;
	public static final byte OP_EXIT_ROOM = 16;
	public static final byte OP_SEND_RULES = 17;
	public static final byte OP_SHORT = 19;
	public static final byte OP_SEND_PLAYERS = 20; //tipo status connecta4
	public static final byte OP_ANSWER = 21;
	public static final byte OP_GET_STATUS = 22;
	public static final byte OP_GET_RULES = 23;
	public static final byte OP_TIMEOUT = 24;
	

	//Returns the opcode of the message
	public byte getOpcode() {
		return opcode;

	}
	
	//crear un metodo que para leer el string con dis. read fully (nickname) en ngmessage
			//crear el array de bytes
			//read fully

	//Method to be implemented specifically by each subclass of NGMessage
	protected abstract byte[] toByteArray();

	//Reads the opcode of the incoming message and uses the subclass to parse the rest of the message
	public static NGMessage readMessageFromSocket(DataInputStream dis) throws IOException { 
		byte operation = dis.readByte();
		//We use the operation to differentiate among all the subclasses
		switch (operation) {
		//TODO additional messages
		//The following case is just an example
		case (OP_BASIC): {
			return makeBasicMessage(operation);
		}
		case (OP_TOKEN_OK): {
			return makeBasicMessage(operation);
		}
		case (OP_TOKEN_ERROR): {
			return makeBasicMessage(operation);
		}
		case (OP_NICK_OK): {
			return makeBasicMessage(operation);
		}
		case (OP_DUPLICATED_NICK): {
			return makeBasicMessage(operation);
		}
		case (OP_QUIT): {
			return makeBasicMessage(operation);
		}
		case (OP_GET_ROOM_LIST): {
			return makeBasicMessage(operation);
		}
		case (OP_PARAM): {
			return NGParamMessage.readFromIS(operation, dis);
		}
		case (OP_SHORT): {
			return NGShortMessage.readFromIS(operation, dis);
		}
		case (OP_NICK): {
			return NGStringMessage.readFromIS(operation, dis);
		}
		case (OP_ROOM_LIST): {
			return NGRoomsMessage.readFromIS(operation, dis);
		}
		case (OP_SEND_PLAYERS): {
			return NGPlayersMessage.readFromIS(operation, dis);
		}
		case (OP_GET_STATUS): {
			return makeBasicMessage(operation);
		}
		
		case (OP_GET_RULES): {
			return makeBasicMessage(operation);
		}
		
		case (OP_ENTER_ROOM): {
			return NGShortMessage.readFromIS(operation, dis);
		}
		case (OP_ACCESS_OK): {
			return makeBasicMessage(operation);
		}
		case (OP_ACCESS_ERROR): {
			return makeBasicMessage(operation);
		}
		case (OP_SEND_RULES): {
			return NGStringMessage.readFromIS(operation, dis);
		}
		case (OP_EXIT_ROOM): {
			return NGShortMessage.readFromIS(operation, dis);
		}
		case (OP_MATRIX): {
			return NGMatrixMessage.readFromIS(operation, dis);
		}
		case (OP_ANSWER) : {
			return NGStringMessage.readFromIS(operation, dis);
		}
		case (OP_TIMEOUT): {
			return makeBasicMessage(operation);
		}
		default:
			System.err.println("Unknown message type received:"+operation);
		}
		return null;
	}
	
	
	//The following method is just an example
	public static NGMessage makeBasicMessage(byte code) {
		return (new NGBasicMessage(code));
	}
	
	public static NGMessage makeParamMessage(byte code, long parametro) {
		return (new NGParamMessage(code, parametro));
	}
	
	public static NGMessage makeShortMessage(byte code, short shrt) {
		return (new NGShortMessage(code, shrt));
	}
	
	public static NGMessage makeRoomsMessage(byte code, List<NGRoomDescription> roomList) {
		return (new NGRoomsMessage(code, roomList));
	}
	
	public static NGMessage makeStringMessage(byte code, String string) {
		return (new NGStringMessage(code, string));
	}
	public static NGMessage makeMatrixMessage(byte code, char[][] data) {
		return (new NGMatrixMessage(code,data));
	}
	
	public static NGMessage makePlayersMessage(byte code, Set<NGPlayerInfo> players) {
		return (new NGPlayersMessage(code, players));
	}

	
	//metodo para obtener la secuencia de bytes que contiene el string en el mensaje
	public static  String readString( DataInputStream dis ) throws IOException {
		int longitud=dis.readInt();
		byte [] nick= new byte[longitud];
		dis.readFully(nick);
		return new String(nick);
	
	}
	
	//metodo sirve para hacer un cast de entero a byte
	public static byte castInt(byte i1) {
		byte b1 = (byte)i1;
		return b1;
		
	}
	
}
