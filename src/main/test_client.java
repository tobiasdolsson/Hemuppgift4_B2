package main;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.*;
import java.io.*;
import java.math.*;
import java.util.*;
import java.nio.charset.Charset;

class Client {
	public static void main(String[] args) {
		new Client().run();
	}
	
	private byte[] concatenate(byte[] a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}
	
	public String toHex(String arg) {
	    return String.format("%040x", new BigInteger(1, arg.getBytes(Charset.forName("UTF-8"))));
	}
	
	public byte[] removeZeros(byte[] a){
		
		if(a[0]==0){
			byte[] b = new byte[a.length-1];
			for(int i =0; i<b.length; i++){
				b[i] = a[i+1];
			}
			return b;
		} else{
			return a;
		}
				
	}

	void run() {
		String serverName = "eitn41.eit.lth.se";
		
		int port = 1337;
		Random rnd = new Random();
		// the p shall be the one given in the manual
		BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF", 16);
		System.out.println(p);
		BigInteger g = new BigInteger("2");

		try {
			Socket client = new Socket(serverName, port);
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			
			// receive g**x1 and convert to a number
			String g_x1_str = in.readLine();
			System.out.println("g**x1: " + g_x1_str);
			BigInteger g_x1 = new BigInteger(g_x1_str, 16);

			// generate g**x2, x2 shall be a random number
			int r = rnd.nextInt();
			BigInteger x2 = BigInteger.valueOf(r);
			// calculate g**x2 mod p
			BigInteger g_x2 = g.modPow(x2, p);
			// convert to hex-string and send.
			out.println(g_x2.toString(16));
			// read the ack/nak. This should yield a nak due to x2 being 0
			System.out.println("\nsent g_x2: " + in.readLine());
			
			//Create DH key
			BigInteger key = g_x1.modPow(x2, p);
			
			// Create shared secret:
			byte keys[] = key.toByteArray();
			keys = removeZeros(keys); //Remove zeroes beginning of array
			String s = "eitn41 <3";
			byte keys2[] = s.getBytes("UTF-8");
			byte finalkeys[] = concatenate(keys, keys2);
			System.out.println("Shared secret array"+Arrays.toString(finalkeys));
			MessageDigest md = null;
			
			try {
				md = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e) {

				e.printStackTrace();
			}
			
			md.update(finalkeys);
			byte byteData[] = md.digest();
			System.out.println("after sha"+Arrays.toString(byteData));
			BigInteger sharedSecret = new BigInteger(1,byteData); 
			
			//SOCIALIST MILLIONAIRE
			
			//Ta emot a2
			String g_a2_str = in.readLine();
			System.out.println("g_a2 received: "+g_a2_str);
			BigInteger g_a2 = new BigInteger(g_a2_str, 16);
			
			//Skicka b2
			r = rnd.nextInt();
			BigInteger b2 = BigInteger.valueOf(r);
			BigInteger g_b2 = g.modPow(b2,p);
			out.println(g_b2.toString(16));
			System.out.println("\nsent b2: - " + in.readLine());
			
			//Ta emot a3
			String g_a3_str = in.readLine();
			System.out.println("\na3 received: "+g_a3_str);
			BigInteger g_a3 = new BigInteger(g_a3_str, 16);
			
			//Skicka b3
			r = rnd.nextInt();
			BigInteger b3 = BigInteger.valueOf(r);
			BigInteger g_b3 = g.modPow(b3, p);
			out.println(g_b3.toString(16));
			System.out.println("\nsent b3: - " + in.readLine());
			
			//Skapa g2 och g3
			BigInteger g2 = g_a2.modPow(b2,p);
			BigInteger g3 = g_a3.modPow(b3, p);
			
			//Ta emot Pa
			String Pa = in.readLine();
			System.out.println("\nPa received: "+Pa);
			
			//Skicka Pb
			r = rnd.nextInt();
			BigInteger b = BigInteger.valueOf(r);
			BigInteger g3_Pb = g3.modPow(b, p);
			out.println(g3_Pb.toString(16));
			System.out.println("\nsent Pb: - " + in.readLine());
			
			//Ta emot Qa
			String Qa = in.readLine();
			System.out.println("\nQa received: "+Qa);
			BigInteger Qaa = new BigInteger(Qa,16);
			
			//Skicka Qb
			BigInteger g_Qb = g.modPow(b,p).multiply(g2.modPow(sharedSecret, p));
			out.println(g_Qb.toString(16));
			System.out.println("\nsent Qb: - " + in.readLine());
			
			//Ta emot Qab
			String Qab = in.readLine();
			System.out.println("\nQab received: "+Qab);
			
			//Skicka Qabinv
			BigInteger Qabinv = Qaa.multiply(g_Qb.modInverse(p));
			Qabinv = Qabinv.modPow(b3,p);
			out.println(Qabinv.toString(16));
			System.out.println("\nsent Qabinv: - " + in.readLine());
			
			//Ta emot auth
			String auth = in.readLine();
			System.out.println("\nauth received: "+auth);
			
			//Skicka meddelande
			BigInteger test = new BigInteger("0123456789abcdef",16);
			test = test.xor(key);
			out.println(test.toString(16));
			
			String response = in.readLine();
			System.out.println("Response: "+response);

			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
