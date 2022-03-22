import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class TestMain {
    public static void main(String[] args) {
        Cipher cipher;
        KeyPair pair;

        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(2048);
            pair = keyPairGen.generateKeyPair();
            PublicKey publicKey = pair.getPublic();

            BigInteger exponent = ((RSAPublicKey) publicKey).getPublicExponent();
            BigInteger modulus = ((RSAPublicKey) publicKey).getModulus();
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey pub = factory.generatePublic(spec);

            String msg="hei";
            String msg1;
            String msg2;
            System.out.println("MSG: " + msg);
            cipher.init(Cipher.ENCRYPT_MODE, pub);
            cipher.update(msg.getBytes());
            byte[] bytes;
            byte[] bytes1;
            bytes= cipher.doFinal();
            //msg = new String(cipher.doFinal());
            System.out.println(msg.length());
            cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());
            System.out.println(new String(cipher.doFinal(bytes)));
            msg="hei";
            System.out.println("MSG: " + msg);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            cipher.update(msg.getBytes());
            bytes1 = cipher.doFinal();
            cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());
            System.out.println(new String(cipher.doFinal(bytes1)));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
    }
}
