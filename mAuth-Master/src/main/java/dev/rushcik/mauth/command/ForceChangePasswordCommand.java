package dev.rushcik.mauth.command;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import net.elytrium.commons.kyori.serialization.Serializer;
import net.elytrium.commons.velocity.commands.SuggestUtils;
import dev.rushcik.mauth.mAuth;
import dev.rushcik.mauth.Settings;
import dev.rushcik.mauth.event.ChangePasswordEvent;
import dev.rushcik.mauth.handler.AuthSessionHandler;
import dev.rushcik.mauth.model.RegisteredPlayer;
import dev.rushcik.mauth.model.SQLRuntimeException;
import net.kyori.adventure.text.Component;

public class ForceChangePasswordCommand extends RatelimitedCommand {

  private final mAuth plugin;
  private final ProxyServer server;
  private final Dao<RegisteredPlayer, String> playerDao;

  private final String message;
  private final String successful;
  private final String notSuccessful;
  private final String notRegistered;
  private final Component usage;

  public ForceChangePasswordCommand(mAuth plugin, ProxyServer server, Dao<RegisteredPlayer, String> playerDao) {
    this.plugin = plugin;
    this.server = server;
    this.playerDao = playerDao;

    this.message = Settings.IMP.MAIN.STRINGS.FORCE_CHANGE_PASSWORD_MESSAGE;
    this.successful = Settings.IMP.MAIN.STRINGS.FORCE_CHANGE_PASSWORD_SUCCESSFUL;
    this.notSuccessful = Settings.IMP.MAIN.STRINGS.FORCE_CHANGE_PASSWORD_NOT_SUCCESSFUL;
    this.notRegistered = Settings.IMP.MAIN.STRINGS.FORCE_CHANGE_PASSWORD_NOT_REGISTERED;
    this.usage = mAuth.getSerializer().deserialize(Settings.IMP.MAIN.STRINGS.FORCE_CHANGE_PASSWORD_USAGE);
  }

  @Override
  public List<String> suggest(SimpleCommand.Invocation invocation) {
    return SuggestUtils.suggestPlayers(this.server, invocation.arguments(), 0);
  }

  @Override
  public void execute(CommandSource source, String[] args) {
    if (args.length == 2) {
      String nickname = args[0];
      String nicknameLowercased = args[0].toLowerCase(Locale.ROOT);
      String newPassword = args[1];

      Serializer serializer = mAuth.getSerializer();
      try {
        RegisteredPlayer registeredPlayer = AuthSessionHandler.fetchInfoLowercased(this.playerDao, nicknameLowercased);

        if (registeredPlayer == null) {
          source.sendMessage(serializer.deserialize(MessageFormat.format(this.notRegistered, nickname)));
          return;
        }

        final String oldHash = registeredPlayer.getHash();
        final String newHash = RegisteredPlayer.genHash(newPassword);

        UpdateBuilder<RegisteredPlayer, String> updateBuilder = this.playerDao.updateBuilder();
        updateBuilder.where().eq(RegisteredPlayer.LOWERCASE_NICKNAME_FIELD, nicknameLowercased);
        updateBuilder.updateColumnValue(RegisteredPlayer.HASH_FIELD, newHash);
        updateBuilder.update();

        this.plugin.removePlayerFromCacheLowercased(nicknameLowercased);
        this.server.getPlayer(nickname)
            .ifPresent(player -> player.sendMessage(serializer.deserialize(MessageFormat.format(this.message, newPassword))));

        this.plugin.getServer().getEventManager().fireAndForget(new ChangePasswordEvent(registeredPlayer, null, oldHash, newPassword, newHash));

        source.sendMessage(serializer.deserialize(MessageFormat.format(this.successful, nickname)));
      } catch (SQLException e) {
        source.sendMessage(serializer.deserialize(MessageFormat.format(this.notSuccessful, nickname)));
        throw new SQLRuntimeException(e);
      }
    } else {
      source.sendMessage(this.usage);
    }
  }

  @Override
  public boolean hasPermission(SimpleCommand.Invocation invocation) {
    return Settings.IMP.MAIN.COMMAND_PERMISSION_STATE.FORCE_CHANGE_PASSWORD
        .hasPermission(invocation.source(), "mAuth.admin.forcechangepassword");
  }
}
