package triangle.triangleapp.helpers;

import android.security.KeyPairGeneratorSpec;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Calendar;

import javax.security.auth.x500.X500Principal;

import triangle.triangleapp.TriangleApplication;

/**
 * Created by marco on 15-6-2017.
 */

public class IntegrityHelper {
    private static final String TAG = "IntegrityHelper";

    private static final String KEY_ALGORITHM = ConfigHelper.getInstance().get(ConfigHelper.KEY_KEY_ALGORITHM);
    private static final String SIGN_ALGORITHM = ConfigHelper.getInstance().get(ConfigHelper.KEY_SIGN_ALGORITHM);
    private static final int KEY_SIZE = ConfigHelper.getInstance().getInt(ConfigHelper.KEY_KEY_SIZE);
    private static final String KEY_ALIAS = ConfigHelper.getInstance().get(ConfigHelper.KEY_KEY_ALIAS);
    private static final String KEY_STORE = ConfigHelper.getInstance().get(ConfigHelper.KEY_KEY_STORE);
    private static final int CERT_END_DATE_INCREMENT = 30;

    /**
     * Generates an keypair using the constant KEY_ALGORITHM and KEY_SIZE
     *
     * @return Generated KeyPair using the algorithm
     */
    @Nullable
    public static KeyPair getKeyPair() {
        KeyPair keyPair = null;
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(KEY_STORE);
            keyStore.load(null);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            Log.e(TAG, "Error getting key store instance!", e);
            return null;
        }

        try {
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                KeyPairGenerator generator;
                try {
                    Log.i(TAG, "Generating key.");

                    Calendar start = Calendar.getInstance();
                    Calendar end = Calendar.getInstance();
                    end.add(Calendar.YEAR, CERT_END_DATE_INCREMENT);

                    generator = KeyPairGenerator.getInstance(KEY_ALGORITHM, KEY_STORE);
                    KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(TriangleApplication.getAppContext())
                            .setAlias(KEY_ALIAS)
                            .setSubject(new X500Principal("CN=" + KEY_ALIAS))
                            .setSerialNumber(BigInteger.TEN)
                            .setStartDate(start.getTime())
                            .setEndDate(end.getTime())
                            .setKeySize(KEY_SIZE)
                            .build();
                    generator.initialize(spec);
                    keyPair = generator.generateKeyPair();

                    Log.i(TAG, "Key generation completed");
                } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
                    Log.e(TAG, "Error during key generation", e);
                }
            } else {
                Log.i(TAG, "Key already in store.");
                KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
                PrivateKey privateKey = keyEntry.getPrivateKey();
                PublicKey publicKey = keyEntry.getCertificate().getPublicKey();

                keyPair = new KeyPair(publicKey, privateKey);
                Log.i(TAG, "Loaded key from store.");
            }
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
            Log.e(TAG, "Error checking keyStore for aliases!");
        }

        return keyPair;
    }

    /**
     * Encrypts the data and gets the base64 string for the signed data
     *
     * @param data       The data to sign and sign
     * @param privateKey The private key used for signing
     * @return Base64 string with encrypted signed hash
     */
    public static String sign(byte[] data, PrivateKey privateKey) {
        Signature dsa;
        try {
            dsa = Signature.getInstance(SIGN_ALGORITHM);
            dsa.initSign(privateKey);
            dsa.update(data, 0, data.length);

            byte[] signedData = dsa.sign();
            return getBase64String(signedData);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error getting Signature instance", e);
        } catch (InvalidKeyException e) {
            Log.e(TAG, "Error invalid key!", e);
        } catch (SignatureException e) {
            Log.e(TAG, "Error with signature", e);
        }

        return "";
    }

    /**
     * Creates a base64 string from data
     *
     * @param data The data to convert to base64
     * @return Base64 string from the data
     */
    public static String getBase64String(byte[] data) {
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

}
