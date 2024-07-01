package maple.bytes;

import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.Arrays;

@PluginDescriptor(
		name = "QuiverAmmo",
		description = "Shows the current ammo the player has equipped in their quiver (I would love C engineer to do a snarky voice comment when entering a raid without ammo)",
		tags = {"bolts", "javelin", "sol", "colosseum", "equipment", "quiver", "ammo"}
)
public class QuiverAmmoPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;



	//hard coded until varplayer is updated to name these items
	private static final int QUIVER_ITEM_ID          = 4142;
	private static final int QUIVER_ITEM_QUANTITY    = 4141;

	private QuiverAmmoOverlay counterBox;

	private boolean quiverInContainer;

	@Override
	protected void startUp() throws Exception
	{
		clientThread.invokeLater(this::checkAllContainers);
	}

	@Override
	protected void shutDown() throws Exception
	{
		removeInfobox();
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.EQUIPMENT.getId() || event.getContainerId() == InventoryID.INVENTORY.getId())
		{
			checkAllContainers();
		}
		updateQuiverState();
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		updateQuiverState();
	}


	//todo: fix stupid.
	private void checkAllContainers()
	{
		quiverInContainer = isQuiverInContainer(client.getItemContainer(InventoryID.EQUIPMENT)) ||
				isQuiverInContainer(client.getItemContainer(InventoryID.INVENTORY));
		updateQuiverState();
	}

	private boolean isQuiverInContainer(ItemContainer container)
	{
		//We check for "Dizana" instead of using a list of item ID's or "quiver" because "Dizana's max cape"
		//



		return container != null && Arrays.stream(container.getItems())
				.anyMatch(item -> itemManager.getItemComposition(item.getId()).getName().contains("Dizana"));
	}

	private void updateQuiverState()
	{
		if (quiverInContainer)
		{
			int quiverItemId = client.getVarpValue(QUIVER_ITEM_ID);
			int quiverItemQuantity = client.getVarpValue(QUIVER_ITEM_QUANTITY);

			if (quiverItemId == -1 || quiverItemQuantity == 0)
			{
				updateWidgetIconEmptyQuiver();
			}
			else
			{
				final ItemComposition comp = itemManager.getItemComposition(quiverItemId);
				final BufferedImage image = itemManager.getImage(quiverItemId, 5, false);
				updateInfobox(quiverItemId, quiverItemQuantity, comp.getName(), image);
			}
		}
		else
		{
			removeInfobox();
		}
	}

	private void updateWidgetIconEmptyQuiver()
	{
		System.out.println("Updating widget icon to show empty quiver.");

		//Alright you programmers, listen up
		final BufferedImage image = itemManager.getImage(687, 5, false);
		updateInfobox(0, 0, "Raids are more fun with ammo in your quiver", image);
	}

	private void updateInfobox(int itemId, int quantity, String name, BufferedImage image)
	{
		if (counterBox != null && counterBox.getItemID() == itemId)
		{
			counterBox.setCount(quantity);
			return;
		}

		removeInfobox();
		counterBox = new QuiverAmmoOverlay(this, itemId, quantity, name, image);
		infoBoxManager.addInfoBox(counterBox);
	}

	private void removeInfobox()
	{
		infoBoxManager.removeInfoBox(counterBox);
		counterBox = null;
	}
}
