package com.lonelydime.IOBookcase;

//import net.minecraft.server.EntityPlayer;
//import net.minecraft.server.TileEntitySign;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRightClickEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
//import org.bukkit.craftbukkit.entity.CraftPlayer;

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
	
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();
		Block bookcase = null;
		boolean signoncase = false;
		int linenum = 1;
		char linecolor = 'f';
		String[] splittext;
		String texttowrite;
		boolean canWrite = true;
		
		if (IOBookcase.Permissions != null) {
			canWrite = IOBookcase.Permissions.has(player, "iobookcase.canwrite");
		}
		else if (IOBookcase.gm != null) {
			canWrite = IOBookcase.gm.getWorldsHolder().getWorldPermissions(player).has(player,"iobookcase.canwrite");
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

				if (event.getLine(0).toLowerCase().contains("@line") ) {
					splittext = event.getLine(0).split(" ");
					if (splittext.length > 1) {
						linenum = Integer.parseInt(splittext[1]);
						if (splittext.length > 2) {
							splittext[2] = splittext[2].toLowerCase();
							
							if (splittext[2].equals("black"))
								linecolor = '0';
							else if (splittext[2].equals("navy"))
								linecolor = '1';
							else if (splittext[2].equals("green"))
								linecolor = '2';
							else if (splittext[2].equals("blue"))
								linecolor = '3';
							else if (splittext[2].equals("red"))
								linecolor = '4';
							else if (splittext[2].equals("purple"))
								linecolor = '5';
							else if (splittext[2].equals("gold"))
								linecolor = '6';
							else if (splittext[2].equals("gray"))
								linecolor = '8';
							else if (splittext[2].equals("rose"))
								linecolor = 'c';
							else if (splittext[2].equals("yellow"))
								linecolor = 'e';
							else if (splittext[2].equals("white"))
								linecolor = 'f';
							else
								linecolor = splittext[2].charAt(0);
	
							if (!((linecolor >= '0' && linecolor <= '9') || (linecolor >= 'a' && linecolor <='f'))) {
								linecolor = 'f';
								player.sendMessage(ChatColor.RED+"You selected a none valid color: Defaulting to white");
							}
						}
						
						texttowrite = "¤"+linecolor+event.getLine(1)+event.getLine(2)+event.getLine(3);
						if (linenum < 11) {
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
							player.sendMessage("Only lines 1-10 allowed.");
						}
					}
					else {
						player.sendMessage("The format is @line #");
					}

				}
				
				//import from text file
				if (event.getLine(0).toLowerCase().contains("@import") ) {
					String sentCaseName = event.getLine(1);
					File importFile = new File(plugin.getDataFolder()+"/import.txt");
					String line;
					int indexoflinenum = 0;
					int indexnum = 0;
					String importlinecolor = "";
					String fileCaseName = "";
					boolean foundCase = false;
					boolean insideline = false;
					boolean insidetag = false;
					String[] casetext = {null, null, null, null, null, null, null, null, null, null};
					
					try {
						BufferedReader br = new BufferedReader(new FileReader(importFile));
						
						
						while((line = br.readLine()) != null) {
							//opening <case> tag
							if (line.contains("<case") && !insidetag) {
								//player.sendMessage("Looking for <case>");
								indexoflinenum = line.indexOf("name=\"");
								indexoflinenum = indexoflinenum + 6;
								
								if (indexoflinenum > -1) {
									fileCaseName = line.substring(indexoflinenum, line.indexOf("\"", indexoflinenum));
									//player.sendMessage("casename: "+fileCaseName);
									//the case they entered on the sign matches the one we have in the file
									if (sentCaseName.matches(fileCaseName)) {
										foundCase = true;
										player.sendMessage("Found "+fileCaseName);
									}
									//not this cases' name, move on
								}
								else { 
									//if the case does not have a name attribute in the import file
									player.sendMessage("Could not find the case's name, make sure your format is correct");
								}
								
								insidetag = true;
							} //end if <case
							
							//set the insidetag flag to false if we've reached the closing case tag
							else if (insidetag && line.contains("</case>")) {
								//player.sendMessage("Looking for </case>");
								insidetag = false;
								foundCase = false;
								fileCaseName="";
							}
							
							//read the <line> tags since we found the case
							else if (insidetag && foundCase && !insideline) {
								//player.sendMessage("Looking for <line>");
								if (line.contains("<line")) {
									//finds the line num - mandatory
									indexoflinenum = line.indexOf("num=\"");
									indexoflinenum = indexoflinenum + 5;
									if (indexoflinenum > -1) {
										indexnum = Integer.parseInt(line.substring(indexoflinenum, line.indexOf("\"", indexoflinenum)) );
										indexnum = indexnum - 1; //convert line number to java array index
										//player.sendMessage("Reading line "+(indexnum+1));
										insideline = true;
									}
									else //if the case does not have a name attribute in the import file
										player.sendMessage("Could not find the line's number, make sure your format is correct");
									
									//finds the line color - optional
									indexoflinenum = line.indexOf("color=\"");
									indexoflinenum = indexoflinenum + 7;
									if (indexoflinenum > -1) {
										importlinecolor = line.substring(indexoflinenum, line.indexOf("\"", indexoflinenum));
										
										if (importlinecolor.equals("black"))
											linecolor = '0';
										else if (importlinecolor.equals("navy"))
											linecolor = '1';
										else if (importlinecolor.equals("green"))
											linecolor = '2';
										else if (importlinecolor.equals("blue"))
											linecolor = '3';
										else if (importlinecolor.equals("red"))
											linecolor = '4';
										else if (importlinecolor.equals("purple"))
											linecolor = '5';
										else if (importlinecolor.equals("gold"))
											linecolor = '6';
										else if (importlinecolor.equals("lightgray"))
											linecolor = '7';
										else if (importlinecolor.equals("gray"))
											linecolor = '8';
										else if (importlinecolor.equals("darkpurple"))
											linecolor = '9';
										else if (importlinecolor.equals("lightgreen"))
											linecolor = 'a';
										else if (importlinecolor.equals("lightblue"))
											linecolor = 'b';
										else if (importlinecolor.equals("rose"))
											linecolor = 'c';
										else if (importlinecolor.equals("lightpurple"))
											linecolor = 'd';
										else if (importlinecolor.equals("yellow"))
											linecolor = 'e';
										else if (importlinecolor.equals("white"))
											linecolor = 'f';
										else
											linecolor = 'f';
									}
									
									else
										linecolor = 'f';
								}
							}
							
							else if (insideline && !line.contains("</line>") && foundCase && !line.contains("</case>")) {
								//player.sendMessage("Looking for <line> contents: "+fileCaseName);
								if (indexnum > -1 && indexnum < 10) {
									casetext[indexnum] = "¤"+linecolor+line.trim();
									//player.sendMessage("Line "+(indexnum+1)+": "+line.trim());
								}
								else {
									player.sendMessage("The line number "+indexnum+" is not between 1 and 10."); 
								}
							}
							
							else if (insideline && line.contains("</line>") && foundCase) {
								//player.sendMessage("Looking for </line>");
								insideline = false;
							}

							//we are inside a case tag that was not matched, do nothing.
							else {
								//player.sendMessage("No action required");
							}

						} //end file while loop
						int i = 0;
						
						if (casetext[0] == null) {
							player.sendMessage("The case with name "+sentCaseName+" was not found.");
						}
						else {
							
							try {
								plugin.deletecase(bookcase.getX(), bookcase.getY(), bookcase.getZ());
							}
							catch(Exception e) {
								System.out.println("Import Delete Error: "+e); 
								player.sendMessage("The old bookcase could not be deleted.");
							}
							
							while (casetext[i] != null) {
								//System.out.println(i+":"+casetext[i]);
								try {
									plugin.writesql(casetext[i], (i+1), bookcase.getX(), bookcase.getY(), bookcase.getZ());
								}
								catch(Exception e) {
									System.out.println("Import Write Error: "+e); 
									player.sendMessage("The database could not be written to.");
								}
								i++;
							}
						
							player.sendMessage(i+" lines written to the case "+bookcase.getX()+","+bookcase.getY()+","+bookcase.getZ());
						}
						
						event.getBlock().setTypeId(0);
						ItemStack currentitem = player.getItemInHand();
						
						currentitem.setTypeId(323);
						currentitem.setAmount(1);
						player.setItemInHand(currentitem);
						
					}
					catch(IOException e) {
						event.getPlayer().sendMessage(ChatColor.RED+"An error occured reading the import file.");
						event.getPlayer().sendMessage(ChatColor.RED+"Please check your server console for more details.");
						System.out.println("Error reading import file: "+e);
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