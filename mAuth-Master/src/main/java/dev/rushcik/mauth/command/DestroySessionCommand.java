package dev.rushcik.mauth.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.elytrium.commons.kyori.serialization.Serializer;
import dev.rushcik.mauth.mAuth;
import dev.rushcik.mauth.Settings;
import net.kyori.adventure.text.Component;

public class DestroySessionCommand extends RatelimitedCommand {

  private final mAuth plugin;

  private final Component successful;
  private final Component notPlayer;

  public DestroySessionCommand(mAuth plugin) {
    this.plugin = plugin;

    Serializer serializer = mAuth.getSerializer();
    this.successful = serializer.deserialize(Settings.IMP.MAIN.STRINGS.DESTROY_SESSION_SUCCESSFUL);
    this.notPlayer = serializer.deserialize(Settings.IMP.MAIN.STRINGS.NOT_PLAYER);
  }

  @Override
  public void execute(CommandSource source, String[] args) {
    if (source instanceof Player) {
      this.plugin.removePlayerFromCache(((Player) source).getUsername());
      source.sendMessage(this.successful);
    } else {
      source.sendMessage(this.notPlayer);
    }
  }

  @Override
  public boolean hasPermission(SimpleCommand.Invocation invocation) {
    return Settings.IMP.MAIN.COMMAND_PERMISSION_STATE.DESTROY_SESSION
        .hasPermission(invocation.source(), "mAuth.commands.destroysession");
  }
}
