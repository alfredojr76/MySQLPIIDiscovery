import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import java.util.Set;


public class Discover {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String RegexPII = "";
		Properties prop = new Properties();
		Properties propRegex = new Properties();
		Properties propField = new Properties();
		

		try (InputStream input = new FileInputStream("config.properties")) {

			// load a properties file
			prop.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		try (InputStream input = new FileInputStream("fields.properties")) {

			// load a properties file
			propField.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		try (InputStream input = new FileInputStream("regex.properties")) {

			// load a properties file
			propRegex.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		FileWriter myWriter = null;
		try {
			myWriter = new FileWriter("Relatorio.txt");

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		Set<Object> keys = propRegex.keySet();
		for (Object k : keys) {
			String key = (String) k;
			System.out.println(" Buscando a REGEX:" + key + ": " + propRegex.getProperty(key));
			
		      try {
					myWriter.write("########\n Buscando a REGEX:" + key + ": " + propRegex.getProperty(key)+"\n");
					myWriter.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
			RegexPII = propRegex.getProperty(key);

			try {
				String myDriver = "com.mysql.cj.jdbc.Driver";
				Class.forName(myDriver);
				// SHOW DATABASES
				String myUrlDB = prop.getProperty("db.url");
				Connection connDB = DriverManager.getConnection(myUrlDB, prop.getProperty("db.user"),
						prop.getProperty("db.password"));
				String queryDB = "SHOW DATABASES";
				Statement stDB = connDB.createStatement();
				ResultSet rsDB = stDB.executeQuery(queryDB);
				while (rsDB.next()) {
					String dbNome = rsDB.getString(1);
					System.err.println("----------------------");
					System.out.format("Database:%s\n", dbNome);
					
				      try {
							myWriter.write("--------------\n");
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				      try {
							myWriter.write("Database: "+dbNome+"\n");
							myWriter.flush();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					
					
					queryDB = "use " + dbNome;
					Statement stDBNome = connDB.createStatement();
					ResultSet rsDBNome = stDBNome.executeQuery(queryDB);
					String queryTables = "SHOW TABLES";
					Statement stTables = connDB.createStatement();
					ResultSet rsTables = stTables.executeQuery(queryTables);
					while (rsTables.next()) {
						String tabela = rsTables.getString(1);
						
						try {
							myWriter.write("Tabela: " + tabela+"\n");
							myWriter.flush();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						String queryCampos = "DESCRIBE " + tabela;
						Statement stCampos = connDB.createStatement();
						ResultSet rsCampos = stCampos.executeQuery(queryCampos);
						while (rsCampos.next()) {
							String campos = rsCampos.getString(1);

							// Valida nome de campo
							Set<Object> pCampos = propField.keySet();
							for (Object kCampos : pCampos) {
								String camposValida = propField.getProperty((String) kCampos);
								if (campos.toLowerCase().contains(camposValida.toLowerCase())) {
									System.err.println("Encontrei um campo:" + campos+ " Minha busca:"+camposValida);
									try {
										myWriter.write("Encontrei um campo: " + campos+" Minha busca:"+camposValida+"\n");
										myWriter.flush();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
							}

							// System.out.format("==>%s\n", campos);
							String query = "select count(*) as total from " + dbNome + "." + tabela + " where " + campos
									+ " REGEXP " + RegexPII;
							// System.out.format("Comando:%s\n", query);
							try {
								Statement stDiscover = connDB.createStatement();
								ResultSet rsDiscover = stDiscover.executeQuery(query);
								while (rsDiscover.next()) {
									int total = rsDiscover.getInt(1);
									if (total > 0) {
										//System.out.format("Tabela:%s\n", tabela);
										//System.out.format("Descoberta:" + campos + ":%s\n", total);
										try {
											myWriter.write("Descoberta REGEX: RegEx:"+key + " Campo:"+campos+" : "+total+"\n");
											myWriter.flush();
										} catch (IOException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
									}
								}
							} catch (Exception e) {
							}

						}
					}

				}
				stDB.close();
			} catch (Exception e) {
				System.err.println("Got an exception! ");
				System.err.println(e.getMessage());
			}

		}
		System.err.println("Fim de análise");
	    try {
			myWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
