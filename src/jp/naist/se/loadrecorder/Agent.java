package jp.naist.se.loadrecorder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;

public class Agent {

	private static MessageDigest digest;
	private static PrintWriter writer;  

	public static void premain(String agentArgs, Instrumentation inst) {
		try {
			if (agentArgs != null && agentArgs.length() > 0) {
				try {
					writer = new PrintWriter(new File(agentArgs));
					Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
						@Override
						public void run() {
							writer.close();
						}
					}));
				} catch (IOException e) {
					System.err.println(agentArgs + " is not writable.  Use System.err instead.");
				}
			}
			digest = MessageDigest.getInstance("SHA-256");
			
			inst.addTransformer(new ClassFileTransformer() {
				@Override
				public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
						ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
					StringBuilder builder = new StringBuilder();
					CodeSource s = protectionDomain.getCodeSource();
					if (s != null) {
						URL l = s.getLocation();
						builder.append(l.toExternalForm());
					} else {
						builder.append("(Unknown Source)");
					}
					builder.append(",");
					builder.append(className);
					builder.append(",");
					byte[] hash = getHash(classfileBuffer);
					for (byte b: hash) {
						builder.append(String.format("%02x", b));
					}
					String info = builder.toString();
					
					if (writer != null) {
						writer.println(info);
					} else {
						System.err.println(info);
					}
					
					return classfileBuffer;
				}
			});

		} catch (NoSuchAlgorithmException e) {
		}
	}
	
	public static byte[] getHash(byte[] buf) {
		return digest.digest(buf);
	}

}
