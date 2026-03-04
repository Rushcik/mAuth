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
import dev.rushcik.mauth.handler.AuthSessionHandler;
import dev.rushcik.mauth.model.RegisteredPlayer;
import dev.rushcik.mauth.model.SQLRuntimeException;
import net.kyori.adventure.text.Component;

public class PremiumCommand extends RatelimitedCommand {

  private final mAuth plugin;
  private final Dao<RegisteredPlayer, String> playerDao;

  private final String confirmKeyword;
  private final Component notRegistered;
  private final Component alreadyPremium;
  private final Component successful;
  private final Component errorOccurred;
  private final Component notPremium;
  private final Component wrongPassword;
  private final Component usage;
  private final Component notPlayer;

  public PremiumCommand(mAuth plugin, Dao<RegisteredPlayer, String> playerDao) {
    this.plugin = plugin;
    this.playerDao = playerDao;

    Serializer serializer = mAuth.getSerializer();
    this.confirmKeyword = Settings.IMP.MAIN.CONFIRM_KEYWORD;
    this.notRegistered = serializer.deserialize(Settings.IMP.MAIN.STRINGS.NOT_REGISTERED);
    this.alreadyPremium = serializer.deserialize(Settings.IMP.MAIN.STRINGS.ALREADY_PREMIUM);
    this.successful = serializer.deserialize(Settings.IMP.MAIN.STRINGS.PREMIUM_SUCCESSFUL);
    this.errorOccurred = serializer.deserialize(Settings.IMP.MAIN.STRINGS.ERROR_OCCURRED);
    this.notPremium = serializer.deserialize(Settings.IMP.MAIN.STRINGS.NOT_PREMIUM);
    this.wrongPassword = serializer.deserialize(Settings.IMP.MAIN.STRINGS.WRONG_PASSWORD);
    this.usage = serializer.deserialize(Settings.IMP.MAIN.STRINGS.PREMIUM_USAGE);
    this.notPlayer = serializer.deserialize(Settings.IMP.MAIN.STRINGS.NOT_PLAYER);
  }

  @Override
  public void execute(CommandSource source, String[] args) {
    if (source instanceof Player) {
      if (args.length == 2) {
        if (this.confirmKeyword.equalsIgnoreCase(args[1])) {
          String usernameLowercase = ((Player) source).getUsername().toLowerCase(Locale.ROOT);
          RegisteredPlayer player = AuthSessionHandler.fetchInfoLowercased(this.playerDao, usernameLowercase);
          if (player == null) {
            source.sendMessage(this.notRegistered);
          } else if (player.getHash().isEmpty()) {
            source.sendMessage(this.alreadyPremium);
          } else if (AuthSessionHandler.checkPassword(args[0], player, this.playerDao)) {
            if (this.plugin.isPremiumExternal(usernameLowercase).getState() == mAuth.PremiumState.PREMIUM_USERNAME) {
              try {
                player.setHash("");
                this.playerDao.update(player);
                this.plugin.removePlayerFromCacheLowercased(usernameLowercase);
                ((Player) source).disconnect(this.successful);
              } catch (SQLException e) {
                source.sendMessage(this.errorOccurred);
                throw new SQLRuntimeException(e);
              }
            } else {
              source.sendMessage(this.notPremium);
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
    return Settings.IMP.MAIN.COMMAND_PERMISSION_STATE.PREMIUM
        .hasPermission(invocation.source(), "mAuth.commands.premium");
  }
}
