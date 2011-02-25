package com.lonelydime.IOBookcase;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRightClickEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;


public class BioBListenerEvent extends BlockListener{
	public static IOBookcase plugin;

	public BioBListenerEvent(IOBookcase instance) {
        plugin = instance;
    }
	
	public void onBlockRightClick(BlockRightClickEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		boolean canRead = true;
		
		if (IOBookcase.Permissions != null) {
			canRead = IOBookcase.Permissions.has(player, "iobookcase.canread");
		}
		if (block.getType().getId() == 47) {
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
	
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();
		Block bookcase = null;
		boolean signoncase = false;
		int linenum = 1;
		String[] splittext;
		String texttowrite;
		boolean canWrite = true;
		
		if (IOBookcase.Permissions != null) {
			canWrite = IOBookcase.Permissions.has(player, "iobookcase.canwrite");
		}
		
		if (canWrite) {
	
			if (event.getBlock().getFace(BlockFace.EAST).getTypeId() == 47) {
				bookcase = event.getBlock().getFace(BlockFace.EAST);
				signoncase = true;
			}
			else if (event.getBlock().getFace(BlockFace.WEST).getTypeId() == 47) {
				bookcase = event.getBlock().getFace(BlockFace.WEST);
				signoncase = true;
			}
			else if (event.getBlock().getFace(BlockFace.SOUTH).getTypeId() == 47) {
				bookcase = event.getBlock().getFace(BlockFace.SOUTH);
				signoncase = true;
			}
			else if (event.getBlock().getFace(BlockFace.NORTH).getTypeId() == 47) {
				bookcase = event.getBlock().getFace(BlockFace.NORTH);
				signoncase = true;
			}
			
			if (signoncase) {

				if (event.getLine(0).toLowerCase().contains("[line") ) {
					splittext = event.getLine(0).split(" ");
					linenum = Integer.parseInt(splittext[1].replace("]", ""));
					texttowrite = event.getLine(1)+" "+event.getLine(2)+" "+event.getLine(3);
					if (linenum < 6) {
						try {
							plugin.writesql(texttowrite, linenum, bookcase.getX(), bookcase.getY(), bookcase.getZ());
							player.sendMessage("Text written to line "+linenum);
						}
						catch (Exception e) {
							player.sendMessage("Failed to write: "+e);
						}
						event.getBlock().setTypeId(0);
						ItemStack currentitem = player.getItemInHand();
						
						currentitem.setTypeId(323);
						currentitem.setAmount(1);
						player.setItemInHand(currentitem);
					}
					else {
						player.sendMessage("Only lines 1-5 allowed.");
					}

				}
			}
				
		}
	}
	
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock().getTypeId() == 47) {
			boolean checkcase = false;
			try {
				checkcase = plugin.checkcase(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());
			}
			catch (Exception e) {
				System.out.println("check fail: "+e);
			}
			
			if (checkcase)  {
				try {
					plugin.deletecase(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());
					event.getPlayer().sendMessage("Bookcase unregistered.");
				}
				catch (Exception e) {
					System.out.println("delete fail: "+e);
				}
			}
		}
	}

}