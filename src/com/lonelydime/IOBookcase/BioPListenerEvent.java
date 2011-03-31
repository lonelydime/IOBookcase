package com.lonelydime.IOBookcase;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

public class BioPListenerEvent extends PlayerListener{
	public static IOBookcase plugin;

	public BioPListenerEvent(IOBookcase instance) {
        plugin = instance;
    }
	
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		boolean canRead = true;
		
		if (IOBookcase.Permissions != null) {
			canRead = IOBookcase.Permissions.has(player, "iobookcase.canread");
		}
		else if (IOBookcase.gm != null) {
			canRead = IOBookcase.gm.getWorldsHolder().getWorldPermissions(player).has(player,"iobookcase.canread");
		}
		
		if (block.getType().getId() == 47) {
			/*if (player.getItemInHand().getTypeId() == 280) {
				EntityPlayer eh = ((CraftPlayer) player).getHandle();
				TileEntitySign tsn = new TileEntitySign();
				System.out.println(tsn.toString());

				eh.a(tsn);
			}*/
			if ((!player.getItemInHand().getType().isBlock() || player.getItemInHand().getTypeId() == 0) 
					&& player.getItemInHand().getTypeId()!= 323) {
				int locx = block.getX();
				int locy = block.getY();
				int locz = block.getZ();
				
				if (canRead) {
					String[] casetext;
					try {
						casetext = plugin.readcase(locx, locy, locz);
						int i = 0;
						while (casetext[i] != null) {
							player.sendMessage(casetext[i]);
							i++;
						}
					}
					catch (Exception e) {
						System.out.println("read: "+e);
					}
				}
			}
		}
	}
}
