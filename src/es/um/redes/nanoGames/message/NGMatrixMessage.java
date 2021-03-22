package es.um.redes.nanoGames.message;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class NGMatrixMessage extends NGMessage {

	
	// ATRIBUTOS
	private char[][] matrix;
		
	// CONTRUCTOR
	public NGMatrixMessage (byte code,char[][] matrix) {
		opcode = code;
		this.matrix=matrix;
	}
	
	// FUNCIONALIDAD
		
	@Override
	public byte[] toByteArray() {
		int Filas = matrix.length;
		int Columnas = matrix[0].length;
		ByteBuffer bb = ByteBuffer.allocate(1+4+4+Columnas*Filas*2);
		bb.put(opcode); //Inserta un campo de 1 byte (opcode es byte)	
		bb.putInt(Filas);
		bb.putInt(Columnas);
		for (int i = 0; i<Filas; i++)
			for (int j = 0; j<Columnas; j++)
					bb.putChar(matrix[i][j]);
		byte[] men = bb.array();    
		return men;
	}

	public static NGMatrixMessage readFromIS(byte code, DataInputStream dis) throws IOException {
		int F=dis.readInt();
		int C=dis.readInt();
		char[][] matrix = new char[F][C];
		for (int i = 0; i<F; i++)
			for (int j = 0; j<C; j++)
				matrix[i][j] = dis.readChar();
		
		return new NGMatrixMessage (code,matrix);
		
	}

	public int getColumnas() {
		return matrix[0].length;
	}

	public int getFilas() {
		return matrix.length;
	}

	public int getCxF() {
		return matrix.length*matrix[0].length;
	}

	public char[][] getMatrix() {
		return matrix;
	}
				
	}
