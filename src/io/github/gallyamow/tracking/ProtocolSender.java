package io.github.gallyamow.tracking;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * Отправляет координаты согласно реализуемому протоколу
 */
public interface ProtocolSender {
	boolean send(List<? extends TrackingCoordinate> coordinates, Socket socket) throws IOException;
}