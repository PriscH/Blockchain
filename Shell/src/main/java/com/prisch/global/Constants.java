package com.prisch.global;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants {

    public static final Path PUBLIC_KEY_PATH = Paths.get("public.key");
    public static final Path PRIVATE_KEY_PATH = Paths.get("private.key");

    public static final int ADDRESS_LENGTH = 12;
}
