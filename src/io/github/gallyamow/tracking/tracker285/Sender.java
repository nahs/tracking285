package io.github.gallyamow.tracking.tracker285;


import io.github.gallyamow.tracking.ProtocolSender;
import io.github.gallyamow.tracking.TrackingCoordinate;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

/**
 * Отправляет список координат на трекинг-сервер
 */
public class Sender implements ProtocolSender {
	@Override
	public boolean send(List<? extends TrackingCoordinate> coordinates, Socket socket) throws IOException {
		byte[] body = Packet.getBytes(coordinates);

		socket.getOutputStream().write(body);

		return readResponse(socket) == 0;
	}

	private int readResponse(Socket socket) throws IOException {
		InputStream inputStream = socket.getInputStream();

		int status = -1;

		// смещение 13 байт до результирующего поля
		for (int i = 0; i < 13; i++) {
			status = inputStream.read();
		}

		inputStream.skip(inputStream.available());

		return status;
	}
}
