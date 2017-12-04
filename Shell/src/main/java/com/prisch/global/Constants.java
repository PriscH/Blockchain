package com.prisch.global;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants {

    public static final Path PUBLIC_KEY_PATH = Paths.get("public.key");
    public static final Path PRIVATE_KEY_PATH = Paths.get("private.key");

    public static final int HASH_LENGTH = 8;
    public static final int ADDRESS_LENGTH = HASH_LENGTH;

    public static final int COINBASE_REWARD = 1000;
    public static final int TRANSACTION_LIMIT = 3;

    public static final String COINBASE_ADDRESS = "00000000";
}
