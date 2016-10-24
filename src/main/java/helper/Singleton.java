package helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Singleton {
	
    private static final Logger logger = LoggerFactory.getLogger(Singleton.class);

	private static final Singleton inst = new Singleton();

	private Singleton() {
		super();
	}

	public synchronized void writeToFile(Path filePath, String str) {
		try {
			logger.info("Writing to file {}, values {}" , filePath.getFileName(), str);
			Files.write(filePath, str.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			logger.error("Error while writing to file" + filePath.getFileName(), e.getMessage());
		}	}

	public static Singleton getInstance() {
		return inst;
	}
}
