package es.um.redes.nanoGames.server.roomManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import es.um.redes.nanoGames.server.NGPlayerInfo;
public class NGConnect4Manager extends NGRoomManager {
	
	private char[][] tablero;
	// Necesitamos unas estructuras para saber el orden de juego (al ser solo dos, no es estrictamente necesario, pero conviene parametrizar)
	private List<String> orden;
	private int turno;
	
	
	public NGConnect4Manager(short id, int filas, int columnas) {
		this.id = id;
		this.registrationName = (byte) 0;
		this.players = new HashSet<NGPlayerInfo>();
		rules = new File("Connect4Rules");
		tablero = new char[filas][columnas];
		resetearTablero();
		orden = new ArrayList<String>();
		turno = 0;
		challengeNumber=0;
		statusNumber=0;
		gameTimeout= 20000;
	}
	
	public NGConnect4Manager(short id) {
		this(id, 7, 7);	
	}

	public int getAltura() {
		return tablero.length;
	}
	
	public int getAnchura() {
		return tablero[0].length;
	}
	
	@Override
	public synchronized boolean registerPlayer(NGPlayerInfo p) {
		if (players.size() < 2){ 
			resetearTablero();
			players.add(p);
			orden.add(p.getNick());
			statusNumber++;
			if (players.size()==2) challengeNumber++;
			return true;
		}
		System.out.println("La sala esta llena");
		return false;
	}

	@Override
	public synchronized NGRoomStatus checkStatus(NGPlayerInfo p) {
			return new NGRoomStatus(statusNumber, players );
		
	}

	@Override
	public synchronized NGChallenge checkChallenge(NGPlayerInfo p) {
		if (players.size()>=2 && orden.get(turno) == p.getNick()) // Si es el turno del jugador
			return new NGChallenge(challengeNumber, tablero);
		return null;
	}

	@Override
	public synchronized NGRoomStatus noAnswer(NGPlayerInfo p) {
		turno=(turno+1) % playersInRoom();
		challengeNumber++;
		gameTimeout=20000;
		return new NGRoomStatus(statusNumber, players);
	}

	@Override
	public synchronized NGRoomStatus answer(NGPlayerInfo p, String answer) {
		// Se modifica el tablero, habria un nuevo challenge
		challengeNumber++;	
		
		// Comprobamos si el movimiento es valido
		int y;
		try {
		y = Integer.parseInt(answer);
		y--;
		}
		catch (NumberFormatException e) {
			y=-1;
		}
		if (movimientoInvalido(y)) {
			turno=(turno+1)%players.size();
			return null;
		}
		
		// Insertar la ficha
		int x = tablero.length-1;
		while (x>=0 && tablero[x][y] != ' '){
			x--;
		}
		char player;
		if (orden.indexOf(p.getNick())==0)
			player = 'o';
		else
			player = 'x';
		tablero[x][y]=player;
		//Comprobar si se ha ganado
		if (hayGanador(player,x,y)) {
			for (NGPlayerInfo i : players)
				if (i.getNick().equals(p.getNick()))
					i.incrementarScore();
			statusNumber++;
			resetearTablero();
		}
		turno=(turno+1)%players.size();
		gameTimeout=20000;
		return new NGRoomStatus(statusNumber, players);	
	}


	@Override
	public synchronized void removePlayer(NGPlayerInfo p) {
		for (NGPlayerInfo i : players)
			i.score=0;
		players.remove(p);
		orden.remove(p.getNick());
		turno=0;
		statusNumber++;
	}

	@Override
	public NGRoomManager duplicate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte getRegistrationName() {
		return registrationName;
	}

	@Override
	public synchronized NGRoomDescription getDescription() {
		return new NGRoomDescription(id, registrationName, players);
	}

	@Override
	public synchronized int playersInRoom() {
		return players.size();
	}

	
	//FUNCIONES AUXILIARES DE LA LOGICA DEL JUEGO
	protected boolean movimientoInvalido(int y){
		return ((y < 0 || y >= getAnchura()) || tablero[0][y]!=' ');
	}
	
	protected boolean hayGanador(char player, int x, int y) {
		//HORIZONTAL.
		int enLinea=1;
		int i = y+1;
	    while(i<getAltura() && tablero[x][i] == player){
	    	enLinea++;
		    i++;
	    }
	    i=y-1;
		while(i>=0 && tablero[x][i] == player){
			enLinea++;
			i--;
		}
		if(enLinea == 4) {
			return true; 
		}
		
		//VERTICAL.
		enLinea=1;
        int j=x+1;
        while(j<getAnchura() && tablero[j][y] == player){
        	enLinea++;
            j++;
        }

        if(enLinea == 4) {
            return true;
        }
        
     // DIAGONAL PRINCIPAL
        enLinea=1;
        i=x+1;
        j=y+1;
        while(i<getAltura() && j<getAnchura() && tablero[i][j] == player){
        	enLinea++;
            i++;
            j++;
        }
        i=x-1;
        j=y-1;

        while(i>=0 && j>=0 && tablero[i][j] == player){
        	enLinea++;
            i--;
            j--;
        }

        if(enLinea == 4){
            return true;
        }
        
        // DIAGONAL SECUNDARIA
        enLinea=1;
        i=x+1;
        j=y-1;
        while(i<getAltura() && j>=0 && tablero[i][j] == player){
        	enLinea++;
            i++;
            j--;
        }

        i=x-1;
        j=y+1;
        while(i>=0 && j<getAnchura() && tablero[i][j] == player){
        	enLinea++;
            i--;
            j++;
        }

        if(enLinea == 4){
            return true;
        }
        return false;
	}
	
	
	protected void resetearTablero() {
		for (int i = 0; i< getAltura(); i++)
			for(int j = 0; j< getAnchura(); j++)
				tablero[i][j]=' ';
	}
	
}
