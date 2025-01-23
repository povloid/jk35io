package k35.io.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Набор утилит для работы с файлами
 */
public class FilesTools {

	static final Logger log = LoggerFactory.getLogger(FilesTools.class);

	/**
	 * Что бы не могли создать экземпляр, так как тут мы будем использовать только
	 * статические методы
	 */
	private FilesTools() {

	}

	/**
	 * Загрузить файл в строку по пути из класса
	 *
	 * @param clazz
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static final String loadStringFromFile(Class<?> clazz, String path) throws IOException {
		return new String(clazz.getResourceAsStream(path).readAllBytes(), StandardCharsets.UTF_8);
	}

	/**
	 * Вычислить контрольную сумму файла по пути
	 *
	 * @param path
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static String calculateFileMd5sum(Path path) throws IOException, NoSuchAlgorithmException {
		log.debug("Вычисление контрольной суммы (MD5) файла по пути {}", path);

		final var md = MessageDigest.getInstance("MD5");

		final var buffer = new byte[1024 * 8];

		try (final var is = new FileInputStream(path.toString())) {

			int read;

			while ((read = is.read(buffer)) > 0) {
				md.update(buffer, 0, read);
			}
		}

		final var digest = md.digest();

		final var md5sum = bytesToHex(digest);

		log.debug("Вычислена контрольная сумма (MD5) {} файла по пути {}", path, md5sum);

		return md5sum;

	}

	/**
	 * Констрольная сумма MD5 строки
	 * 
	 * @param source
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String getMd5Hash(String source) throws NoSuchAlgorithmException {

		var md = MessageDigest.getInstance("MD5");

		md.update(source.getBytes());

		byte[] digest = md.digest();

		return bytesToHex(digest);

	}

	/**
	 * Биты в строковое представление
	 * 
	 * @param bytes
	 * @return
	 */
	public static String bytesToHex(byte[] bytes) {

		var builder = new StringBuilder();

		for (final var b : bytes) {
			builder.append(String.format("%02x", b & 0xff));
		}

		return builder.toString();
	}

	/**
	 * Получить расширение файла
	 *
	 * @param path
	 * @return
	 */
	public static Optional<String> getFileExtension(Path path) {
		final var fileName = path.getFileName().toString();
		return getFileExtension(fileName);
	}

	/**
	 * Получить расширение файла
	 *
	 * @param path
	 * @return
	 */
	public static Optional<String> getFileExtension(String fileName) {
		final int dotIndex = fileName.lastIndexOf('.');
		return (dotIndex == -1) ? Optional.empty() : Optional.of(fileName.substring(dotIndex + 1));
	}

	/**
	 * Получить список файлов директории
	 *
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public static Set<String> listFilesUsingFilesList(String dir) throws IOException {
		try (Stream<Path> stream = Files.list(Paths.get(dir))) {
			return stream.filter(file -> !Files.isDirectory(file)).map(Path::getFileName)
					.map(Path::toString).collect(Collectors.toSet());
		}
	}

	/**
	 * Получить список файлов с проходом в глубину
	 *
	 * @param dir
	 * @param depth
	 * @return
	 * @throws IOException
	 */
	public static List<Path> listFilesUsingFileWalk(String dir, int depth) throws IOException {
		try (Stream<Path> stream = Files.walk(Paths.get(dir), depth, FileVisitOption.FOLLOW_LINKS)) {
			return stream.filter(file -> !Files.isDirectory(file)).collect(Collectors.toList());
		}
	}

}
