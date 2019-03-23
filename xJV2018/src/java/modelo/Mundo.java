/** 
 * Proyecto: Juego de la vida.
 * Organiza aspectos de gestión de la simulación según el modelo2
 * @since: prototipo1.2
 * @source: Simulacion.java 
 * @version: 2.0 - 2019.03.23
 * @author: ajp
 */

package modelo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Mundo implements Serializable {

	public static final int TAMAÑO_MUNDO = 18;
	private String nombre;
	private byte[][] espacio;
	private int tamañoMundo;
	private List<Posicion> distribucion;
	private Map<String, int[]> constantes;

	public enum FormaEspacio { PLANO, ESFERICO }
	private FormaEspacio tipoMundo;

	public Mundo(String nombre, byte[][] espacio, 
			List distribucion, Map constantes, FormaEspacio tipoMundo) {
		this.nombre = nombre;
		this.espacio = espacio;
		this.constantes = constantes;
		this.distribucion = new LinkedList<Posicion>();
		this.tipoMundo = tipoMundo;
		//cargarDistribucion();
		this.tamañoMundo = espacio.length;	
		
		establecerLeyes();
	}
	
	public Mundo() {	
		this("Demo1", new byte[TAMAÑO_MUNDO][TAMAÑO_MUNDO], 
				new LinkedList<Posicion>(), 
				new HashMap<String, int[]>(), FormaEspacio.PLANO);
	}

	public Mundo(Mundo mundo) {
		this.nombre = new String(mundo.nombre);
		this.espacio = mundo.espacio.clone();
		this.distribucion = new LinkedList<Posicion>(mundo.distribucion);
		this.constantes = new HashMap<String, int[]>(mundo.constantes);
		this.tipoMundo = mundo.tipoMundo;
		this.tamañoMundo = espacio.length;
		
		//cargarDistribucion();
		establecerLeyes();
	}
	
	public String getId() {
		return nombre;
	}
	
	public FormaEspacio getTipoMundo() {
		return tipoMundo;
	}
	
	public int getTamañoMundo() {
		return tamañoMundo;
	}
	
	public void setEspacio(byte[][] espacio) {
		assert espacio != null;
		this.espacio = espacio;
	}
	
	private void establecerLeyes() {
		constantes.put("ValoresSobrevivir", new int[] {2, 3});
		constantes.put("ValoresRenacer", new int[] {3});
	}
	
	private void cargarDistribucion() {
		espacio = new byte[tamañoMundo][tamañoMundo];
		for(Posicion pos : distribucion) {
			espacio[pos.getX()][pos.getY()] = 1;
		}	
	}

	private void extraerDistribucion() {
		for (int i=0; i < espacio.length; i++) {
			for (int j=0; i < espacio.length; j++) {
				if(espacio[i][j] == 1) {	
					distribucion.add(new Posicion(i, j));
					tamañoMundo = i;
				}
			}	
		}
	}
	
	/**
	 * Despliega en texto el estado almacenado, corresponde
	 * a una generación del Juego de la vida.
	 */
	public String toStringEstadoMundo() {	
		StringBuilder salida = new StringBuilder();	
		for (int i = 0; i < espacio.length; i++) {
			for (int j = 0; j < espacio.length; j++) {		
				salida.append(espacio[i][j] == 1 ? "|o" : "| ");
			}
			salida.append("|\n");
		}
		return salida.toString();
	}

	/**
	 * Actualiza el estado del Juego de la Vida.
	 * Actualiza según la configuración establecida para la forma del espacio.
	 */
	public void actualizarMundo() {
		if (tipoMundo == FormaEspacio.PLANO) {
			actualizarMundoPlano();
		}
		if (tipoMundo == FormaEspacio.ESFERICO) {
			actualizarMundoEsferico();
		}
	}

	/**
	 * Actualiza el estado almacenado del Juego de la Vida.
	 * Las celdas periféricas son adyacentes con las del lado contrario.
	 * El mundo representado sería esférico cerrado sin límites para células de dos dimensiones.
	 */
	private void actualizarMundoEsferico()  {     					
		byte[][] nuevoEstado = new byte[espacio.length][espacio.length];

		for (int i = 0; i < espacio.length; i++) {
			for (int j = 0; j < espacio.length; j++) {

				int filaSuperior = i-1;
				int filaInferior = i+1;
				// Reajusta filas adyacentes.
				if (i == 0) {
					filaSuperior = espacio.length-1;
				}
				if (i == espacio.length-1) {
					filaInferior = 0;
				}

				int colAnterior = j-1;
				int colPosterior = j+1;
				// Reajusta columnas adyacentes.
				if (j == 0) {
					colAnterior = espacio.length-1;
				}
				if (j == espacio.length-1) {
					colPosterior = 0;
				}

				int vecinas = 0;							
				vecinas += espacio[filaSuperior][colAnterior];			// Celda NO
				vecinas += espacio[filaSuperior][j];					// Celda N		NO | N | NE
				vecinas += espacio[filaSuperior][colPosterior];			// Celda NE   	-----------
				vecinas += espacio[i][colPosterior];					// Celda E		 O | * | E
				vecinas += espacio[filaInferior][colPosterior];			// Celda SE	  	----------- 
				vecinas += espacio[filaInferior][j]; 					// Celda S		SO | S | SE
				vecinas += espacio[filaInferior][colAnterior]; 			// Celda SO 
				vecinas += espacio[i][colAnterior];						// Celda O           			                                     	

				actualizarCelda(nuevoEstado, i, j, vecinas);
			}
		}
		espacio = nuevoEstado;
	}

	/**
	 * Actualiza el estado de cada celda almacenada del Juego de la Vida.
	 * Las celdas periféricas son los límites absolutos.
	 * El mundo representado sería plano, cerrado y con límites para células de dos dimensiones.
	 */
	private void actualizarMundoPlano()  {     					
		byte[][] nuevoEstado = new byte[TAMAÑO_MUNDO][TAMAÑO_MUNDO];

		for (int i = 0; i < TAMAÑO_MUNDO; i++) {
			for (int j = 0; j < TAMAÑO_MUNDO; j++) {
				int vecinas = 0;							
				vecinas += visitarCeldaNoroeste(i, j);		
				vecinas += visitarCeldaNorte(i, j);			// 		NO | N | NE
				vecinas += visitarCeldaNoreste(i, j);		//    	-----------
				vecinas += visitarCeldaEste(i, j);			// 		 O | * | E
				vecinas += visitarCeldaSureste(i, j);		// 	  	----------- 
				vecinas += visitarCeldaSur(i, j); 			// 		SO | S | SE
				vecinas += visitarCeldaSuroeste(i, j); 	  
				vecinas += visitarCeldaOeste(i, j);		          			                                     	

				actualizarCelda(nuevoEstado, i, j, vecinas);
			}
		}
		espacio = nuevoEstado;
	}

	/**
	 * Aplica las leyes del mundo a la celda indicada dada la cantidad de células adyacentes vivas.
	 * @param nuevoEstado
	 * @param fila
	 * @param col
	 * @param vecinas
	 */
	private void actualizarCelda(byte[][] nuevoEstado, int fila, int col, int vecinas) {	
		for (int valor : constantes.get("ValoresRenacer")) {
			if (vecinas == valor) {									// Pasa a estar viva.
				nuevoEstado[fila][col] = 1;
				return;
			}
		}	
		for (int valor : constantes.get("ValoresSobrevivir")) {
			if (vecinas == valor && espacio[fila][col] == 1) {		// Permanece viva, si lo estaba.
				nuevoEstado[fila][col] = 1;
				return;
			}
		}
	}

	/**
	 * Obtiene el estado o valor de la celda vecina situada al Oeste de la indicada por la coordenada. 
	 * @param fila de la celda evaluada.
	 * @param col de la celda evaluada.
	 * @return el estado o valor de la celda Oeste.
	 */
	private byte visitarCeldaOeste(int fila, int col) {
		if (col-1 >= 0) {
			return espacio[fila][col-1]; 			// Celda O.
		}
		return 0;
	}

	/**
	 * Obtiene el estado o valor de la celda vecina situada al Suroeste de la indicada por la coordenada. 
	 * @param fila de la celda evaluada.
	 * @param col de la celda evaluada.
	 * @return el estado o valor de la celda Suroeste.
	 */
	private byte visitarCeldaSuroeste(int fila, int col) {
		if (fila+1 < TAMAÑO_MUNDO && col-1 >= 0) {
			return espacio[fila+1][col-1]; 		// Celda SO.
		}
		return 0;
	}

	/**
	 * Obtiene el estado o valor de la celda vecina situada al Sur de la indicada por la coordenada. 
	 * @param fila de la celda evaluada.
	 * @param col de la celda evaluada.
	 * @return el estado o valor de la celda Sur.
	 */
	private byte visitarCeldaSur(int fila, int col) {
		if (fila+1 < TAMAÑO_MUNDO) {
			return espacio[fila+1][col]; 			// Celda S.
		}
		return 0;
	}

	/**
	 * Obtiene el estado o valor de la celda vecina situada al Sureste de la indicada por la coordenada. 
	 * @param fila de la celda evaluada.
	 * @param col de la celda evaluada.
	 * @return el estado o valor de la celda Sureste.
	 */
	private byte visitarCeldaSureste(int fila, int col) {
		if (fila+1 < TAMAÑO_MUNDO && col+1 < TAMAÑO_MUNDO) {
			return espacio[fila+1][col+1]; 		// Celda SE.
		}
		return 0;
	}

	/**
	 * Obtiene el estado o valor de la celda vecina situada al Este de la indicada por la coordenada. 
	 * @param fila de la celda evaluada.
	 * @param col de la celda evaluada.
	 * @return el estado o valor de la celda Este.
	 */
	private byte visitarCeldaEste(int fila, int col) {
		if (col+1 < TAMAÑO_MUNDO) {
			return espacio[fila][col+1]; 			// Celda E.
		}
		return 0;
	}

	/**
	 * Obtiene el estado o valor de la celda vecina situada al Noreste de la indicada por la coordenada. 
	 * @param fila de la celda evaluada.
	 * @param col de la celda evaluada.
	 * @return el estado o valor de la celda Noreste.
	 */
	private byte visitarCeldaNoreste(int fila, int col) {
		if (fila-1 >= 0 && col+1 < TAMAÑO_MUNDO) {
			return espacio[fila-1][col+1]; 		// Celda NE.
		}
		return 0;
	}

	/**
	 * Obtiene el estado o valor de la celda vecina situada al NO de la indicada por la coordenada. 
	 * @param fila de la celda evaluada.
	 * @param col de la celda evaluada.
	 * @return el estado o valor de la celda NO.
	 */
	private byte visitarCeldaNorte(int fila, int col) {
		if (fila-1 >= 0) {
			return espacio[fila-1][col]; 			// Celda N.
		}
		return 0;
	}

	/**
	 * Obtiene el estado o valor de la celda vecina situada al Noroeste de la indicada por la coordenada. 
	 * @param fila de la celda evaluada.
	 * @param col de la celda evaluada.
	 * @return el estado o valor de la celda Noroeste.
	 */
	private byte visitarCeldaNoroeste(int fila, int col) {
		if (fila-1 >= 0 && col-1 >= 0) {
			return espacio[fila-1][col-1]; 		// Celda NO.
		}
		return 0;
	}

} // class