package com.prisch.global;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants {

    public static final Path PUBLIC_KEY_PATH = Paths.get("public.key");
    public static final Path PRIVATE_KEY_PATH = Paths.get("private.key");

    public static final int HASH_LENGTH = 8;
    public static final int ADDRESS_LENGTH = HASH_LENGTH;

    public static final String LOCK_HEIGHT_PROP = "LOCK_HEIGHT";
}
