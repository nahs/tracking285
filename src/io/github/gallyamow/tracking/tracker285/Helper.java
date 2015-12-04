package io.github.gallyamow.tracking.tracker285;

public class Helper {
	public static byte setBit(byte value, int pos) {
		return (byte) (value | (1 << pos));
	}

	public static byte crc8(byte[] data, int len) {
		byte crc = (byte) 0xFF;

		while (len > 0) {

			crc ^= data[data.length - len];

			for (int i = 0; i < 8; i++) {
				byte sum = (byte) ((crc & 0x80) & 0x000000ff);
				if (sum != 0) {
					crc = (byte) (((crc << 1) & 0x000000ff) ^ 0x31);
				} else {
					crc = (byte) ((crc << 1) & 0x000000ff);
				}
			}
			len--;
		}

		return (byte) (crc & 0x000000ff);
	}

	public static short crc16(byte[] data, int len) {
		short crc = (short) 0xFFFF;
		byte i;
		while (len > 0) {
			crc ^= data[data.length - len] << 8;

			for (i = 0; i < 8; i++) {
				short sum = (short) ((crc & 0x8000) & 0x0000ffff);
				if (sum != 0) {
					crc = (short) ((((crc << 1) & 0x0000ffff) ^ 0x1021) & 0x0000ffff);
				} else {
					crc = (short) ((crc << 1) & 0x0000ffff);
				}
			}
			len--;
		}
		return crc;
	}

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 5];

		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 5] = '0';
			hexChars[j * 5 + 1] = 'x';
			hexChars[j * 5 + 2] = hexArray[v >>> 4];
			hexChars[j * 5 + 3] = hexArray[v & 0x0F];
			hexChars[j * 5 + 4] = ' ';
		}
		return new String(hexChars).substring(0, hexChars.length - 1);
	}

	protected static final char[] hexArray = "0123456789abcdef".toCharArray();
}