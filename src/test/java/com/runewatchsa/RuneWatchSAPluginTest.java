package com.runewatchsa;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RuneWatchSAPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RuneWatchSAPlugin.class);
		RuneLite.main(args);
	}
}
