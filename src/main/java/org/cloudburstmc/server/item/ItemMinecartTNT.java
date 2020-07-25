package org.cloudburstmc.server.item;

import com.nukkitx.math.vector.Vector3f;
import org.cloudburstmc.server.block.Block;
import org.cloudburstmc.server.block.behavior.BlockBehaviorRail;
import org.cloudburstmc.server.entity.EntityTypes;
import org.cloudburstmc.server.entity.vehicle.TntMinecart;
import org.cloudburstmc.server.level.Level;
import org.cloudburstmc.server.level.Location;
import org.cloudburstmc.server.math.BlockFace;
import org.cloudburstmc.server.player.Player;
import org.cloudburstmc.server.registry.EntityRegistry;
import org.cloudburstmc.server.utils.Identifier;
import org.cloudburstmc.server.utils.Rail;

public class ItemMinecartTNT extends Item {

    public ItemMinecartTNT(Identifier id) {
        super(id);
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public boolean onActivate(Level level, Player player, Block block, Block target, BlockFace face, Vector3f clickPos) {
        if (Rail.isRailBlock(target.getState())) {
            Rail.Orientation type = ((BlockBehaviorRail) target).getOrientation();
            double adjacent = 0.0D;
            if (type.isAscending()) {
                adjacent = 0.5D;
            }
            Vector3f pos = target.getPosition().toFloat().add(0.5, 0.0625 + adjacent, 0.5);
            TntMinecart minecart = EntityRegistry.get().newEntity(EntityTypes.TNT_MINECART, Location.from(pos, level));

            if (player.isSurvival()) {
                Item item = player.getInventory().getItemInHand();
                item.decrementCount();
                player.getInventory().setItemInHand(item);
            }

            minecart.spawnToAll();
            return true;
        }
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}