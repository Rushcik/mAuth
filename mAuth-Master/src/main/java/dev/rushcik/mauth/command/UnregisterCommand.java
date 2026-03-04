package dev.rushcik.mauth.command;

import com.j256.ormlite.dao.Dao;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import java.sql.SQLException;
import java.util.Locale;
import net.elytrium.commons.kyori.serialization.Serializer;
import dev.rushcik.mauth.mAuth;
import dev.rushcik.mauth.Settings;
import dev.rushcik.mauth.event.AuthUnregisterEvent;
import dev.rushcik.mauth.handler.AuthSessionHandler;
import dev.rushcik.mauth.model.RegisteredPlayer;
import dev.rushcik.mauth.model.SQLRuntimeException;
import net.kyori.adventure.text.Component;

public class UnregisterCommand extends RatelimitedCommand {

  private final mAuth plugin;
  private final Dao<RegisteredPlayer, String> playerDao;

  private final String confirmKeyword;
  private final Component notPlayer;
  private final Component notRegistered;
  private final Component successful;
  private final Component errorOccurred;
  private final Component wrongPassword;
  private final Component usage;
  private final Component crackedCommand;

  public UnregisterCommand(mAuth plugin, Dao<RegisteredPlayer, String> playerDao) {
    this.plugin = plugin;
    this.playerDao = playerDao;

    Serializer serializer = mAuth.getSerializer();
    this.confirmKeyword = Settings.IMP.MAIN.CONFIRM_KEYWORD;
    this.notPlayer = serializer.deserialize(Settings.IMP.MAIN.STRINGS.NOT_PLAYER);
    this.notRegistered = serializer.deserialize(Settings.IMP.MAIN.STRINGS.NOT_REGISTERED);
    this.successful = serializer.deserialize(Settings.IMP.MAIN.STRINGS.UNREGISTER_SUCCESSFUL);
    this.errorOccurred = serializer.deserialize(Settings.IMP.MAIN.STRINGS.ERROR_OCCURRED);
    this.wrongPassword = serializer.deserialize(Settings.IMP.MAIN.STRINGS.WRONG_PASSWORD);
    this.usage = serializer.deserialize(Settings.IMP.MAIN.STRINGS.UNREGISTER_USAGE);
    this.crackedCommand = serializer.deserialize(Settings.IMP.MAIN.STRINGS.CRACKED_COMMAND);
  }

  @Override
  public void execute(CommandSource source, String[] args) {
    if (source instanceof Player) {
      if (args.length == 2) {
        if (this.confirmKeyword.equalsIgnoreCase(args[1])) {
          String username = ((Player) source).getUsername();
          String usernameLowercase = username.toLowerCase(Locale.ROOT);
          RegisteredPlayer player = AuthSessionHandler.fetchInfoLowercased(this.playerDao, usernameLowercase);
          if (player == null) {
            source.sendMessage(this.notRegistered);
          } else if (player.getHash().isEmpty()) {
            source.sendMessage(this.crackedCommand);
          } else if (AuthSessionHandler.checkPassword(args[0], player, this.playerDao)) {
            try {
              this.plugin.getServer().getEventManager().fireAndForget(new AuthUnregisterEvent(username));
              this.playerDao.deleteById(usernameLowercase);
              this.plugin.removePlayerFromCacheLowercased(usernameLowercase);
              ((Player) source).disconnect(this.successful);
            } catch (SQLException e) {
              source.sendMessage(this.errorOccurred);
              throw new SQLRuntimeException(e);
            }
          } else {
            source.sendMessage(this.wrongPassword);
          }

          return;
        }
      }

      source.sendMessage(this.usage);
    } else {
      source.sendMessage(this.notPlayer);
    }
  }

  @Override
  public boolean hasPermission(SimpleCommand.Invocation invocation) {
    return Settings.IMP.MAIN.COMMAND_PERMISSION_STATE.UNREGISTER
        .hasPermission(invocation.source(), "mAuth.commands.unregister");
  }
}
