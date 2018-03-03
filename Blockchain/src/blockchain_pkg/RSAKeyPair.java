package blockchain_pkg;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;

public class RSAKeyPair {
    private RSAPublicKey publicKey; 
    private RSAPrivateCrtKey privateKey;
	private KeyPair keyPair;
	private Signature sig = null; 
 
     
    public RSAKeyPair(RSAPublicKey publicKey, RSAPrivateCrtKey privateKey) { 
        this.publicKey = publicKey; 
        this.privateKey = privateKey;
    } 
 
    public RSAKeyPair() {
        KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
        keyGen.initialize(512);
        keyPair = keyGen.genKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();
   	}

	@Override 
    public String toString() { 
        return publicKey+"\n"+privateKey; 
    } 
 
    public RSAKey getPublicKey() { 
        return (RSAKey) publicKey; 
    } 
 
    public RSAPrivateCrtKey getPrivateKey() { 
        return privateKey; 
    }

	public byte[] sign(byte[] rawDataToSign) {
        byte[] signed = null;
        try {
			sig = Signature.getInstance("SHA256withRSA");
			sig.initSign(privateKey);
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
        try {
            sig.update(rawDataToSign);
            signed = sig.sign();
		} catch (SignatureException e) {
			e.printStackTrace();
		}		
        return signed;
	} 
}

