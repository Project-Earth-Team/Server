package org.cloudburstmc.server.player;

import com.nukkitx.protocol.bedrock.BedrockServerSession;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.cloudburstmc.server.CloudServer;
import org.cloudburstmc.server.event.player.PlayerCreationEvent;
import org.cloudburstmc.server.network.BedrockInterface;
import org.cloudburstmc.server.scheduler.AsyncTask;
import org.cloudburstmc.server.utils.ClientChainData;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Extollite
 */
@Log4j2
public class PlayerLoginData {
    private final BedrockServerSession session;
    private final CloudServer server;
    private final BedrockInterface interfaz;

    private AsyncTask preLoginEventTask;
    private String username;
    private ClientChainData chainData;
    private boolean shouldLogin;

    public PlayerLoginData(BedrockServerSession session, CloudServer server, BedrockInterface interfaz) {
        this.session = session;
        this.server = server;
        this.interfaz = interfaz;
        shouldLogin = false;
    }

    public Player initializePlayer() {
        Player player;

        PlayerCreationEvent ev = new PlayerCreationEvent(interfaz, Player.class, Player.class, this.chainData.getClientId(), session.getAddress());
        this.server.getEventManager().fire(ev);
        Class<? extends Player> clazz = ev.getPlayerClass();

        try {
            Constructor<? extends Player> constructor = clazz.getConstructor(BedrockServerSession.class, ClientChainData.class);
            player = constructor.newInstance(session, chainData);
            this.server.addPlayer(session.getAddress(), player);
            session.addDisconnectHandler(interfaz.initDisconnectHandler(player));
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            log.throwing(Level.ERROR, e);
            return null;
        }

        player.processLogin();
        player.completeLoginSequence();

        return player;
    }

    public AsyncTask getPreLoginEventTask() {
        return preLoginEventTask;
    }

    public void setPreLoginEventTask(AsyncTask preLoginEventTask) {
        this.preLoginEventTask = preLoginEventTask;
    }

    public ClientChainData getChainData() {
        return chainData;
    }

    public void setChainData(ClientChainData chainData) {
        this.chainData = chainData;
    }

    public boolean isShouldLogin() {
        return shouldLogin;
    }

    public void setShouldLogin(boolean shouldLogin) {
        this.shouldLogin = shouldLogin;
    }

    public BedrockServerSession getSession() {
        return session;
    }

    public String getName() {
        return username;
    }

    public void setName(String username) {
        this.username = username;
    }
}
