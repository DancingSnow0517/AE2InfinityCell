package cn.dancingsnow.aeinfinitycell;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public final class ServerWorldAccess {

    private static WorldServer serverWorld;

    private ServerWorldAccess() {}

    public static void setServer(MinecraftServer server) {
        serverWorld = server == null ? null : server.worldServerForDimension(0);
    }

    public static World getServerWorld() {
        if (serverWorld != null) {
            return serverWorld;
        }
        MinecraftServer server = MinecraftServer.getServer();
        return server == null ? null : server.worldServerForDimension(0);
    }

    public static void clear() {
        serverWorld = null;
    }
}
