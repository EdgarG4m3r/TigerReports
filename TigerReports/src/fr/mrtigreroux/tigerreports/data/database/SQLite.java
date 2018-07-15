package fr.mrtigreroux.tigerreports.data.database;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import fr.mrtigreroux.tigerreports.TigerReports;
import fr.mrtigreroux.tigerreports.utils.ConfigUtils;

/**
 * @author MrTigreroux
 */

public class SQLite extends Database {
	
	public SQLite() {}
	
	@Override
	public void openConnection() {
		File dataFolder = new File(TigerReports.getInstance().getDataFolder(), "tigerreports.db");
		if(!dataFolder.exists()) {
			try {
				dataFolder.createNewFile();
			} catch (IOException ex) {
				logError(ConfigUtils.getInfoMessage("Failed creation of tigerreports.db file.", "La creation du fichier tigerreports.db a echoue."), ex);
			}
		}
		
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"+dataFolder);
		} catch (ClassNotFoundException missing) {
			logError(ConfigUtils.getInfoMessage("SQLite is missing.", "SQLite n'est pas installe."), null);
		} catch (SQLException ex) {
			logError(ConfigUtils.getInfoMessage("An error has occurred during the connection to the SQLite database:", "Une erreur s'est produite lors de la connexion a la base de donnees SQLite:"), ex);
		}
		return;
    }

	@Override
	public void initialize() {
		Bukkit.getScheduler().runTaskAsynchronously(TigerReports.getInstance(), new Runnable() {
			
			@Override
			public void run() {
				String reportColumns = "('report_id' INTEGER PRIMARY KEY, 'status' varchar(50) NOT NULL DEFAULT 'Waiting', 'appreciation' varchar(10), 'date' varchar(20), 'reported_uuid' char(36), 'reporter_uuid' char(36), 'reason' varchar(150), 'reported_ip' varchar(22), 'reported_location' varchar(50), 'reported_messages' text, 'reported_gamemode' char(10), 'reported_on_ground' char(1), 'reported_sneak' varchar(1), 'reported_sprint' varchar(1), 'reported_health' varchar(10), 'reported_food' varchar(10), 'reported_effects' text, 'reporter_ip' varchar(22) NOT NULL, 'reporter_location' varchar(50) NOT NULL, 'reporter_messages' text);";
				update("CREATE TABLE IF NOT EXISTS tigerreports_users ('uuid' char(36) NOT NULL, 'name' varchar(20), 'cooldown' varchar(20), 'immunity' varchar(20), 'notifications' text, 'true_appreciations' int(5) DEFAULT '0', 'uncertain_appreciations' int(5) DEFAULT '0', 'false_appreciations' int(5) DEFAULT '0', 'reports' int(5) DEFAULT '0', 'reported_times' int(5) DEFAULT '0', 'processed_reports' int(5) DEFAULT '0');", null);
				update("CREATE TABLE IF NOT EXISTS tigerreports_reports "+reportColumns, null);
				update("CREATE TABLE IF NOT EXISTS tigerreports_comments ('report_id' INTEGER NOT NULL, 'comment_id' INTEGER PRIMARY KEY,'status' varchar(7), 'date' varchar(20), 'author' varchar(32), 'message' text);", null);			
				update("CREATE TABLE IF NOT EXISTS tigerreports_archived_reports "+reportColumns, null);
			}
			
		});
	}
	
	@Override
	public boolean isValid() throws SQLException {
		return !connection.isClosed();
	}
	
}
