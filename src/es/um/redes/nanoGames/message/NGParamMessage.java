package es.um.redes.nanoGames.message;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class NGParamMessage extends NGMessage {
	// ATRIBUTOS
	// byte opcode
	private long param;

	// CONTRUCTOR
	public NGParamMessage(byte code, long param) {
		opcode = code;
		this.param = param;
	}
	
	// FUNCIONALIDAD
	@Override
	public byte[] toByteArray() {
		ByteBuffer bb = ByteBuffer.allocate(9);
		bb.put(opcode); 			//Inserta un campo de 1 byte (opcode es byte)
		bb.putLong(param);  		//Inserta un campo de 8 bytes (token es long)
		byte[] men = bb.array();    //Obtiene todo el mensaje como byte[]
		return men;
	}

	public static NGParamMessage readFromIS(byte code, DataInputStream dis) throws IOException {
		long param = dis.readLong();
		return new NGParamMessage(code,param);
		
	}

	//TODO Replace this method according to your specific message
	public long getParam() {
		return param;
	}
}
