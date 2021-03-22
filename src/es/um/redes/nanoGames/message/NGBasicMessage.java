package es.um.redes.nanoGames.message;

import java.nio.ByteBuffer;

public class NGBasicMessage extends NGMessage {
	// ATRIBUTOS
	// byte opcode
	
	//CONSTRUCTOR
	public NGBasicMessage(byte code) {
		opcode = code;
	}
	
	// FUNCIONALIDAD
	@Override
	public byte[] toByteArray() {
		ByteBuffer bb = ByteBuffer.allocate(1);
		bb.put(opcode); 			//Inserta un campo de 1 byte (opcode es byte)
		byte[] men = bb.array();    //Obtiene todo el mensaje como byte[]
		return men;
	}
}
