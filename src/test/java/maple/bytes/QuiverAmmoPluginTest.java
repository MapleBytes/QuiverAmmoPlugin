package maple.bytes;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class QuiverAmmoPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(QuiverAmmoPlugin.class);
		RuneLite.main(args);
	}
}