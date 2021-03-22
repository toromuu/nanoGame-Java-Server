package es.um.redes.nanoGames.message;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class NGStringMessage extends NGMessage {
	// ATRIBUTOS
	// byte opcode
	private String string;

	// CONTRUCTOR
	public NGStringMessage(byte code, String string) {
		opcode = code;
		this.string = string;
	}
	
	// FUNCIONALIDAD
	@Override
	public byte[] toByteArray() {
		ByteBuffer bb = ByteBuffer.allocate(1+4+string.length());
		bb.put(opcode); 			//Inserta un campo de 1 byte (opcode es byte)
		bb.putInt(string.length());  		//Inserta un campo de 8 bytes (token es long)
		bb.put(string.getBytes());
		byte[] men = bb.array();    //Obtiene todo el mensaje como byte[]
		return men;
	}

	public static NGStringMessage readFromIS(byte code, DataInputStream dis) throws IOException {
		return new NGStringMessage(code, readString(dis));
		
	}

	//TODO Replace this method according to your specific message
	public String getString() {
		return string;
	}
}
