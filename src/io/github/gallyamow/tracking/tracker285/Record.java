package io.github.gallyamow.tracking.tracker285;

import io.github.gallyamow.tracking.TrackingCoordinate;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Запись Record состоит из одного SubRecord, который состоит из 1 SrPosData записи.
 * Record = { SubRecord: { SrPosData: { координаты } } }
 */
public class Record {
	private static final long UTC_OFFSET_2010 = 1262304000;

	private TrackingCoordinate coordinate;

	public Record(TrackingCoordinate coordinate) {
		this.coordinate = coordinate;
	}

	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(byteStream);

		byte[] subRecord = subRecord();

		// длина subRecord
		dataStream.writeShort(Short.reverseBytes((short) subRecord.length));

		// ?
		dataStream.writeShort(Short.reverseBytes((short) 0x90));

		dataStream.writeByte(getRecordFlags());

		// tracker num
		dataStream.writeInt(Integer.reverseBytes(coordinate.getTracker()));

		// ? timestamp
		dataStream.writeInt(1);

		// ? sst
		dataStream.writeByte(1);

		// ? rst
		dataStream.writeByte(1);

		dataStream.write(subRecord);

		byte[] result = byteStream.toByteArray();

		dataStream.close();
		byteStream.close();

		return result;
	}

	/**
	 * SubRecord
	 */
	private byte[] subRecord() throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(byteStream);

		byte[] posData = srPosData();

		// ? type
		dataStream.writeByte(16);

		// ?
		dataStream.writeShort(Short.reverseBytes((short) 21));

		// данные
		dataStream.write(posData);

		byte[] result = byteStream.toByteArray();

		dataStream.close();
		byteStream.close();

		return result;
	}

	/**
	 * SrPosData
	 */
	private byte[] srPosData() throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(byteStream);

		// NTM (4): время от 2010 года
		int time = (int) (coordinate.getDatetime().getTime() / 1000 - UTC_OFFSET_2010);
		dataStream.writeInt(Integer.reverseBytes(time));

		// LAT (4): широта
		int lat = translateCoordinate(coordinate.getLat(), 90);
		dataStream.writeInt(Integer.reverseBytes(lat));

		// LON (4): долгота
		int lon = translateCoordinate(coordinate.getLon(), 180);
		dataStream.writeInt(Integer.reverseBytes(lon));

		// FLG (1)
		dataStream.writeByte(Short.reverseBytes(getSrPosDataFlags(coordinate)));

		// скорость в км/ч с дискретностью 0,1 км/ч
		int speed = (int) (coordinate.getSpeed() * 3.6); // 3600 / 1000
		dataStream.writeShort(Short.reverseBytes((short) (speed))); // SPD младшие биты

		short direction = (short) coordinate.getBearing();
		dataStream.writeByte(Short.reverseBytes(direction)); // DIR (1): азимут

		// ODM (3): одометр (нет данных)
		dataStream.write(new byte[3]);

		// DIN (1): состояние основных дискретных входов (не передаем)
		dataStream.writeByte(0);

		// SRC (1): источник - таймер при включенном зажигании (не передаем)
		dataStream.writeByte(0);

		// ALT (3): высота над уровнем моря (не передаем)
		// dataStream.write(new byte[3]);

		// SRCD (2): данные об источнике (не передаем)
		// dataStream.write(new byte[2]);

		byte[] result = byteStream.toByteArray();

		dataStream.close();
		byteStream.close();

		return result;
	}

	private short getRecordFlags() {
		short flags = 0;

		flags = (short) (flags | (1 << 7));// has id
//		flags = (short) (flags | (0 << 6));// has event
//		flags = (short) (flags | (0 << 5));// has time
		flags = (short) (flags | (2 << 3));// has priority 2
		flags = (short) (flags | (1 << 2));// group?
		flags = (short) (flags | 1);// src?

		return flags;
	}

	private byte getSrPosDataFlags(TrackingCoordinate coordinate) {
		byte flags = 0;

		// 1 бит - ALTE определяет наличие поля ALT в подзаписи = 0
		// 2 бит - LOHS определяет полушарие долготы

		if (coordinate.getLon() < 0) {
			// западная долгота
			flags = Helper.setBit(flags, 2);
		}

		// 3 бит - LAHS определяет полушарие широты
		if (coordinate.getLat() < 0) {
			// южная широта
			flags = Helper.setBit(flags, 3);
		}

		// 4 бит MV признак движения = 1
		flags = Helper.setBit(flags, 4);

		// 5 бит: ВВ признак отправки данных из памяти = 0
		// 6 бит FIX тип определения координат = 0
		// 7 бит CS тип определения координат = 0

		// 8 бит CS признак "валидности" координатных данных = 1
		flags = Helper.setBit(flags, 8);

		return flags;
	}

	/**
	 * LAT - широта по модулю, градусы/90 · 0xFFFFFFFF и взята целая часть;
	 * LONG - долгота по модулю, градусы/180 · 0xFFFFFFFF и взята целая часть;
	 */
	private int translateCoordinate(double coordinate, int divider) {
		BigDecimal bigCoordinate = BigDecimal.valueOf(coordinate);
		bigCoordinate = bigCoordinate.multiply(BigDecimal.valueOf(0xffffffffL));
		bigCoordinate = BigDecimal.valueOf(bigCoordinate.longValue());
		bigCoordinate = bigCoordinate.divide(BigDecimal.valueOf(divider), RoundingMode.DOWN);

		return bigCoordinate.intValue();
	}
}
