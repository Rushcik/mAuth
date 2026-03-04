package dev.rushcik.mauth.backend.type;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import dev.rushcik.mauth.mAuth;
import dev.rushcik.mauth.backend.Endpoint;

public class UnknownEndpoint extends Endpoint {

  private String type;

  public UnknownEndpoint(mAuth plugin) {
    super(plugin);
  }

  public UnknownEndpoint(mAuth plugin, String type) {
    super(plugin);
    this.type = type;
  }

  @Override
  public void write(ByteArrayDataOutput output) {
    output.writeUTF(this.type);
    output.writeInt(-2);
  }

  @Override
  public void read(ByteArrayDataInput input) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeContents(ByteArrayDataOutput output) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void readContents(ByteArrayDataInput input) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "UnknownEndpoint{"
        + "type='" + this.type + '\''
        + '}';
  }
}
