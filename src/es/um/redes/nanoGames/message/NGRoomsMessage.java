package es.um.redes.nanoGames.message;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import es.um.redes.nanoGames.server.roomManager.NGRoomDescription;

public class NGRoomsMessage extends NGMessage {
		
		// ATRIBUTOS
	private List<NGRoomDescription> roomList;
		
	// CONTRUCTOR
	public NGRoomsMessage (byte code, List<NGRoomDescription> roomList) {
		opcode = code;
		this.roomList = roomList;
	}
	
	// FUNCIONALIDAD
	
	@Override
	public byte[] toByteArray() {
		int tam=0;
		for (NGRoomDescription i : roomList) {
			tam+=i.toByteArray().length;
		}
		ByteBuffer bb = ByteBuffer.allocate(1+2+tam);
		bb.put(opcode);
		bb.putShort( (short) roomList.size());
		for (NGRoomDescription i : roomList) {
			bb.put(i.toByteArray());
		}
		byte[] men = bb.array();
		return men;
	}

	public static NGRoomsMessage readFromIS(byte code, DataInputStream dis) throws IOException {
		short Nr =dis.readShort();
		NGRoomsMessage objeto = new NGRoomsMessage (code,new ArrayList<NGRoomDescription>());
		for (int i = 0; i<Nr; i++)
			objeto.roomList.add(NGRoomDescription.readFromIS(dis));
		return objeto;
		
	}

	public List<NGRoomDescription> getRoomList() {
		return new ArrayList<NGRoomDescription>(roomList);
	}

	
}
	