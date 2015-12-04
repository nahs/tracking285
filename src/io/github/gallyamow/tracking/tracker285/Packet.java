package io.github.gallyamow.tracking.tracker285;

import io.github.gallyamow.tracking.TrackingCoordinate;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Пакет протокола.
 * Протокол поддерживает пакетную отправку координат.
 * Packet = {
 * { Coordinate: { SubRecord: { SrPosData: { координаты } } } }
 * ...
 * { Coordinate: { SubRecord: { SrPosData: { координаты } } } }
 * }
 */
public class Packet {
	private static AtomicInteger counter = new AtomicInteger(0);

	public static byte[] getBytes(List<? extends TrackingCoordinate> coordinates) throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(byteStream);

		// PRV (1): содержит значение 0x01
		dataStream.writeByte(0x01);

		// SKID (1): идентификатор ключа шифрования
		dataStream.writeByte(0);

		// флаги (1)
		dataStream.writeByte(getFlags());

		// HL (1): длина заголовка
		dataStream.writeByte(11);

		// НЕ (1): определяет применяемый метод кодирования = 0
		dataStream.writeByte(0);

		byte[] dataBytes = packCoordinates(coordinates);

		// FDL (2): длина данных
		dataStream.writeShort(Short.reverseBytes((short) dataBytes.length));

		// PID (2): номер пакета
		dataStream.writeShort(Short.reverseBytes((short) counter.incrementAndGet()));

		// РТ (1): тип пакета
		dataStream.writeByte(1);

		// пропущено 2 поля PRA(2), RCA(2), TTL (1)

		// HCS (1): header check sum (crc8 заголовка)
		byte[] headerBytes = byteStream.toByteArray();
		dataStream.writeByte(Helper.crc8(headerBytes, 10));

		// пока 11 байтов

		// SFRD (?): данные
		dataStream.write(dataBytes);

		// SFRCS (0, 2): crc16 данных
		dataStream.writeShort(Short.reverseBytes(Helper.crc16(dataBytes, dataBytes.length)));

		byte[] result = byteStream.toByteArray();

		dataStream.close();
		byteStream.close();

		return result;
	}

	/**
	 * Упаковывает массив координат в массив байтов, результат представляет собой тело пакета.
	 *
	 * @param coordinates координаты
	 * @return массив байтов полученных из списка координат
	 * @throws IOException
	 */
	private static byte[] packCoordinates(List<? extends TrackingCoordinate> coordinates) throws IOException {
		ByteArrayOutputStream coordinatesStream = new ByteArrayOutputStream();

		for (TrackingCoordinate coordinate : coordinates) {
			coordinatesStream.write(new Record(coordinate).getBytes());
		}

		byte[] result = coordinatesStream.toByteArray();

		coordinatesStream.close();

		return result;
	}

	private static byte getFlags() {
		byte flags = 0;

		// 1, 2 бит: PRF префикс заголовка транспортного = 00
		// 3 бит: RTE определяет необходимость дальнейшей маршрутизации = 0
		// 4 бит: ENA (Encryption Algorithm) = 0 (без шифрования)
		// 5, 6 бит: CMP (Compressed) = 0 (без сжатия) = 0
		// 7, 8 бит: PR (Priority) = 0 (наивысший) = 0

		// по дефолту получается нулевой байт
		return flags;
	}
}
