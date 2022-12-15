package com.example.demo;

import java.net.URI;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;

import javax.crypto.Cipher;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FinalApplication {
	public static void main(String[] args) throws Exception{
		Scanner input = new Scanner(System.in);
		boolean isContinue = true;
		
		while(isContinue) {
			System.out.println("Registered as User 2");
			System.out.println("\nMenu: ");
			System.out.println("1. Send Message");
			System.out.println("2. Read Message");
			
			System.out.print("\nYour choice?: ");
			
			int choice = 0;
			
			try {
				choice = input.nextInt();
			} catch (Exception e) {
			}
			
			switch (choice) {
			case 1: {
				sendMessage();
				break;
			}
			case 2: {
				readMessage();
				break;
			}
			default:
				System.out.println("there is no option");
			}
			
			System.out.print("\ncontinue?(y/n): ");
			String continueChoice = input.next();
			
			if (continueChoice.equals("y") || continueChoice.equals("Y")) {
				isContinue = true;
			}else {
				isContinue = false;
			}
			
		}
		
		System.out.println("\nThanks...");
		System.out.println("Neo Jarmawijaya");
		System.out.println("2042006");
	}
	
	public static void sendMessage() throws Exception{
		Scanner input = new Scanner(System.in);
		
		System.out.println("\nPlease fill out the form");
		System.out.print("\nSender: ");
		String sender = input.nextLine();
		
		System.out.print("Receiver: ");
		String receiver = input.nextLine();
		
		System.out.print("Message: ");
		String message = input.nextLine();
		
		// encrypt with public key
		PublicKey pubsKey = Crypto.stringToPublicKey(TempStorage.userTwoPubsKey);
		String encryptedMessage = Crypto.encrypt(message, pubsKey);
		
		// make http call
		saveMessageToDB(sender, receiver, encryptedMessage);
	}
	
	public static void saveMessageToDB(String sender, String receiver, String message) throws Exception {
		var body = "{\"sender\":\"" + sender + "\", \"receiver\":\"" + receiver + "\", \"message\":\"" + message + "\"}";
		
		var client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
		        .uri(URI.create("http://web-service/message"))
		        .header("Content-Type", "application/json; charset=UTF-8")
		        .POST(BodyPublishers.ofString(body))
		        .build();
		
		var response = client.send(request, HttpResponse.BodyHandlers.ofString());
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		ResponseAPI respApi = objectMapper.readValue(response.body(), ResponseAPI.class);
		System.out.println(respApi.message);
	}
	
	
	public static void readMessage() throws Exception {
		PrivateKey privKey = Crypto.stringToPrivateKey(TempStorage.userOnePrivKey);
		Scanner input = new Scanner(System.in);
		
		System.out.print("read message from?: ");
		String sender = input.nextLine();
		
		var client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
		        .uri(URI.create("http://web-service/message?from=" + sender))
		        .header("Content-Type", "application/json; charset=UTF-8")
		        .GET()
		        .build();
		
		var response = client.send(request, HttpResponse.BodyHandlers.ofString());
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		ResponseAPI respApi = objectMapper.readValue(response.body(), ResponseAPI.class);
		System.out.println(respApi.message);
		
		if (respApi.message.equals("there is no messages from "+sender)) return;
		
		System.out.println("there is "+respApi.data.size()+" unread messages from " + sender);
		for (int i = 1; i <= respApi.data.size(); i++) {
			System.out.println(i+". "+Crypto.decrypt(respApi.data.get(i-1).message, privKey));
		}
		
	}
}


class ResponseAPI{
	public boolean success;
	public String message;
	public List<Message> data;
	
	public ResponseAPI(boolean success, String message, List<Message> data) {
		this.success = success;
		this.message = message;
		this.data = data;
	}

	public ResponseAPI() {
		super();
	}
}

class Message{
	public String message;

	public String toString() {
		return this.message;
	}
	
	public Message(String message) {
		this.message = message;
	}
	
	public Message() {
		super();
	}
}

class TempStorage{
	static final String userOnePrivKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIK4b6r/JiaE2xiIIZlbZk9uji+4MtEOZ45p29NbYsmMMilC29QhYI5c+R/pomGP3NvYsPsTq75tXSXq11lR/78vocKVVvvLr62NwBtkoLdeYdPwzz3eA4nixc4GIE3aC5fKNbU6OTM2hk5d2HSOFiyby+ZMhXlHETTDqbnm5UjfAgMBAAECgYAAqQPPXkiIC0W8AvwAdUi3//vx28FN8v+s5XX5xE1kbTPXp4HKuXxuW6PaeGici4h3B8olCn1kkdLVnTEHP3XGNIKkcGeu0jAeoq1tlKS9QxDTfZHhWuxZJ4qNrS3lYVMKdmi8MQVpaIke1l0nC62uSwNPp8eIY3U6v6KzYVYCaQJBAO5mbWFlOpRMnEGnxnRhWtC2kGu6DlOp/SfzSVfrfFBDtLcylfJk/XwJdWEllxv98FOYxR7ZXD9hk7l5lyNzDI0CQQCMXvLxVh+sN+SYnSjMbErqaRfnBQCDnlLPKDLnxQOjwFFTDXxHXMWfyFqLycAvyuqJeP5OApsaix5v7mInsE4bAkEAjx6Oq9nJwR388K14RoXrr0DqksGvuCplAIbdkRqeFtGOvCxyOILIap6DCT836GYa0RT1wf9bkfFSbhbA1VdMQQJAURlqc1S25+FDCYmDUNggUF53mNmi+mg2n8kx4AZLnAg49CsOaoyHAVHYPntJtlMePl7RTnDi++r0ouavfw61kQJAcCR26XP2/mJwpWH5FNXed1xHPRH7kSjQpHlgnhGIotItvPGxrFrnokxb+Gtd2hOSXR6vXBxdsNk7yGpJ+9Uz1A==";
	static final String userOnePubsKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCCuG+q/yYmhNsYiCGZW2ZPbo4vuDLRDmeOadvTW2LJjDIpQtvUIWCOXPkf6aJhj9zb2LD7E6u+bV0l6tdZUf+/L6HClVb7y6+tjcAbZKC3XmHT8M893gOJ4sXOBiBN2guXyjW1OjkzNoZOXdh0jhYsm8vmTIV5RxE0w6m55uVI3wIDAQAB";

	static final String userTwoPrivKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJsCPiSITePM2TX1Jw6d6MWukQu0Ax/v3AzY+SRWGGnlwvVv6xxdA0qvqMXcogLG6dhUe12K3vdHj3EF3DOL/phjTV0j3E+GARH6A9yYDyD1bUxK+DesV58Gni7Yf7aveGtw1Cn1R1I19EMPq75NAKtAA7CVfohadNTm5+NO9ncrAgMBAAECgYAXQbgNfCPk50xkU5gHZzrQRLeUwBlDQUDMmVXmv+zMIAG8bbfNRXP5JdorzEVvJuVZT0eZH9JXMIs9eRRyPsa1CL+COyGluzYp5ZLD2+48ILss3sbZlMRJQzanVUYTvPlBnOiqjc28NNJxmBbP/zimY9rjl7N/FUoDbBaxMoRliQJBALXvrHrHiWmcuUxPRAuH6pvVIOwoUf1458p0JFEo1IPZQ0U62oARqaFV4gjPtqwIZa9sGusSOT/Sn0a50DcJJ4MCQQDaHFi38RZmBbbXffrnqF3b++LR85Ps1B5gvdZodalAchirs8ayE6bkHzrBENpf1aRZqd8UDwWQgFGynCTg9bk5AkEAl2CJEUtu0Pn+zzhwtGttUCWgE/5kYdM0gSXrhb/EOsQODc8vODn5+uwbextgsXj3KmN3bjmmeq0Dq3Q1g2VRgwJANAfjGi5HG5ttXMbO3giKK3pRR6iFt09617UxusWm1JrjS8KdOcn6XB0fFQOCNK5wgHtHni9fMnOKCcO6AHhd8QJBAKKuEMf0+wFiqRDCY7VJKUEDwb6HDC0fLFPC8KQWdm7TEjip1UMGtvADOi6JcUuwE9WHGGXQHXjAYvLfeIRh9Pc=";
	static final String userTwoPubsKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCbAj4kiE3jzNk19ScOnejFrpELtAMf79wM2PkkVhhp5cL1b+scXQNKr6jF3KICxunYVHtdit73R49xBdwzi/6YY01dI9xPhgER+gPcmA8g9W1MSvg3rFefBp4u2H+2r3hrcNQp9UdSNfRDD6u+TQCrQAOwlX6IWnTU5ufjTvZ3KwIDAQAB";
}

class Crypto {
	public static PrivateKey stringToPrivateKey(String key) throws Exception {
        byte[] baseKey = decode(key);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(baseKey));
    }

	public static PublicKey stringToPublicKey(String key) throws Exception {
        byte[] baseKey = decode(key);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(baseKey));
    }
    
    private static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }
    
    private static byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    public static String encrypt(String message, PublicKey key) throws Exception {
    	byte[] messageToBytes = message.getBytes();
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(messageToBytes);
        return encode(encryptedBytes);
    }

    public static String decrypt(String encryptedMessage, PrivateKey key) throws Exception {
    	byte[] encryptedBytes = decode(encryptedMessage);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
        return new String(decryptedMessage, "UTF8");
    }
}

