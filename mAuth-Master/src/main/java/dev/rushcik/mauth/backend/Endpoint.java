package dev.rushcik.mauth.backend;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import dev.rushcik.mauth.mAuth;
import dev.rushcik.mauth.Settings;

public abstract class Endpoint {

  protected final mAuth plugin;
  protected String type;
  protected String username;

  public Endpoint(mAuth plugin) {
    this.plugin = plugin;
  }

  public Endpoint(mAuth plugin, String type, String username) {
    this.plugin = plugin;
    this.type = type;
    this.username = username;
  }

  public void write(ByteArrayDataOutput output) {
    output.writeUTF(this.type);
    if (!this.type.equals("available_endpoints") && !Settings.IMP.MAIN.BACKEND_API.ENABLED_ENDPOINTS.contains(this.type)) {
      output.writeInt(-1);
      output.writeUTF(this.username);
      return;
    }

    output.writeInt(1);
    output.writeUTF(Settings.IMP.MAIN.BACKEND_API.TOKEN);
    output.writeUTF(this.username);
    this.writeContents(output);
  }

  public void read(ByteArrayDataInput input) {
    int version = input.readInt();
    if (version != 0) {
      throw new IllegalStateException("unsupported '" + this.type + "' endpoint version: " + version);
    }

    this.username = input.readUTF();
    this.readContents(input);
  }

  public abstract void writeContents(ByteArrayDataOutput output);

  public abstract void readContents(ByteArrayDataInput input);
}
