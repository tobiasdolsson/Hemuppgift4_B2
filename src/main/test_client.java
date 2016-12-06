package main;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.*;
import java.io.*;
import java.math.*;
import java.util.*;

class Client {
	public static void main(String[] args) {
		new Client().run();
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
			BigInteger x2 = new BigInteger("2");
			// calculate g**x2 mod p
			BigInteger g_x2 = g.modPow(x2, p);
			// convert to hex-string and send.
			out.println(g_x2.toString(16));
			// read the ack/nak. This should yield a nak due to x2 being 0
			System.out.println("\nsent g_x2: " + in.readLine());
			
			//Här börjar min kod
			
			BigInteger key = g_x1.modPow(x2, p);
			System.out.println("key: "+key);
			
			//Shared secret:
			
			String secret = key.toString() + "eitn41 <3";
			
			MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e) {

				e.printStackTrace();
			}
			md.update(secret.getBytes());

			byte byteData[] = md.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
			
			System.out.println("Shared Secret: " + sb.toString());
			BigInteger sharedSecret = new BigInteger(sb.toString(),16);
			
			//SOCIALIST MILLIONAIRE
			
			String g_a2_str = in.readLine();
			System.out.println("g_a2 received: "+g_a2_str);
			BigInteger g_a2 = new BigInteger(g_a2_str, 16);
			
			BigInteger b2 = new BigInteger("2");
			BigInteger g_b2 = g.modPow(b2,p);
			out.println(g_b2.toString(16));
			System.out.println("\nsent b2: - " + in.readLine());
			
			String g_a3_str = in.readLine();
			System.out.println("\na3 received: "+g_a3_str);
			BigInteger g_a3 = new BigInteger(g_a3_str, 16);
			
			BigInteger b3 = new BigInteger("2");
			BigInteger g_b3 = g.modPow(b3, p);
			out.println(g_b3.toString(16));
			System.out.println("\nsent b3: - " + in.readLine());
			
			BigInteger g2 = g.modPow(g_a2.multiply(g_b2), p);
			BigInteger g3 = g.modPow(g_a3.multiply(g_b3), p);
			
			String Pa = in.readLine();
			System.out.println("\nPa received: "+Pa);
			
			
			BigInteger Pb = new BigInteger("2");
			BigInteger g_Pb = g3.modPow(Pb, p);
			out.println(g_Pb.toString(16));
			System.out.println("\nsent Pb: - " + in.readLine());
			
			String Qa = in.readLine();
			System.out.println("\nQa received: "+Qa);
			BigInteger Qaa = new BigInteger(Qa,16);
			
			BigInteger Qb = new BigInteger("2");
			BigInteger g_Qb = (g.modPow(Qb,p)).multiply(g2.modPow(sharedSecret, p));
			out.println(g_Qb.toString(16));
			System.out.println("\nsent Qb: - " + in.readLine());
			
			String Qab = in.readLine();
			System.out.println("\nQab received: "+Qab);
			
			BigInteger Qabinv = Qaa.multiply(g_Qb.modInverse(p));
			Qabinv = Qabinv.modPow(b3,p);
			out.println(Qabinv.toString(16));
			System.out.println("\nsent Qabinv: - " + in.readLine());
			
			String auth = in.readLine();
			System.out.println("\nauth received: "+auth);
			
			
			//Skicka meddelande
			BigInteger test = new BigInteger("539",16);
			
			out.println(test);
			
			String response = in.readLine();
			System.out.println(response);

			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
