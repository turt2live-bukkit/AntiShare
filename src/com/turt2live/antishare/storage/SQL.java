/*******************************************************************************
 * Copyright (c) 2012 turt2live (Travis Ralston).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 * turt2live (Travis Ralston) - initial API and implementation
 ******************************************************************************/
package com.turt2live.antishare.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import com.turt2live.antishare.AntiShare;

/*
 * Thanks go to Example Depot for teaching me everything in this class.
 * http://www.exampledepot.com/egs/java.sql/pkg.html
 */
/**
 * SQL Manager
 * 
 * @author turt2live
 */
public class SQL {

	public static final String REGIONS_TABLE = "AS_Regions";
	public static final String INVENTORIES_TABLE = "AS_Inventories";

	private AntiShare plugin;
	private Connection connection;
	private boolean connected = false;
	private String database = "";
	private String sU, sP, sD, sH; // SQL connection details for reconnect

	/**
	 * Creates a new SQL Manager
	 */
	public SQL(){
		this.plugin = AntiShare.getInstance();
	}

	/**
	 * Attempts a connection
	 * 
	 * @param host the hostname
	 * @param username the username
	 * @param password the password
	 * @param database the database
	 * @return true if connected
	 */
	public boolean connect(String host, String username, String password, String database){
		sU = username;
		sP = password;
		sH = host;
		sD = database;
		try{
			String driverName = "org.gjt.mm.mysql.Driver";
			Class.forName(driverName);
			String url = "jdbc:mysql://" + host + "/" + database;
			connection = DriverManager.getConnection(url, username, password);
			if(connection != null){
				connected = !connection.isClosed();
			}else{
				connected = false;
			}
			this.database = database;
			return true;
		}catch(ClassNotFoundException e){
			plugin.getLogger().warning("You do not have a MySQL driver, please install one. AntiShare will use Flat-File for now");
		}catch(SQLException e){
			plugin.getLogger().warning("Cannot connect to SQL! Check your settings. AntiShare will use Flat-File for now");
		}
		return false;
	}

	/**
	 * Disconnects from the SQL server
	 */
	public void disconnect(){
		if(connection != null){
			try{
				if(!connection.isClosed()){
					connection.close();
				}
			}catch(SQLException e){
				AntiShare.getInstance().log("AntiShare encountered and error. Please report this to turt2live.", Level.SEVERE);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Checks for a connection
	 * 
	 * @return true if connected
	 */
	public boolean isConnected(){
		return connection != null && connected;
	}

	/**
	 * Gets the SQL connection
	 * 
	 * @return the connection
	 */
	public Connection getConnection(){
		return connection;
	}

	/**
	 * Reconnects to the SQL server
	 */
	public void reconnect(){
		connect(sH, sU, sP, sD);
	}

	/**
	 * Gets the SQL database in use
	 * 
	 * @return the database name
	 */
	public String getDatabase(){
		return database;
	}

	/**
	 * Executes setup on the SQL server
	 */
	public void setup(){
		if(!isConnected()){
			reconnect();
		}
		try{
			createQuery(connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + INVENTORIES_TABLE + "` (" +
					"  `id` int(11) NOT NULL AUTO_INCREMENT," +
					"  `type` varchar(25) NOT NULL, " +
					"  `name` varchar(50) NOT NULL," +
					"  `gamemode` varchar(25) NOT NULL," +
					"  `world` varchar(100) NOT NULL," +
					"  `slot` int(11) NOT NULL," +
					"  `itemID` int(11) NOT NULL," +
					"  `itemName` varchar(25) NOT NULL," +
					"  `itemDurability` int(11) NOT NULL," +
					"  `itemAmount` int(11) NOT NULL," +
					"  `itemData` int(11) NOT NULL," +
					"  `itemEnchant` text NOT NULL," +
					"  PRIMARY KEY (`id`)" +
					")"));
			createQuery(connection.prepareStatement("CREATE TABLE IF NOT EXISTS `" + REGIONS_TABLE + "` (" +
					"  `id` int(11) NOT NULL AUTO_INCREMENT," +
					"  `regionName` varchar(255) NOT NULL," +
					"  `mix` decimal(25,4) NOT NULL," +
					"  `miy` decimal(25,4) NOT NULL," +
					"  `miz` decimal(25,4) NOT NULL," +
					"  `max` decimal(25,4) NOT NULL," +
					"  `may` decimal(25,4) NOT NULL," +
					"  `maz` decimal(25,4) NOT NULL," +
					"  `creator` varchar(25) NOT NULL," +
					"  `gamemode` varchar(25) NOT NULL," +
					"  `showEnter` int(11) NOT NULL," +
					"  `showExit` int(11) NOT NULL," +
					"  `world` varchar(100) NOT NULL," +
					"  `uniqueID` varchar(100) NOT NULL," +
					"  `enterMessage` varchar(300) NOT NULL," +
					"  `exitMessage` varchar(300) NOT NULL," +
					"  PRIMARY KEY (`id`)" +
					")"));
		}catch(SQLException e){
			AntiShare.getInstance().log("AntiShare encountered and error. Please report this to turt2live.", Level.SEVERE);
			e.printStackTrace();
		}
	}

	/**
	 * Wipes a table
	 * 
	 * @param tablename the table name
	 */
	public void wipeTable(String tablename){
		if(!isConnected()){
			reconnect();
		}
		try{
			updateQuery(connection.prepareStatement("DELETE FROM " + tablename));
		}catch(SQLException e){
			AntiShare.getInstance().log("AntiShare encountered and error. Please report this to turt2live.", Level.SEVERE);
			e.printStackTrace();
		}
	}

	/**
	 * Executes a "create" query
	 * 
	 * @param statement the statement
	 */
	public void createQuery(PreparedStatement statement){
		updateQuery(statement);
	}

	/**
	 * Executes a "delete" query
	 * 
	 * @param statement the statement
	 * @return the number of rows affected
	 */
	public int deleteQuery(PreparedStatement statement){
		return updateQuery(statement);
	}

	/**
	 * Executes a "get" or "select" query
	 * 
	 * @param statement the statement
	 * @return the results
	 */
	public ResultSet getQuery(PreparedStatement statement){
		if(!isConnected()){
			reconnect();
		}
		try{
			return statement.executeQuery();
		}catch(SQLException e){
			AntiShare.getInstance().log("AntiShare encountered and error. Please report this to turt2live.", Level.SEVERE);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Executes an "insert" query
	 * 
	 * @param statement the statement
	 */
	public void insertQuery(PreparedStatement statement){
		if(!isConnected()){
			reconnect();
		}
		try{
			statement.executeUpdate();
		}catch(SQLException e){
			AntiShare.getInstance().log("AntiShare encountered and error. Please report this to turt2live.", Level.SEVERE);
			e.printStackTrace();
		}
	}

	/**
	 * Executes an "update" query
	 * 
	 * @param statement the statement
	 * @return the number of rows affected
	 */
	public int updateQuery(PreparedStatement statement){
		if(!isConnected()){
			reconnect();
		}
		try{
			return statement.executeUpdate();
		}catch(SQLException e){
			AntiShare.getInstance().log("AntiShare encountered and error. Please report this to turt2live.", Level.SEVERE);
			e.printStackTrace();
		}
		return 0;
	}
}
