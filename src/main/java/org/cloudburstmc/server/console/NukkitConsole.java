package org.cloudburstmc.server.console;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import lombok.RequiredArgsConstructor;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.cloudburstmc.server.CloudServer;
import org.cloudburstmc.server.event.server.ServerCommandEvent;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class NukkitConsole extends SimpleTerminalConsole {
    private final CloudServer server;
    private final BlockingQueue<String> consoleQueue = new LinkedBlockingQueue<>();
    private final AtomicBoolean executingCommands = new AtomicBoolean(false);

    @Override
    protected boolean isRunning() {
        return server.isRunning();
    }

    @Override
    protected void runCommand(String command) {
        if (executingCommands.get()) {
            try (Timing ignored = Timings.serverCommandTimer.startTiming()) {
                ServerCommandEvent event = new ServerCommandEvent(server.getConsoleSender(), command);
                if (server.getPluginManager() != null) {
                    server.getEventManager().fire(event);
                }
                if (!event.isCancelled()) {
                    CloudServer.getInstance().getScheduler().scheduleTask(null, () -> server.dispatchCommand(event.getSender(), event.getCommand()));
                }
            }
        } else {
            consoleQueue.add(command);
        }
    }

    public String readLine() {
        try {
            return consoleQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void shutdown() {
        server.shutdown();
    }

    @Override
    protected LineReader buildReader(LineReaderBuilder builder) {
        builder.completer(new NukkitConsoleCompleter(server));
        builder.appName("Cloudburst");
        builder.option(LineReader.Option.HISTORY_BEEP, false);
        builder.option(LineReader.Option.HISTORY_IGNORE_DUPS, true);
        builder.option(LineReader.Option.HISTORY_IGNORE_SPACE, true);
        return super.buildReader(builder);
    }

    public boolean isExecutingCommands() {
        return executingCommands.get();
    }

    public void setExecutingCommands(boolean executingCommands) {
        if (this.executingCommands.compareAndSet(!executingCommands, executingCommands) && executingCommands) {
            consoleQueue.clear();
        }
    }
}
