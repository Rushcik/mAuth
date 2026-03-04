package dev.rushcik.mauth.command;

import com.j256.ormlite.dao.Dao;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Locale;
import net.elytrium.commons.kyori.serialization.Serializer;
import dev.rushcik.mauth.mAuth;
import dev.rushcik.mauth.Settings;
import dev.rushcik.mauth.model.RegisteredPlayer;
import dev.rushcik.mauth.model.SQLRuntimeException;
import net.kyori.adventure.text.Component;

public class ForceRegisterCommand extends RatelimitedCommand {

  private final mAuth plugin;
  private final Dao<RegisteredPlayer, String> playerDao;

  private final String successful;
  private final String notSuccessful;
  private final Component usage;
  private final Component takenNickname;
  private final Component incorrectNickname;

  public ForceRegisterCommand(mAuth plugin, Dao<RegisteredPlayer, String> playerDao) {
    this.plugin = plugin;
    this.playerDao = playerDao;

    this.successful = Settings.IMP.MAIN.STRINGS.FORCE_REGISTER_SUCCESSFUL;
    this.notSuccessful = Settings.IMP.MAIN.STRINGS.FORCE_REGISTER_NOT_SUCCESSFUL;
    this.usage = mAuth.getSerializer().deserialize(Settings.IMP.MAIN.STRINGS.FORCE_REGISTER_USAGE);
    this.takenNickname = mAuth.getSerializer().deserialize(Settings.IMP.MAIN.STRINGS.FORCE_REGISTER_TAKEN_NICKNAME);
    this.incorrectNickname = mAuth.getSerializer().deserialize(Settings.IMP.MAIN.STRINGS.FORCE_REGISTER_INCORRECT_NICKNAME);
  }

  @Override
  public void execute(CommandSource source, String[] args) {
    if (args.length == 2) {
      String nickname = args[0];
      String password = args[1];

      Serializer serializer = mAuth.getSerializer();
      try {
        if (!this.plugin.getNicknameValidationPattern().matcher(nickname).matches()) {
          source.sendMessage(this.incorrectNickname);
          return;
        }

        String lowercaseNickname = nickname.toLowerCase(Locale.ROOT);
        if (this.playerDao.idExists(lowercaseNickname)) {
          source.sendMessage(this.takenNickname);
          return;
        }

        RegisteredPlayer player = new RegisteredPlayer(nickname, "", "").setPassword(password);
        this.playerDao.create(player);

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
    return Settings.IMP.MAIN.COMMAND_PERMISSION_STATE.FORCE_REGISTER
        .hasPermission(invocation.source(), "mAuth.admin.forceregister");
  }
}
