package dev.rushcik.mauth.migration;

public interface MigrationHashVerifier {

  boolean checkPassword(String hash, String password);
}
