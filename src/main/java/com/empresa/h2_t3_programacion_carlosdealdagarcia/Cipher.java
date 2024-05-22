package com.empresa.h2_t3_programacion_carlosdealdagarcia;

public class Cipher {
    private static final int KEY = 0xAB; // Clave fija para el cifrado

    public static String encrypt(String input) {
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] ^= KEY; // Aplicar XOR con la clave
        }
        return new String(chars);
    }
}
