package org.coreocto.dev.hf.serverlib.crypto;

import org.coreocto.dev.hf.commonlib.crypto.BlockCipherFactory;
import org.coreocto.dev.hf.commonlib.crypto.IByteCipher;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AesCbcPkcs5BcImpl implements IByteCipher {
    private static final String CIPHER_AES = "AES";
    private static final String CIPHER_TRANSFORM = "AES/CBC/PKCS5Padding";

    private byte[] key = null;
    private byte[] iv = null;

    public AesCbcPkcs5BcImpl(byte[] key, byte[] iv) {
        this.key = key;
        this.iv = iv;
    }

    @Override
    public byte[] encrypt(byte[] data) throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        Cipher encryptCipher = BlockCipherFactory.getCipher(CIPHER_AES, CIPHER_TRANSFORM, Cipher.ENCRYPT_MODE, key, iv);
        return encryptCipher.doFinal(data);
    }

    @Override
    public byte[] decrypt(byte[] data) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException {
        Cipher decryptCipher = BlockCipherFactory.getCipher(CIPHER_AES, CIPHER_TRANSFORM, Cipher.DECRYPT_MODE, key, iv);
        return decryptCipher.doFinal(data);
    }
}

