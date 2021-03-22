package es.um.redes.nanoGames.message;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class NGShortMessage extends NGMessage {
	// ATRIBUTOS
	// byte opcode
	private short shrt;

	// CONTRUCTOR
	public NGShortMessage(byte code, short shrt) {
		opcode = code;
		this.shrt = shrt;
	}
	
	// FUNCIONALIDAD
	@Override
	public byte[] toByteArray() {
		ByteBuffer bb = ByteBuffer.allocate(3);
		bb.put(opcode); 			
		bb.putShort(shrt);  		
		byte[] men = bb.array();    //Obtiene todo el mensaje como byte[]
		return men;
	}

	public static NGShortMessage readFromIS(byte code, DataInputStream dis) throws IOException {
		Short shr = dis.readShort();
		return new NGShortMessage(code,shr);
		
	}

	//TODO Replace this method according to your specific message
	public short getShort() {
		return shrt;
	}
}
