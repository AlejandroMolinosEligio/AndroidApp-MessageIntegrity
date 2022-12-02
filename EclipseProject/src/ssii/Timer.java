	package ssii;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.lang.Thread;

// Clase para la ceración de hilo secundario

public class Timer implements Runnable {

	public static void addEntry(Float value3) {

		// Declaramos diercción de base de datos, consulta SQL, fecha actual y mes

		String sql = "INSERT INTO tendencias(tendence,month,value) VALUES(?,?,?)";
		String url = "jdbc:sqlite:src/example.db";
		LocalDateTime fecha_actual = LocalDateTime.now();
		Integer month = fecha_actual.getMonthValue();

		// Consultamos las últimas tendencias y guardamos valores asociados

		String consulta = getLastTendencias();
		String[] parametros = consulta.split(";");

		Float value1 = Float.parseFloat(parametros[1]);
		Float value2 = Float.parseFloat(parametros[3]);

		// Declaramos valoración Negativa por defecto

		String value = "NEGATIVA";

		// Comprobamos resultados restantes de la valoración

		if (((value3 > value1) && (value3 > value2)) | ((value3 > value1) && (value3.equals(value2)))
				| ((value3.equals(value1)) && (value3 > value2))) {
			value = "POSITIVA";
		} else if (value3.equals(value2) && value3.equals(value1)) {
			value = "NULA";
		}

		// Conectamos con la base de datos y añadimos nueva entrada con la tendencia
		// resuelta

		try (Connection conn = DriverManager.getConnection(url); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, value);
			pstmt.setInt(2, month);
			pstmt.setFloat(3, value3);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		// Escribimos en el registro de tendencias

		writeTendences(value3, value);

		System.out.println("Añadidos los datos");
	}

	// Método para la escritura de registro tendencias

	private static void writeTendences(Float value, String tendence) {

		// Declaramos variables principales a utilizar

		LocalDateTime fecha_actual = LocalDateTime.now();
		FileWriter fichero = null;
		PrintWriter pw = null;

		// Establecemos valor por defecto de la tendencia y comprobamos el resto por si
		// debemos modificarlo

		String ten = "+";

		if (tendence.equals("NULA")) {
			ten = "O";
		} else if (tendence.equals("NEGATIVA")) {
			ten = "-";
		}

		// Escribimos el fichero de tendencias con la nueva entrada

		try {
			fichero = new FileWriter("src/tendences/tendences.txt", true);
			pw = new PrintWriter(fichero);
			pw.println("[FECHA]= " + LocalDate.now().getMonth() + "/" + fecha_actual.getYear() + "; [RATIO]= " + value
					+ "; [TENDENCIA]= " + ten);
			fichero.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Método para obtener las dos últimas tendencias

	public static String getLastTendencias() {

		// Declaramos consula SQL y dirección de base de datos

		String sql = "SELECT value,month FROM tendencias ORDER BY id DESC LIMIT 2";
		String url = "jdbc:sqlite:src/example.db";

		// Declaramos variable a devolver

		String res = "";

		// Conectamos con base de datos y recuperamos los valores de la tendencia

		try (Connection conn = DriverManager.getConnection(url);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			// loop through the result set
			while (rs.next()) {
				res += rs.getInt("month") + ";" + rs.getString("value") + ";";

			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return res;
	}

	// Método para guardar archivo log mensual

	public static void saveMontLog() {

		// Declaramos variables principales del método

		LocalDateTime fecha_actual = LocalDateTime.now();
		File archivo = null;
		FileWriter fichero = null;
		PrintWriter pw = null;
		FileReader fr = null;
		BufferedReader br = null;
		List<String> lines = new ArrayList<>();

		// Declaramos contadores para recuento

		Integer total = 0;
		Integer correct = 0;

		// Leemos el fichero log actual

		try {
			archivo = new File("src/logs/log.txt");
			fr = new FileReader(archivo);
			br = new BufferedReader(fr);

			// Comprobamos resultado de la linea y aumentamos contador en función

			String linea;

			while ((linea = br.readLine()) != null) {
				lines.add(linea);
				total += 1;
				if (linea.contains("true"))
					correct += 1;
			}

			// Realizamos porcentaje

			Float div = ((float) correct / (float) total) * 100;

			// Creamos fichero log mensual

			fichero = new FileWriter("src/logs/log" + fecha_actual.toString().substring(0, 10) + ".txt");
			pw = new PrintWriter(fichero);

			// Añadimos entrada a la base de datos

			addEntry(div);

			// Escribimos el log mensual

			pw.println("#################### " + div + "% de validaciones correctas ####################\n");

			for (int i = 0; i < lines.size(); i++)
				pw.println(lines.get(i));

			fichero.close();

			// Borramos fichero log original para que inicie el nuevo mes limpio

			fichero = new FileWriter("src/logs/log.txt");
			pw = new PrintWriter(fichero);
			pw.println("");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// Aprovechamos el finally para
				// asegurarnos que se cierra el fichero.
				if (null != fichero)
					fichero.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

	}

	// Método para la ejecución del temporizador

	public static void startTimer() {

		// Iniciamos un bucle infinito en el que comprobará cada día si ha cambiado de
		// mes

		while (true) {

			// Declaramos variables principales a utilizar

			String entrada = getLastTendencias();
			Integer last_month = Integer.parseInt(entrada.split(";")[0]);
			LocalDateTime fecha_actual = LocalDateTime.now();
			Integer month = fecha_actual.getMonthValue();

			// Comprobamos si hemos cambiado de mes

			if (last_month != month) {

				saveMontLog();
			}

			// Esperamos un día hasta la siguiente comprobación

			try {
				Thread.sleep(1000 * 60 * 60 * 24);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	// Método principal requerido por los hilos para poder inicializarlo

	public void run() {

		// Llamada a método principal del sistema

		startTimer();

	}

}
