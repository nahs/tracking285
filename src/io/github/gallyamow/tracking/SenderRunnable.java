package io.github.gallyamow.tracking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

abstract public class SenderRunnable implements Runnable {
	private static final String TAG = "SenderRunnable";

	private InetSocketAddress socketAddress;
	private int timeout;

	private ProtocolSender sender;

	public SenderRunnable(String host, int port, int timeout, ProtocolSender sender) {
		socketAddress = new InetSocketAddress(host, port);

		this.timeout = timeout;
		this.sender = sender;
	}

	/**
	 * Метод должен вернуть список еще неотправленных координат
	 *
	 * @return список координат для отправки
	 */
	abstract protected List<? extends TrackingCoordinate> getCoordinates();

	/**
	 * Вызывается когда координаты успешно отправлены
	 *
	 * @param transmitted список отправленных коордиенат
	 */
	abstract protected void onSuccess(List<? extends TrackingCoordinate> transmitted);

	protected void onError(String message, Throwable e) {
		/**
		 * Логирование
		 */
	}

	@Override
	public void run() {
		List<? extends TrackingCoordinate> coordinates = getCoordinates();

		if (coordinates.size() == 0) {
			return;
		}

		boolean success = false;

		Socket socket = new Socket();

		// try-with-resources - api 19
		try {
			socket.connect(socketAddress, timeout);

			success = sender.send(coordinates, socket);
		} catch (IOException e) {
			onError("socket open or send coordinates failed", e);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				onError("socket close failed", e);
			}
		}

		if (success) {
			onSuccess(coordinates);
		}
	}
}
