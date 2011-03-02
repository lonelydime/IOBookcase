package com.lonelydime.IOBookcase;

import java.io.File;
//import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;
import org.anjocaido.groupmanager.GroupManager;

public class IOBookcase extends JavaPlugin{
	private final BioBListenerEvent blockListener = new BioBListenerEvent(this);
	
	private final Logger log = Logger.getLogger("Minecraft");
	public static PermissionHandler Permissions = null;
	public static GroupManager gm = null;
	public static String nospawnblocks; 

	public void onDisable() {
		log.info("[IOBookcase] Disabled");	
	}

	public void onEnable() {
		
		if (!new File(getDataFolder().toString()).exists() ) {
        	new File(getDataFolder().toString()).mkdir();
        }
		
		//nospawnblocks = getConfiguration().getString("no-spawn-blocks", "17,18");
		
        //Create the pluginmanage pm.
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.BLOCK_RIGHTCLICKED, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        pm.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
        //pm.registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Normal, this);
        
        //Get the infomation from the plugin.yml file.
        PluginDescriptionFile pdfFile = this.getDescription();
        
        try {
        	this.connect();
        }
        catch (Exception e) {
        	System.out.println("Database: "+e);
        }
        
        //Print that the plugin has been enabled!
        log.info("[IOBookcase] version " + pdfFile.getVersion() + " by lonelydime is enabled!");
        setupPermissions();
	}
	
	public void setupPermissions() {
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
		Plugin p = this.getServer().getPluginManager().getPlugin("GroupManager");
		
		if(Permissions == null) {
		    if(test != null) {
		    	Permissions = ((Permissions)test).getHandler();
		    }
		}
		
		if (p != null) {
            if (!p.isEnabled()) {
                this.getServer().getPluginManager().enablePlugin(p);
            }
            gm = (GroupManager) p;
        } 
	}
	
	public void connect() throws Exception {

		Class.forName("org.sqlite.JDBC");
		
		Connection connection = DriverManager.getConnection("jdbc:sqlite:"+getDataFolder().toString()+File.separator+"bookcase.db");
		Statement statement = connection.createStatement();
		
		statement.execute("CREATE TABLE IF NOT EXISTS bookshelf (`locx` REAL,"
				+"`locy` REAL, `locz` REAL, `line1` varchar(32), `line2` varchar(32), `line3` varchar(32), `line4` varchar(32), `line5` varchar(32) );");
		
		ResultSet rs = statement.executeQuery("pragma table_info (bookshelf)");
		boolean requiresupdate = true;
		
		while (rs.next()) {
			if (rs.getString("name").matches("line6") ) {
				requiresupdate = false;
			}
        }
		
		if (requiresupdate) {
			log.info("[IOBookcase] - Updating table to allow 10 lines");
			statement.execute("ALTER TABLE bookshelf ADD COLUMN `line6` varchar(32)");
			statement.execute("ALTER TABLE bookshelf ADD COLUMN `line7` varchar(32)");
			statement.execute("ALTER TABLE bookshelf ADD COLUMN `line8` varchar(32)");
			statement.execute("ALTER TABLE bookshelf ADD COLUMN `line9` varchar(32)");
			statement.execute("ALTER TABLE bookshelf ADD COLUMN `line10` varchar(32)");
			log.info("[IOBookcase] - Table updated");
		}
		
		statement.close();
		connection.close();
	}
	
	public void writesql(String text, int linenum, int x, int y, int z) throws Exception {
		boolean foundentry = false;
		String linename = "line"+linenum;
		Class.forName("org.sqlite.JDBC");
		
		Connection connection = DriverManager.getConnection("jdbc:sqlite:"+getDataFolder().toString()+File.separator+"bookcase.db");		
		Statement statement = connection.createStatement();
		ResultSet rs = statement.executeQuery("SELECT `line1` FROM `bookshelf` WHERE `locx` = "+x+" AND `locy` = "+y+" AND `locz` = "+z+";");
		
		while (rs.next()) {
			foundentry = true;
        }
		
		//System.out.println("INSERT INTO bookshelf (`line1`,`locx`,`locy`,`locz`) VALUES ('"+text+"', "+x+", "+y+", "+z+");");
		//System.out.println(foundentry);
		
		text = text.replace("'", "''");
		text = text.replace(";", " ");
		
		if (foundentry)
			statement.executeUpdate("UPDATE bookshelf SET `"+linename+"`='"+text+"' WHERE `locx`="+x+" AND `locy`="+y+" AND `locz`="+z);
		else
			statement.executeUpdate("INSERT INTO bookshelf (`"+linename+"`,`locx`,`locy`,`locz`) VALUES ('"+text+"', "+x+", "+y+", "+z+")");
		
		rs.close();
		statement.close();
		connection.close();
		//System.out.println("write closed");
	}
	
	public String[] readcase(int x, int y, int z) throws Exception {
		
		String[] sendback = {"This bookcase is empty.", null, null, null, null, null, null, null, null, null};
		
		Class.forName("org.sqlite.JDBC");
		
		Connection connection = DriverManager.getConnection("jdbc:sqlite:"+getDataFolder().toString()+File.separator+"bookcase.db");		
		Statement statement = connection.createStatement();
		ResultSet rs = statement.executeQuery("SELECT `line1`, `line2`, `line3`, `line4`, `line5`, `line6`, `line7`, `line8`, `line9`, `line10` FROM `bookshelf` WHERE `locx` = "+x+" AND `locy` = "+y+" AND `locz` = "+z+";");
		//ResultSet rs = statement.executeQuery("SELECT * FROM bookshelf");
        
        while (rs.next()) {
        	sendback[0] = rs.getString("line1");
        	sendback[1] = rs.getString("line2");
        	sendback[2] = rs.getString("line3");
        	sendback[3] = rs.getString("line4");
        	sendback[4] = rs.getString("line5");
        	sendback[5] = rs.getString("line6");
        	sendback[6] = rs.getString("line7");
        	sendback[7] = rs.getString("line8");
        	sendback[8] = rs.getString("line9");
        	sendback[9] = rs.getString("line10");
        }
        	
		
		//System.out.println(connection.getWarnings());
		rs.close();
		statement.close();
        connection.close();

		return sendback;
	}
	
	public boolean checkcase(int x, int y, int z) throws Exception {
		boolean check = false;
		
		Class.forName("org.sqlite.JDBC");
		
		Connection connection = DriverManager.getConnection("jdbc:sqlite:"+getDataFolder().toString()+File.separator+"bookcase.db");		
		Statement statement = connection.createStatement();
		ResultSet rs = statement.executeQuery("SELECT `line1` FROM `bookshelf` WHERE `locx` = "+x+" AND `locy` = "+y+" AND `locz` = "+z+";");
		//ResultSet rs = statement.executeQuery("SELECT * FROM bookshelf");
        
        while (rs.next()) {
        	check = true;
        }
        	
		
		//System.out.println(connection.getWarnings());
		rs.close();
		statement.close();
        connection.close();

		return check;
	}
	
	public void deletecase(int x, int y, int z) throws Exception {
		Class.forName("org.sqlite.JDBC");
		
		Connection connection = DriverManager.getConnection("jdbc:sqlite:"+getDataFolder().toString()+File.separator+"bookcase.db");		
		Statement statement = connection.createStatement();
		ResultSet rs = statement.executeQuery("SELECT `line1` FROM `bookshelf` WHERE `locx` = "+x+" AND `locy` = "+y+" AND `locz` = "+z+";");
		
		
		//System.out.println("INSERT INTO bookshelf (`line1`,`locx`,`locy`,`locz`) VALUES ('"+text+"', "+x+", "+y+", "+z+");");
		//System.out.println(foundentry);

		statement.executeUpdate("DELETE FROM bookshelf WHERE `locx`="+x+" AND `locy`="+y+" AND `locz`="+z);

		rs.close();
		statement.close();
		connection.close();
	}
}
