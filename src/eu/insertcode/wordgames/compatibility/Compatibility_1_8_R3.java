package eu.insertcode.wordgames.compatibility;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

public class Compatibility_1_8_R3 implements Compatibility {
	
	@Override
	public void sendJson(Player p, String json) {
		IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a(json);
		
		
		PacketPlayOutChat packet = new PacketPlayOutChat(component);
		
		
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}
}
