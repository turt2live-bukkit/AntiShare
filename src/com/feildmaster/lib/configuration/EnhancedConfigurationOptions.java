package com.feildmaster.lib.configuration;

import org.bukkit.configuration.file.YamlConfigurationOptions;

/**
 * Enhanced Options, contains Header options
 * 
 * @author Feildmaster
 */
public class EnhancedConfigurationOptions extends YamlConfigurationOptions {
	public EnhancedConfigurationOptions(EnhancedConfiguration configuration){
		super(configuration);
	}

	@Override
	public EnhancedConfiguration configuration(){
		return (EnhancedConfiguration) super.configuration();
	}

	@Override
	public EnhancedConfigurationOptions copyDefaults(boolean value){
		super.copyDefaults(value);
		return this;
	}

	@Override
	public EnhancedConfigurationOptions header(String value){
		super.header(value);
		return this;
	}

	/**
	 * 
	 * 
	 * @param lines Comma Separated strings to build into the header
	 * @return
	 */
	public EnhancedConfigurationOptions header(String... lines){
		StringBuilder string = new StringBuilder();
		//String separator = System.getProperty("line.separator"); // This wont do anything...

		for(String s : lines){
			if(s == null){
				continue;
			}
			if(string.length() > 0){
				string.append("\n");
			}
			string.append(s);
		}

		header(string.length() == 0 ? null : string.toString());

		return this;
	}

	@Override
	public EnhancedConfigurationOptions copyHeader(boolean value){
		super.copyHeader(value);
		return this;
	}

	@Override
	public EnhancedConfigurationOptions pathSeparator(char value){
		super.pathSeparator(value);
		return this;
	}

	@Override
	public EnhancedConfigurationOptions indent(int value){
		super.indent(value);
		return this;
	}
}
