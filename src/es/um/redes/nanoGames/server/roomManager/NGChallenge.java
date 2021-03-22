package es.um.redes.nanoGames.server.roomManager;



/**
 * Representa la informacion actual sobre un desafio
 * en este caso el desafio se plantea como el tablero de juego
 * el cual se ve sometido a diversos cambios durante la partida segun 
 * los movimientos realizados por los jugadores
 */

public class NGChallenge {
	public short challengeNumber;
	public char[][] challenge;
	
	//Status initialization
	NGChallenge() {
		challengeNumber = 0;
		challenge = null;
	}

	public NGChallenge(short currentChallengeNumber, char[][] currentChallenge) {
		this.challengeNumber = currentChallengeNumber;
		challenge = currentChallenge;
	}
	
	public short getChallengeNumber() {
		return challengeNumber;
	}
	
	public char[][] getChallenge() {
		return challenge;
	}

}
