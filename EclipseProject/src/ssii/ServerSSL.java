package ssii;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import java.io.*;
import java.time.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ServerSSL {

	// Método para guardar entrada en archivo log

	public static void guardarLog(String pedido, Boolean resultado, Integer usuario) {

		// Definimos las variables principales para la lectura y escritura del fichero

		FileWriter fichero = null;
		PrintWriter pw = null;

		// Realizamos el intento de lectura del fichero y escribimos la entrada
		// correspondiente

		try {
			fichero = new FileWriter("src/logs/log.txt", true);
			pw = new PrintWriter(fichero);

			pw.println(LocalDateTime.now() + " --- [PEDIDO]= " + pedido + "; --- [Usuario]=" + usuario
					+ "[VERICIDAD FIRMA]= " + resultado);

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



	// Método para la creación de base de datos, tablas y datos iniciales

	public static void createNewTable() {
		// Url de conexión base de datos

		String url = "jdbc:sqlite:src/example.db";

		// Querys SQL que se van a ejecutar

		String sql = "CREATE TABLE IF NOT EXISTS users (\n" + "	id integer PRIMARY KEY AUTOINCREMENT NOT NULL,\n"
				+ "	usuario integer UNIQUE NOT NULL,\n" + " publicKey VARCHAR(10000) NOT NULL\n" + ");";

		String sq2 = "CREATE TABLE IF NOT EXISTS pedidos (\n" + "	id integer PRIMARY KEY AUTOINCREMENT,\n"
				+ " usuario integer,\n" + " pedido integer,\n" + " estado text,\n"
				+ " fechaHora  DATE DEFAULT CURRENT_TIMESTAMP,\n" + " FOREIGN KEY(usuario) REFERENCES users(usuario)\n"
				+ ");";

		String datos = "INSERT INTO users(usuario,publicKey) VALUES(?,?)";

		String sql3 = "CREATE TABLE IF NOT EXISTS tendencias (\n"
				+ "	id integer PRIMARY KEY AUTOINCREMENT NOT NULL,\n" + "	tendence text NOT NULL,\n"
				+ " month int, \n" + "	value float NOT NULL\n" + ");";

		// Conectamos con la base de datos y ejecutamos las querys anteriores

		try (Connection conn = DriverManager.getConnection(url); Statement stmt = conn.createStatement()) {
			// create a new table
			stmt.execute(sql);
			stmt.execute(sq2);
			stmt.execute(sql3);

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("Tablas creadas exitosamente");

		// Lista con el contenido de las claves públicas de los usuarios del sistema

		List<String> pubKeys = new ArrayList<>();
		pubKeys.add("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAoBrVvg/IhU3pABzYx742\r\n"
				+ "QsnQGt5Z1lYA3/nmwY9QAcxo6RjYtYy+mQgwk9wJYk6Oc2/lyHttM+Rmc12dK2Rw\r\n"
				+ "SylQA/AdHQ3hWTMtGMyFZiZWrop3K2lDzwk5D48o1bcQx+eB1Enh3ZD54Henlb/q\r\n"
				+ "PLV+Ty3QkGg0FMHM45XsvgMs/MAoajs+NfbKR1dc4M7AndCk0vyjkMNHWQjkvfPW\r\n"
				+ "Teba+lBxzGHe0ibLMrQxJlYzGqMKRFvpudQXcDy8rJDb1femRkxj3oqPWUq0hNl3\r\n"
				+ "SjkE8URQGCEBKxAxhpF/wgG1V1BeEfUp8NllaENB327VLHCd5AyYIRIDGo7QVbXV\r\n"
				+ "LHfA9NyWdxZ+peUTRQf/kYM8BdViyK4ZjP2yeifIt77RucYFT2/7XHnkaLesX2fG\r\n"
				+ "6mzrXPBYLW/gajve0SpxlbQHwxYVOWOxUP5122wCSNOte+Jh4rhCJuUUw4IRWySr\r\n"
				+ "VjoDj6kvXXUvx7Gkx/y2dishYbyJpUOSj1N6xLOG9EW5tEdHLrypt4j4WYXIEQqV\r\n"
				+ "vFqnFqX0QFW5NWV2faR3HiIat6/RMbpHuOzKr7i/f/nznBA+r0+DdllZ72AzN5w6\r\n"
				+ "EjIPHu3KttRsHCODoJcH1cj8EsmqZOaqGtwO1WsIGYNuRdfo9pZCxL3t8qFBKJ22\r\n"
				+ "CZ7EZmz81tzkpI84rW7fNSUCAwEAAQ==".replace("\r\n", "").trim());

		pubKeys.add("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAz6iKvx/1bmkCXHREEXTI\r\n"
				+ "UIlyITZG6+F1j6E7o3SAz9dSB51/5yyH29j5XWlyBB337fCmOsyZdtq9xemG0mTM\r\n"
				+ "FhtY/XK8VLq5ZQbmay1mCBuVEq0G5y4cMMVwfQlobTqltKXkeQt75NE7uxIGqn5x\r\n"
				+ "GxkyC6RFRFrFsJ2GSOyZBij+3kMx1c70/lxVk+otwAc3D78oFdkX+aIYJovNe3q/\r\n"
				+ "8bVX/t+QVvvM4FTAFmN7GnwgZsk9C9Ewvy4sb1AinpQGmSDZ2sT03jERjbqc3TNy\r\n"
				+ "TeGUW+18jL8B0nV7Uv8QDxDAyZBwBAMimbglmfz5p54DG/iNEn4xn4UpnVqoQ7wu\r\n"
				+ "euYaKgeSOviob39qXkc/vddNcAF9HX7gByVPx359lyMQ5K0JR9Pl6lMgDaYnV42r\r\n"
				+ "8E4qYTnRHK2/R/nHT6LmCCV7e09NmVahs4wVLImPNRW+GrlXdN1+odsUiCWe6P+h\r\n"
				+ "Xm0zRhDtBi/PyDhA1hm3cTfWznItYJ3lV2qkYaoDJiVLSUureOBz5Nq1XMSL/zQG\r\n"
				+ "64DpDD09NSWFiCS/hzbzNeQmHScXY2YP64rRCys2MgY/k2XvfLRsXROtc20t+KFN\r\n"
				+ "ajbeL+zjSNPEem7CyZpsAfq/TPaFQ4w12vdwGlWt2hY1ScBGadbEC7VqbKevxfrE\r\n"
				+ "7Mj75MCegO/IV7DC6OyvtQMCAwEAAQ==".replace("\r\n", "").trim());
		pubKeys.add("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAwjskDAVnpSPGBJpddSfQ\r\n"
				+ "Afu2fFfbXKVIvRKjZeqHBRjvteEc7y3OK8sIKHuN0tx+6iWnTMDEEbtiPMq0jZZ7\r\n"
				+ "fj0OZaEQMd4ygLWOuOWa4TlwfDcHdtYRZRTnsv8Of13SKJLT619fn8JbQZYCZQ16\r\n"
				+ "SBcRn/48uQohKepo6elBv7ilmXRxZHa0HgdzP852Su6OIFz5YZUPDh2jnbDdcvAS\r\n"
				+ "X3aoRXaVEBelOwMxNFpQstCuEHOsPBfsx053Hhsw6VmBMHCxvJlTvp6KHVvR1R50\r\n"
				+ "975YMpC44OVXAHzE90Dvit+js0vGeCDZf6MpLa08IILQcoh058aEl0UyTUr4YXIL\r\n"
				+ "4SHK4sD+h5oy1UmRpL2mhHQX7KGm7gWyCWqhGj3CzkSFe72Xw5KAKXAHqe3S1j45\r\n"
				+ "6T2GC4YCbAhLj8YJDrp4XdGEyRRMZ2IcsqJM57Zmryzi5oEHfDijbJ/4ox3+eJ09\r\n"
				+ "EF1E31pUAY69W/y36UWOep0ZxYXXOiXdcpQ27WrNEHJEFHkawEL30eDvNzJQ/wV/\r\n"
				+ "1k6pf9VNPUrjnIrhvgO80EN9vuu/9lT1xzrmvOnID/YdZCWWnVlSMqKzy/ahzByO\r\n"
				+ "TIHoA/TXV6v9o66S649aNTLjRnVLygQzGUaPvhI3gmGWpopuqN4RUKjSZ9To9uV2\r\n"
				+ "jt4faH1snXC6kiRFOMLvLcsCAwEAAQ==".replace("\r\n", "").trim());

		// Añadimos los datos asociados en la table de usuarios de la base de datos

		for (int i = 0; i < 3; i++) {

			try (Connection conn = DriverManager.getConnection(url);
					PreparedStatement pstmt = conn.prepareStatement(datos)) {
				pstmt.setInt(1, i + 1);
				pstmt.setString(2, pubKeys.get(i));
				pstmt.executeUpdate();
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		}

		// Declaramos e insertamos datos iniciales para tendencias

		String tendencias_iniciales = "INSERT INTO tendencias(tendence,month,value) VALUES(?,?,?)";

		for (int i = 0; i < 2; i++) {

			try (Connection conn = DriverManager.getConnection(url);
					PreparedStatement pstmt = conn.prepareStatement(datos)) {
				pstmt.setString(1, "NULA");
				pstmt.setInt(2, 9 + i);
				pstmt.setFloat(3, (float) 0.0);
				pstmt.executeUpdate();
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		}
	}

	// Método para la obtención de la clave pública del usuario

	public static PublicKey getPublicKey(Integer id) throws NoSuchAlgorithmException {

		// Especificamos ruta de conexión base de datos y declaramos varibales a
		// devolver

		String url = "jdbc:sqlite:src/example.db";
		PublicKey PublicKey = null;
		String pubKey = "";

		// Consulta SQL a la lista de usuarios

		String sql = String.format("SELECT * FROM users WHERE usuario = %d", id);

		// Realizamos conexión y devolvemos la clave pública del usuario

		try (Connection conn = DriverManager.getConnection(url);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			while (rs.next()) {
				pubKey = rs.getString("publicKey");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		// Una vez recuperada la clave pública como String la parseamos al tipo
		// PublicKey para devolverlo

		byte[] publicBytes = Base64.getMimeDecoder().decode(pubKey.getBytes());
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		try {
			PublicKey = keyFactory.generatePublic(keySpec);
		} catch (InvalidKeySpecException e) {

			System.err.println("[ERROR]: " + e);
		}

		return PublicKey;

	}

	// Método para añadir una entrada a la lista de pedidos

	public static void addEntry(Integer usuario, String estado, String pedido) {

		// Declaramos url de base de datos para la conexión y query base para insertar
		// datos

		String sql = "INSERT INTO pedidos(usuario,estado,pedido) VALUES(?,?,?)";
		String url = "jdbc:sqlite:src/example.db";

		// Conectamos con base de datos e insertamos el dato de entrada

		try (Connection conn = DriverManager.getConnection(url); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, usuario);
			pstmt.setString(2, estado);
			pstmt.setString(3, pedido);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		System.out.println("Añadidos los datos");
	}

	// Método para la comprobación de la firma

	public static Boolean comprobarFirma(String pedido, String pedidoFirmado, Integer id)
			throws NoSuchAlgorithmException {

		// Definimos variable a davolver por defecto a true

		Boolean res = true;

		// Recuperamos la clave pública del usuario de entrada

		PublicKey pubKey = getPublicKey(id);

		// Declaramos firma, introducimos datos en la misma, la clave pública y
		// verificamos firma

		Signature sig = null;
		byte[] data = (pedido+id).getBytes();
		try {
			sig = Signature.getInstance("SHA256WithRSA");
			sig.initVerify(pubKey);
			sig.update(data);
			res = sig.verify(Base64.getMimeDecoder().decode(pedidoFirmado.getBytes()));

		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			System.err.println("[ERROR]: " + e);
		}
		return res;

	}

	// Método principal del sistema

	public static void main(String[] args) throws InterruptedException {



		// Comprobación de la existencia de la base de datos, en caso negativo se crea

		File archivo = new File("src/example.db");

		if (!archivo.exists()) {
			createNewTable();
		}

		// Puerto que vamos a utilizar para SocketSSL y declaración de variables para
		// uso de KeyStore

		int port = 1712;

		TrustManager[] trustManagers;
		KeyManager[] keyManagers;

		// Creación del hilo secundario para timer mensual

		Runnable runnable = new Timer();
		Thread hilo = new Thread(runnable);
		hilo.start();

		try {

			// ============ CERTIFICADOS PROPIOS ===============

			// Cargamos nuestro certificado
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream("./keys/server/serverKey.jks"), "123456".toCharArray());

			// Preparamos nuestra lista de certificados
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, "123456".toCharArray());

			// Obtenemos nuestra lista de certificados
			keyManagers = kmf.getKeyManagers();

			// ============= CONFIANZA =====================
			// Cargamos nuestros certificados de confianza
			KeyStore trustedStore = KeyStore.getInstance("JKS");
			trustedStore.load(new FileInputStream("./keys/server/serverTrustedCerts.jks"), "123456".toCharArray());

			// Preparamos nuestra lista de confianza
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(trustedStore);

			// Obtenemos nuestra lista de lugares seguros
			trustManagers = tmf.getTrustManagers();

			// =============== CONEXION =================
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(keyManagers, trustManagers, null);

			SSLServerSocketFactory ssf = sc.getServerSocketFactory();
			ServerSocket serverSocket = ssf.createServerSocket(port);

			Integer numPeticiones=0;
			LocalDateTime fechaHora = LocalDateTime.now();
			
			// Mantenemos conexión abierta del servidor
			
			Instant start = Instant.now();
			
			// CODE HERE        
			
			while (true) {
					Instant finish = Instant.now();
					long timeElapsed = Duration.between(start, finish).toHours();
					Long horasMax = 4L;

					if(horasMax.equals(timeElapsed)){
						start= Instant.now();
						numPeticiones=0;

					}
					
				if(numPeticiones<=3){
					System.out.println("Esperando");
					Socket conexion = serverSocket.accept();
					System.out.println("Conexión establecida");

					// Definimos entrada y salida para conexión con cliente

					DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
					DataInputStream entrada = new DataInputStream(conexion.getInputStream());

					// Incrementamos el número de peticiones
					
					numPeticiones+=1;
					
					// Recibir
					String input = entrada.readUTF();
					String[] paramtros = input.split(";");
					String[] muebles = paramtros[0].split(" ");
					String pedido = paramtros[0];
					String Camas = muebles[0];
					String Mesas = muebles[1];
					String Sillas = muebles[2];
					String Sillones = muebles[3];
					Integer id = Integer.parseInt(paramtros[1]);
					String pedidoFirmado = paramtros[2];
					
					// Comprobamos la firma en cuestión

					Boolean resultadoComprobacion = comprobarFirma(pedido, pedidoFirmado, id);

					// Convertimos la comprobación a un String para poder tratarlo correctamente y enviamos la respuesta al cliente

					String c = "";
					if (resultadoComprobacion) {
						c = "true";
						salida.writeUTF("Petición OK");
						salida.flush();
					} else {
						c = "false";
						salida.writeUTF("Petición INCORRECTA");
						salida.flush();
					}

					// Añadimos el pedido a la base de datos y guardamos entrada en el log

					addEntry(id, c, pedido);
					guardarLog(pedido, resultadoComprobacion, id);

					// Cerramos conexión

					conexion.close();
				}else{
					//Si las peticiones en 4 horas son 
					Thread.sleep(5*1000);
					numPeticiones = 0;
				}
				
			}

		} catch (IOException ex) {
			System.err.println("[ERROR-SOCKET]: " + ex);
		} catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | UnrecoverableKeyException
				| KeyManagementException ex) {
			System.err.println("[ERROR]: " + ex);
		}

	}

}
